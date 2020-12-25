package com.dokeraj.androtainer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dokeraj.androtainer.HomeFragmentDirections
import com.dokeraj.androtainer.R
import com.dokeraj.androtainer.models.PContainer
import kotlinx.android.synthetic.main.docker_card_item.view.*

import kotlinx.android.synthetic.main.fragment_home.*
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.dokeraj.androtainer.DockerListerFragmentDirections

class DockerContainerAdapter(private var PContainerList: List<PContainer>) :
    RecyclerView.Adapter<DockerContainerAdapter.ExampleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val itemViewX =
            LayoutInflater.from(parent.context).inflate(R.layout.docker_card_item, parent, false)

        return ExampleViewHolder(itemViewX)
    }

    private val selectedPosition = mutableMapOf<Int, Boolean>()
    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        val currentItem = PContainerList[position]

        PContainerList[0].name = "go menjame"

        holder.dockerNameView.text = currentItem.name
        holder.dockerStateView.text = currentItem.state



        if (selectedPosition.getOrDefault(position, false)) {
            println("TRUE")
            holder.dockerButton.text = "start"
        } else {
            println("EVE ${holder.dockerButton.text}")
            holder.dockerButton.text = "Stop"
        }

        holder.dockerButton.setOnClickListener(View.OnClickListener {
            /*if (selectedPosition.getOrDefault(position, false)) {
                selectedPosition.put(position, true)
                println("true: $selectedPosition")
                println("TRUE")
                holder.dockerButton.text = "Start"
            } else {
                selectedPosition.put(position, true)
                println("fal: $selectedPosition")
                println("EVE ${holder.dockerButton.text}")
                holder.dockerButton.text = "Stop"
            }*/


        })

    }

    override fun getItemCount() = PContainerList.size

    class ExampleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dockerNameView: TextView = itemView.etDockerName
        val dockerStateView: TextView = itemView.etDockerState
        val dockerButton: Button = itemView.btnStartStop

    }
}