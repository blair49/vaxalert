package com.blairfernandes.vaxalert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blairfernandes.vaxalert.databinding.SessionDetailsItemBinding
import com.blairfernandes.vaxalert.model.SessionDetails

class SessionDetailsAdapter(private val sessionDetailsList: List<SessionDetails>?) : RecyclerView.Adapter<SessionDetailsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: SessionDetailsItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SessionDetailsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            with(sessionDetailsList?.get(position)){
                binding.tvCenterName.text = this?.centerName ?: ""
                binding.tvCenterAddress.text = this?.centerAddress ?: ""
                binding.tvVaxName.text = this?.vaccine ?: ""
                binding.tvCost.text = this?.cost ?: ""
                binding.tvDose1Capacity.text = itemView.context.getString(R.string.tvDose1, this?.available_capacity_dose1)
                binding.tvDose2Capacity.text = itemView.context.getString(R.string.tvDose2, this?.available_capacity_dose2)
                binding.tvAge.text = this?.age ?: ""
                binding.tvDate.text = this?.date ?: ""
            }
        }
    }

    override fun getItemCount(): Int {
        return sessionDetailsList?.size?: 0
    }
}