package com.blairfernandes.vaxalert

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.blairfernandes.vaxalert.databinding.FragmentResultsBinding
import com.blairfernandes.vaxalert.model.SessionDetails
import com.blairfernandes.vaxalert.service.SESSION_DETAILS


/**
 * A simple [Fragment] subclass.
 */
class ResultsFragment : Fragment() {

    private var sessionDetailsList: ArrayList<SessionDetails>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ResultsFragment", "Fragment onCreate called")
        sessionDetailsList = arguments?.getParcelableArrayList(SESSION_DETAILS)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        Toast.makeText(context, "Got ${sessionDetailsList?.size ?: 0} sessions", Toast.LENGTH_SHORT).show()
        val binding = FragmentResultsBinding.inflate(inflater, container, false)
        binding.resultsHeading.text = getString(R.string.foundSessions, sessionDetailsList?.size ?: 0)
        val adapter = SessionDetailsAdapter(sessionDetailsList)

        binding.resultList.adapter = adapter
        binding.modifySearchBtn.setOnClickListener {
            findNavController().navigate(R.id.action_resultsFragment_to_createAlertFragment)
        }

        return binding.root
    }

}