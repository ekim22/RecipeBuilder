package com.sdp.recipebuilder

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.sdp.recipebuilder.adapter.StepAdapter
import com.sdp.recipebuilder.model.Step


class StepFragment(private val steps: HashMap<String, String>) : Fragment() {
    private var TAG = StepFragment::class.qualifiedName
    private var stepView: View? = null
    private var stepRecyclerView: RecyclerView? = null
    private var stepList = ArrayList<Step>()
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        for ((k, v) in steps.toSortedMap()) {
            Log.d(TAG, "onCreate: sorted map: $k $v")
            stepList.add(Step(k,v))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        stepView = inflater.inflate(R.layout.fragment_step, container, false)
        stepRecyclerView = stepView!!.findViewById(R.id.step_recycler) as RecyclerView
        val adapter = this.context?.let { StepAdapter(it, stepList) }
        stepRecyclerView!!.layoutManager = LinearLayoutManager(this.context)
        stepRecyclerView!!.adapter = adapter

        return stepView
    }

//    override fun onStart() {
//        super.onStart()
//        db = FirebaseFirestore.getInstance()
//    }



}