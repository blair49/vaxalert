package com.blairfernandes.vaxalert

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.blairfernandes.vaxalert.databinding.ActivityMainBinding
import com.blairfernandes.vaxalert.model.SessionDetails
import com.blairfernandes.vaxalert.service.SESSION_DETAILS


private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(){

    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var navGraph: NavGraph

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationFragment) as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        navGraph = graphInflater.inflate(R.navigation.navigation)
        navController = navHostFragment.navController

        if (intent.getParcelableArrayListExtra<SessionDetails>(SESSION_DETAILS) != null){
            Log.d(TAG, "Starting results fragment")
            navGraph.startDestination = R.id.resultsFragment
        } else{
            Log.d(TAG, "Starting create alert fragment")
            navGraph.startDestination = R.id.createAlertFragment
        }
        navController.graph = navGraph

    }

    override fun onNewIntent(intent: Intent?) {
        Log.d(TAG, "onNewIntent called")
        super.onNewIntent(intent)
        if (intent?.getParcelableArrayListExtra<SessionDetails>(SESSION_DETAILS) != null){
            Log.d(TAG, "Starting results fragment")
            navController.navigate(
                R.id.action_createAlertFragment_to_resultsFragment,
                intent.extras
            )
        } /*else{
            Log.d(TAG, "Starting create alert fragment")
            navGraph.startDestination = R.id.createAlertFragment
        }
        navController.graph = navGraph*/
    }

    override fun onSupportNavigateUp() = findNavController(R.id.navigationFragment).navigateUp()
}