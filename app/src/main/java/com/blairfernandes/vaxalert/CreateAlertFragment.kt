package com.blairfernandes.vaxalert

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.startForegroundService
import androidx.fragment.app.Fragment
import com.blairfernandes.vaxalert.databinding.CreateAlertFragmentBinding
import com.blairfernandes.vaxalert.model.District
import com.blairfernandes.vaxalert.model.State
import com.blairfernandes.vaxalert.service.*
import com.blairfernandes.vaxalert.util.*
import com.google.android.material.internal.TextWatcherAdapter
import com.google.gson.Gson

private const val TAG = "CreateAlertFragment"

class CreateAlertFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var binding: CreateAlertFragmentBinding

    private lateinit var stateAdapter: ArrayAdapter<State>
    private lateinit var districtAdapter: ArrayAdapter<District>
    private lateinit var searchByAdapter: ArrayAdapter<String>

    //private val agePredicates = ArrayList<(Session) -> Boolean>()
    private val ageFilters = ArrayList<String>()
    private val costFilters = ArrayList<String>()
    private val doseFilters = ArrayList<String>()
    private val vaxFilters = ArrayList<String>()

    private val sharedPrefsManager by lazy { SharedPrefsManager(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = CreateAlertFragmentBinding.inflate(inflater, container, false)
        getPreferences(requireContext()).registerOnSharedPreferenceChangeListener(this)
        Log.d(TAG, "onCreateView called")
        return binding.root
    }

    override fun onDetach() {
        super.onDetach()
        getPreferences(requireContext()).unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchByItems = listOf("PIN", "District")
        searchByAdapter =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, searchByItems)

        binding.content.menuSearchby.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val selectedItem: String = s.toString()
                if (selectedItem == "PIN") {
                    binding.content.stateTextInputLayout.visibility = View.GONE
                    binding.content.districtTextInputLayout.visibility = View.GONE
                    binding.content.textInputPincode.visibility = View.VISIBLE
                } else if (selectedItem == "District") {
                    binding.content.textInputPincode.visibility = View.GONE
                    binding.content.stateTextInputLayout.visibility = View.VISIBLE
                    binding.content.districtTextInputLayout.visibility = View.VISIBLE
                }
            }
        })
        binding.content.menuSearchby.setAdapter(searchByAdapter)
        binding.content.menuSearchby.setText(getString(R.string.searchByPin), false)

        LocationService.getStates { success ->
            if (success) {
                Log.i(TAG, "Get states Request successful")
                stateAdapter = ArrayAdapter(
                    view.context,
                    android.R.layout.simple_spinner_dropdown_item,
                    LocationService.states
                )
                binding.content.stateDropdown.setAdapter(stateAdapter)
            } else {
                Log.i(TAG, "An error occurred while getting states")
            }
        }

        binding.content.stateDropdown.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                clearErrors()
                //val selectedState = s.toString()
                val state: State? = LocationService.states.find { it.stateName.contentEquals(s) }
                if (state != null) {
                    LocationService.getDistricts(state.stateId) { success ->
                        if (success) {
                            Log.i(TAG, "Get districts request successful")
                            districtAdapter = ArrayAdapter(
                                view.context,
                                android.R.layout.simple_spinner_dropdown_item,
                                LocationService.districts
                            )
                            binding.content.districtDropdown.setAdapter(districtAdapter)
                            binding.content.districtDropdown.setText("", false)
                        }

                    }
                } else {
                    binding.content.stateTextInputLayout.error = "Please enter a valid state name"
                }
            }
        })

        binding.content.btnStartSearching.setOnClickListener {

            if (getServiceState(requireContext()) == ServiceState.STOPPED) {
                if(validateInput()) {
                    Log.d(TAG, "Starting Search")
                    saveFilters()
                    actionSearchService(Actions.START)
                }
            } else {
                Log.d(TAG, "Stopping Search")
                actionSearchService(Actions.STOP)
            }
        }

        binding.content.chipAge18Plus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !ageFilters.contains(AGE_18_PLUS)) {
                ageFilters.add(AGE_18_PLUS)
                //agePredicates.add{ it.min_age_limit==18 && it.allow_all_age }
            } else {
                ageFilters.removeAll(listOf(AGE_18_PLUS))
                //agePredicates.remove{ it.min_age_limit==18 && it.allow_all_age }
            }
        }

        binding.content.chipAge1844.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !ageFilters.contains(AGE_18_44)) {
                ageFilters.add(AGE_18_44)
                //agePredicates.add{ it.min_age_limit==18 && it.max_age_limit==44 }
            } else {
                ageFilters.removeAll(listOf(AGE_18_44))
                //agePredicates.remove{ it.min_age_limit==18 && it.max_age_limit==44 }
            }
        }

        binding.content.chipAge45Plus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !ageFilters.contains(AGE_45_PLUS)) {
                ageFilters.add(AGE_45_PLUS)
                //agePredicates.add{ it.min_age_limit==45 }
            } else {
                ageFilters.removeAll(listOf(AGE_45_PLUS))
                //agePredicates.remove{ it.min_age_limit==45 }
            }
        }

        binding.content.chipCostFree.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !costFilters.contains(COST_FREE)) {
                costFilters.add(COST_FREE)
            } else {
                costFilters.removeAll(listOf(COST_FREE))
            }
        }

        binding.content.chipCostPaid.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !costFilters.contains(COST_PAID)) {
                costFilters.add(COST_PAID)
            } else {
                costFilters.removeAll(listOf(COST_PAID))
            }
        }

        binding.content.chipDose1.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !doseFilters.contains(DOSE_1)) {
                doseFilters.add(DOSE_1)
            } else {
                doseFilters.removeAll(listOf(DOSE_1))
            }
        }

        binding.content.chipDose2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !doseFilters.contains(DOSE_2)) {
                doseFilters.add(DOSE_2)
            } else {
                doseFilters.removeAll(listOf(DOSE_2))
            }
        }

        binding.content.chipVaccineCovaxin.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !vaxFilters.contains(COVAXIN)) {
                vaxFilters.add(COVAXIN)
            } else {
                vaxFilters.removeAll(listOf(COVAXIN))
            }
        }

        binding.content.chipVaccineCovishield.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !vaxFilters.contains(COVISHIELD)) {
                vaxFilters.add(COVISHIELD)
            } else {
                vaxFilters.removeAll(listOf(COVISHIELD))
            }
        }

        binding.content.chipVaccineSputnikv.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !vaxFilters.contains(SPUTNIKV)) {
                vaxFilters.add(SPUTNIKV)
            } else {
                vaxFilters.removeAll(listOf(SPUTNIKV))
            }
        }

        when (getServiceState(requireContext())) {
            ServiceState.STOPPED -> {
                setInputActive(true)
                binding.content.btnStartSearching.text = getString(R.string.startSearching)
            }
            else -> {
                setInputActive(false)
                binding.content.btnStartSearching.text = getString(R.string.stopSearching)
            }
        }
        restoreFilters()

        if(sharedPrefsManager.searchBy == 0){
            binding.content.menuSearchby.setText(getString(R.string.searchByPin), false)
            binding.content.etPincode.setText(sharedPrefsManager.pin)
        }else{
            binding.content.menuSearchby.setText(getString(R.string.searchByDistrict), false)
            binding.content.stateDropdown.setText(sharedPrefsManager.state)
            binding.content.districtDropdown.setText(sharedPrefsManager.district, false)
        }

    }

    private fun saveFilters() {
        val gson = Gson()
        val ageFiltersJsonString = gson.toJson(ageFilters)
        val costFiltersJsonString = gson.toJson(costFilters)
        val doseFiltersJsonString = gson.toJson(doseFilters)
        val vaxFiltersJsonString = gson.toJson(vaxFilters)
        sharedPrefsManager.ageFilters = ageFiltersJsonString
        sharedPrefsManager.costFilters = costFiltersJsonString
        sharedPrefsManager.doseFilters = doseFiltersJsonString
        sharedPrefsManager.vaxFilters = vaxFiltersJsonString
        Log.d(
            TAG,
            "Saved Filters $ageFiltersJsonString $costFiltersJsonString $doseFiltersJsonString $vaxFiltersJsonString to prefs"
        )
    }

    private fun validateInput(): Boolean {
        clearErrors()
        val searchBy = binding.content.menuSearchby.text.toString()

        if (searchBy == "PIN") {

            if (binding.content.etPincode.text.isNullOrBlank() || binding.content.etPincode.text.toString().length < 6) {
                binding.content.textInputPincode.error = "Enter a valid PIN code"
                return false
            }

        } else {

            if (binding.content.districtDropdown.text.isNullOrBlank()) {
                binding.content.districtTextInputLayout.error = "Please select a district"
                return false
            }

            val selectedDistrict = binding.content.districtDropdown.text.toString()
            val district: District? =
                LocationService.districts.find { it.districtName == selectedDistrict }

            if (district == null) {
                binding.content.districtTextInputLayout.error = "Please enter a valid district name"
                return false
            }

        }

        return true
    }

    private fun actionSearchService(action: Actions) {

        if (getServiceState(requireContext()) == ServiceState.STOPPED && action == Actions.STOP) return

        val intent = Intent(context, SearchService::class.java)
        intent.action = action.name

        if(action == Actions.START) {
            val searchBy = binding.content.menuSearchby.text.toString()
            if (searchBy == "PIN") {
                intent.putExtra(SEARCH_BY, 0)
                sharedPrefsManager.searchBy = 0
                val pin = binding.content.etPincode.text.toString()

                intent.putExtra(PIN, pin)
                sharedPrefsManager.pin = pin
            } else {
                intent.putExtra(SEARCH_BY, 1)
                sharedPrefsManager.searchBy = 1
                val selectedDistrict = binding.content.districtDropdown.text.toString()
                val district: District? =
                    LocationService.districts.find { it.districtName == selectedDistrict }
                val districtId = district!!.districtId

                intent.putExtra(DISTRICT_ID, districtId)
                sharedPrefsManager.district = selectedDistrict
                sharedPrefsManager.state = binding.content.stateDropdown.text.toString()
            }
        }
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //Log.d(TAG, "Starting the service in >=26 Mode")
        startForegroundService(requireContext(), intent)
        return
        //}

        //Log.d(TAG, "Starting the service in < 26 Mode")
        //startService(it)
    }

    private fun clearErrors() {
        binding.content.textInputPincode.error = ""
        binding.content.stateTextInputLayout.error = ""
        binding.content.districtTextInputLayout.error = ""
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (getServiceState(requireContext())) {
            ServiceState.STOPPED -> {
                setInputActive(true)
                binding.content.btnStartSearching.text = getString(R.string.startSearching)
            }
            else -> {
                setInputActive(false)
                binding.content.btnStartSearching.text = getString(R.string.stopSearching)
            }
        }
    }

    private fun setInputActive(isEnabled: Boolean) {
        binding.content.textInputLayout.isEnabled = isEnabled
        binding.content.textInputPincode.isEnabled = isEnabled
        binding.content.stateTextInputLayout.isEnabled = isEnabled
        binding.content.districtTextInputLayout.isEnabled = isEnabled

        binding.content.chipAge18Plus.isEnabled = isEnabled
        binding.content.chipAge1844.isEnabled = isEnabled
        binding.content.chipAge45Plus.isEnabled = isEnabled

        binding.content.chipCostFree.isEnabled = isEnabled
        binding.content.chipCostPaid.isEnabled = isEnabled

        binding.content.chipDose1.isEnabled = isEnabled
        binding.content.chipDose2.isEnabled = isEnabled

        binding.content.chipVaccineCovishield.isEnabled = isEnabled
        binding.content.chipVaccineCovaxin.isEnabled = isEnabled
        binding.content.chipVaccineSputnikv.isEnabled = isEnabled
    }

    private fun restoreFilters() {
        Log.d(TAG, "restoreFilters called")
        try {
            val gson = Gson()
            val lAgeFilters = gson.fromJson(sharedPrefsManager.ageFilters, ArrayList::class.java)
            val lCostFilters = gson.fromJson(sharedPrefsManager.costFilters, ArrayList::class.java)
            val lDoseFilters = gson.fromJson(sharedPrefsManager.doseFilters, ArrayList::class.java)
            val lVaxFilters = gson.fromJson(sharedPrefsManager.vaxFilters, ArrayList::class.java)
            for (filter in lAgeFilters) {
                when (filter) {
                    AGE_18_PLUS -> {
                        binding.content.chipAge18Plus.isChecked = true
                        if (!ageFilters.contains(AGE_18_PLUS)) ageFilters.add(AGE_18_PLUS)
                    }
                    AGE_18_44 -> {
                        binding.content.chipAge1844.isChecked = true
                        if(!ageFilters.contains(AGE_18_44)) ageFilters.add(AGE_18_44)
                    }
                    AGE_45_PLUS -> {
                        binding.content.chipAge45Plus.isChecked = true
                        if (!ageFilters.contains(AGE_45_PLUS)) ageFilters.add(AGE_45_PLUS)
                    }
                }
            }
            for (filter in lCostFilters) {
                when (filter) {
                    COST_FREE -> {
                        binding.content.chipCostFree.isChecked = true
                        if(!costFilters.contains(COST_FREE)) costFilters.add(COST_FREE)
                    }
                    COST_PAID -> {
                        binding.content.chipCostPaid.isChecked = true
                        if(!costFilters.contains(COST_PAID)) costFilters.add(COST_PAID)
                    }
                }
            }
            for (filter in lDoseFilters) {
                when (filter) {
                    DOSE_1 -> {
                        binding.content.chipDose1.isChecked = true
                        if(!doseFilters.contains(DOSE_1)) doseFilters.add(DOSE_1)
                    }
                    DOSE_2 -> {
                        binding.content.chipDose2.isChecked = true
                        if(!doseFilters.contains(DOSE_2)) doseFilters.add(DOSE_2)
                    }
                }
            }
            for (filter in lVaxFilters) {
                when (filter) {
                    COVAXIN -> {
                        binding.content.chipVaccineCovaxin.isChecked = true
                        if(!vaxFilters.contains(COVAXIN)) vaxFilters.add(COVAXIN)
                    }
                    COVISHIELD -> {
                        binding.content.chipVaccineCovishield.isChecked = true
                        if(!vaxFilters.contains(COVISHIELD)) vaxFilters.add(COVISHIELD)
                    }
                    SPUTNIKV -> {
                        binding.content.chipVaccineSputnikv.isChecked = true
                        if(!vaxFilters.contains(SPUTNIKV)) vaxFilters.add(SPUTNIKV)
                    }
                }
            }
        } catch (e:Exception){
            Log.d(TAG, "error occurred while getting filters")
        }
    }

}