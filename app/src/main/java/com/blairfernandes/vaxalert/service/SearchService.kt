package com.blairfernandes.vaxalert.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.blairfernandes.vaxalert.MainActivity
import com.blairfernandes.vaxalert.R
import com.blairfernandes.vaxalert.model.Center
import com.blairfernandes.vaxalert.model.Session
import com.blairfernandes.vaxalert.model.SessionDetails
import com.blairfernandes.vaxalert.util.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

const val SESSION_DETAILS = "sessionDetails"
private const val TAG = "SearchService"

class SearchService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false

    private val agePredicates = ArrayList<(Session) -> Boolean>()
    private val costPredicates = ArrayList<(Center) -> Boolean>()
    private val dosePredicates = ArrayList<(Session) -> Boolean>()
    private val vaxPredicates = ArrayList<(Session) -> Boolean>()

    private val sharedPrefsManager by lazy { SharedPrefsManager(applicationContext) }
    private val notificationChannelId = "SEARCH SERVICE CHANNEL"
    private val resultsNotificationChannelId = "SEARCH SERVICE Results CHANNEL"
    private val alertNotificationId = 1

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "The service has been created")
        getFilters()
        val notification = createNotification()
        startForeground(1, notification)
    }

    private fun getFilters() {
        val gson = Gson()
        val ageFilters = gson.fromJson(sharedPrefsManager.ageFilters, ArrayList::class.java)
        val costFilters = gson.fromJson(sharedPrefsManager.costFilters, ArrayList::class.java)
        val doseFilters = gson.fromJson(sharedPrefsManager.doseFilters, ArrayList::class.java)
        val vaxFilters = gson.fromJson(sharedPrefsManager.vaxFilters, ArrayList::class.java)
        for (filter in ageFilters) {
            when (filter) {
                AGE_18_PLUS -> {
                    agePredicates.add { it.min_age_limit == 18 && it.allow_all_age }
                }
                AGE_18_44 -> {
                    agePredicates.add { it.min_age_limit == 18 && it.max_age_limit == 44 }
                }
                AGE_45_PLUS -> {
                    agePredicates.add { it.min_age_limit == 45 }
                }
            }
        }
        for (filter in costFilters) {
            when (filter) {
                COST_FREE -> {
                    costPredicates.add { it.fee_type == "Free" }
                }
                COST_PAID -> {
                    costPredicates.add { it.fee_type == "Paid" }
                }
            }
        }
        for (filter in doseFilters) {
            when (filter) {
                DOSE_1 -> {
                    dosePredicates.add { it.isDose1Available }
                }
                DOSE_2 -> {
                    dosePredicates.add { it.isDose2Available }
                }
            }
        }
        for (filter in vaxFilters) {
            when (filter) {
                COVAXIN -> {
                    vaxPredicates.add { it.vaccine == "COVAXIN" }
                }
                COVISHIELD -> {
                    vaxPredicates.add { it.vaccine == "COVISHIELD" }
                }
                SPUTNIKV -> {
                    vaxPredicates.add { it.vaccine == "SPUTNIK V" }
                }
            }
        }
    }

    private fun createNotification(): Notification {


        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Searching notification",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Search service channel"
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                PendingIntent.getActivity(this, 101, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Searching vaccination sessions")
            .setContentText("Looking for sessions based on your criteria")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // for under API 26 compatibility
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand executed with startId: $startId")
        if (intent != null) {
            val action = intent.action
            Log.d(TAG, "using an intent with action $action")
            when (action) {
                Actions.START.name -> {
                    val searchBy = intent.getIntExtra(SEARCH_BY, 0)
                    if (searchBy == 0) {
                        val pin: String = intent.getStringExtra(PIN).toString()
                        startService(searchBy, pin)
                    } else {
                        val districtId: Int = intent.getIntExtra(DISTRICT_ID, 0)
                        startService(searchBy, districtId)
                    }
                }
                Actions.STOP.name -> stopService()
                else -> Log.d(TAG, "This should never happen. No action in the received intent")
            }
        } else {
            Log.d(
                TAG,
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "The service has been destroyed")
        Toast.makeText(this, "Stopped Searching", Toast.LENGTH_SHORT).show()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, SearchService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent =
            PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent
        )
    }

    private fun startService(searchBy: Int, searchCriteria: Any) {
        if (isServiceStarted) return
        Log.d(TAG, "Starting the foreground service task")
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                launch(Dispatchers.IO) {
                    searchSessions(searchBy, searchCriteria)
                }
                delay(3 * 1000)
            }
        }
    }

    private fun stopService() {
        Log.d(TAG, "Stopping the foreground service")
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d(TAG, "Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
    }

    private fun searchSessions(searchBy: Int, searchCriteria: Any) {
        Log.d(
            TAG,
            "searchSessions started with parameters searchBy: $searchBy, searchCriteria: $searchCriteria"
        )
        val jsonParser = JsonParser()
        if (searchBy == 0) {
            //search by pin
            val pin: String = searchCriteria as String
            SessionService.findCalendarByPin(pin) { success, responseBodyString ->
                if (success) {
                    Log.d(TAG, "findCalendarByPin: Request successful")
                    try {
                        val centersArray = JSONObject(responseBodyString).getJSONArray("centers")
                        jsonParser.parseCentersJSON(centersArray, findAvailableSessions)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException occured: ${e.message}")
                    }
                } else {
                    Log.d(TAG, "findCalendarByPin: Something went wrong")
                }
            }
        } else {
            //search by district
            val districtId: Int = searchCriteria as Int
            SessionService.findCalendarByDistrict(districtId) { success, responseBodyString ->
                if (success) {
                    Log.d(TAG, "findCalendarByDistrict: Request successful")
                    try {
                        val centersArray = JSONObject(responseBodyString).getJSONArray("centers")
                        jsonParser.parseCentersJSON(centersArray, findAvailableSessions)
                    } catch (e: JSONException) {
                        Log.e(TAG, "JSONException occured: ${e.message}")
                    }

                } else {
                    Log.d(TAG, "findCalendarByDistrict: Something went wrong")
                }
            }
        }
        return
    }

    private val findAvailableSessions = fun(centers: List<Center>) {
        val sessionDetailsList = ArrayList<SessionDetails>()
        var centers = centers

        if (costPredicates.isNotEmpty()) {
            centers = centers.filter { center -> costPredicates.any { it(center) } }
        }

        for (center in centers) {
            var sessions = center.sessions.filter { it.isAvailable }
            if (agePredicates.isNotEmpty()) {
                sessions = sessions.filter { session -> agePredicates.any { it(session) } }
            }
            if (dosePredicates.isNotEmpty()) {
                sessions = sessions.filter { session -> dosePredicates.any { it(session) } }
            }
            if (vaxPredicates.isNotEmpty()) {
                sessions = sessions.filter { session -> vaxPredicates.any { it(session) } }
            }

            for (session in sessions) {
                val centerAddress =
                    "${center.address}, ${center.district_name}, ${center.state_name}, ${center.pincode}"
                val age =
                    when {
                        session.min_age_limit == 18 && session.allow_all_age -> "18 & Above"
                        session.min_age_limit == 18 && session.max_age_limit == 44 -> "18 - 44 only"
                        session.min_age_limit == 45 -> "45 & Above"
                        else -> "NA"
                    }
                sessionDetailsList.add(
                    SessionDetails(
                        center.name,
                        centerAddress,
                        center.fee_type,
                        session.date,
                        age,
                        session.vaccine,
                        session.available_capacity_dose1,
                        session.available_capacity_dose2
                    )
                )
                Log.d(
                    TAG,
                    "findAvailableSessions: Found session at ${center.name} | Available capacity: ${session.available_capacity} | Date: ${session.date} | Min age: ${session.min_age_limit} | Cost: ${center.fee_type} | dose1: ${session.available_capacity_dose1} | dose2: ${session.available_capacity_dose2} | vaccine: ${session.vaccine}"
                )
            }
        }
        if (sessionDetailsList.size > 0) {
            stopService()
            sessionsFoundNotification(sessionDetailsList)
        }

    }

    private fun sessionsFoundNotification(sessionDetailsList: ArrayList<SessionDetails>) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                resultsNotificationChannelId,
                "Search results notification",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Search service results channel"

            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { resultsIntent ->
                resultsIntent.putParcelableArrayListExtra(SESSION_DETAILS, sessionDetailsList)
                resultsIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                PendingIntent.getActivity(this, 102, resultsIntent, PendingIntent.FLAG_CANCEL_CURRENT)
            }

        val notification = NotificationCompat.Builder(this, resultsNotificationChannelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle("Found ${sessionDetailsList.size} vaccination sessions")
            .setContentText("Vaccines available at these centers")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(alertNotificationId, notification)
    }

}

