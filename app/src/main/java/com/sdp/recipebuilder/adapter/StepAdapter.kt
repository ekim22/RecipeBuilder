package com.sdp.recipebuilder.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.sdp.recipebuilder.R
import com.sdp.recipebuilder.model.Step

class StepAdapter(private val context: Context, private val stepList: List<Step>) : RecyclerView.Adapter<StepAdapter.StepViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StepViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_step,
                parent, false)
        return StepViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: StepViewHolder, position: Int) {
        val currentStep = stepList[position]
        Log.d("StepAdapter", "onBindViewHolder: currentStep " + currentStep.toString())
        holder.stepView.text = currentStep.step
        holder.stepView.setOnClickListener {
            Toast.makeText(context, "Delete the step", Toast.LENGTH_SHORT).show()
        }

    }

    override fun getItemCount() = stepList.size

    class StepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stepView: TextView = itemView.findViewById(R.id.step_list_item)
    }
}