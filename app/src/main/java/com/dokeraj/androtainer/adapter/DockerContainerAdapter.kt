package com.dokeraj.androtainer.adapter

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dokeraj.androtainer.Interfaces.ApiInterface
import com.dokeraj.androtainer.R
import com.dokeraj.androtainer.models.*
import com.dokeraj.androtainer.network.RetrofitInstance
import kotlinx.android.synthetic.main.docker_card_item.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DockerContainerAdapter(
    private var pContainerList: List<PContainer>,
    baseUrl: String?,
    jwt: String?,
    private val contekst: Context,
) :
    RecyclerView.Adapter<DockerContainerAdapter.ContainerViewHolder>() {

    val bUrl: String = baseUrl!!
    val bJwt: String = jwt!!


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
        val itemViewX =
            LayoutInflater.from(parent.context).inflate(R.layout.docker_card_item, parent, false)

        return ContainerViewHolder(itemViewX)
    }


    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        val currentItem: PContainer = pContainerList[position]

        holder.dockerNameView.text = currentItem.name.trim().capitalize()

        when (currentItem.state) {
            ContainerStateType.running -> {
                setStateForRunning(currentItem.status.capitalize(), position, holder)
            }
            ContainerStateType.exited -> {
                setStateForExited(currentItem.status.capitalize(), position, holder)
            }
            ContainerStateType.transitioning -> {
                setTransitioningStyle(currentItem.status, position, holder)
            }
            /**else -> { // todo:: wait to see if there are any more states
            holder.dockerButton.text = "Issue!"
            holder.dockerNameView.text = currentItem.name
            holder.dockerStatusView.text = currentItem.status
            holder.dockerButton.isEnabled = false
            holder.dockerButton.background.colorFilter =
            BlendModeColorFilter(ContextCompat.getColor(contekst,
            R.color.disRed), BlendMode.SRC)
            }*/
        }

        holder.dockerButton.setOnClickListener {
            startStopDockerContainer(bUrl,
                bJwt,
                currentItem.id,
                if (pContainerList[position].state == ContainerStateType.running) ContainerActionType.STOP else ContainerActionType.START,
                holder,
                position)
        }

    }

    override fun getItemCount() = pContainerList.size

    class ContainerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dockerNameView: TextView = itemView.etDockerName
        val dockerStatusView: TextView = itemView.etDockerStatus
        val dockerButton: Button = itemView.btnStartStop
        val cardHolderLayout: ConstraintLayout = itemView.cardHolderLayout
    }

    private fun startStopDockerContainer(
        url: String,
        jwt: String,
        containerId: String,
        actionType: ContainerActionType,
        holder: ContainerViewHolder,
        currentItemNum: Int,
    ) {
        val header = "Bearer $jwt"

        val urlToCall =
            "${url.removeSuffix("/")}/api/endpoints/1/docker/containers/$containerId/${actionType.name.toLowerCase()}"

        println("URL TO CALL: ${urlToCall}")
        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)

        // change the style and disable button
        val transitioningText =
            if (actionType == ContainerActionType.START) "Starting" else "Exiting"
        setTransitioningStyle(transitioningText, currentItemNum, holder)

        api.startStopContainer(header, urlToCall).enqueue(object : Callback<Unit?> {
            override fun onResponse(call: Call<Unit?>, response: Response<Unit?>) {

                when (response.code()) {
                    204 -> {
                        println("VO 204 i action-ot so e praten: ${actionType.name}")
                        when (actionType) {
                            ContainerActionType.START -> setStateForRunning(null,
                                currentItemNum,
                                holder)
                            ContainerActionType.STOP -> setStateForExited(null,
                                currentItemNum,
                                holder)
                        }
                    }
                    304 -> {
                        println("No change in state")
                    }
                    else -> {
                        // some problem?!?!
                        println("DADE NEKOJ CUDEN KOD: ${response.code()}")
                    }
                }

            }

            override fun onFailure(call: Call<Unit?>, t: Throwable) {
                println("FAILURE!!! na START/STOP!!!!!! ${t.message}")
            }
        })
    }

    private fun setStateForExited(
        statusText: String?,
        currentItemNum: Int,
        holder: ContainerViewHolder,
    ) {
        pContainerList[currentItemNum].state = ContainerStateType.exited
        pContainerList[currentItemNum].status = statusText ?: "Exited just now"
        val currentItem = pContainerList[currentItemNum]


        if (holder.dockerNameView.text.toString().trim().capitalize() == currentItem.name.trim()
                .capitalize()
        ) {
            // change cardHolderLayout background
            holder.cardHolderLayout.background.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.disRed), BlendMode.SRC)
            holder.dockerStatusView.text = currentItem.status.capitalize()
            holder.dockerButton.text = ContainerActionType.START.name
            holder.dockerButton.isEnabled = true

            // change button background
            val btnBackground = holder.dockerButton.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.btn_lister), BlendMode.SRC)
            holder.dockerButton.background = btnBackground
        }
    }

    private fun setStateForRunning(
        statusText: String?,
        currentItemNum: Int,
        holder: ContainerViewHolder,
    ) {
        pContainerList[currentItemNum].state = ContainerStateType.running
        pContainerList[currentItemNum].status = statusText ?: "Started just now"
        val currentItem = pContainerList[currentItemNum]

        if (holder.dockerNameView.text.toString().trim().capitalize() == currentItem.name.trim()
                .capitalize()
        ) {
            holder.cardHolderLayout.background.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.disGreen), BlendMode.SRC)
            holder.dockerStatusView.text = currentItem.status.capitalize()
            holder.dockerButton.text = ContainerActionType.STOP.name
            holder.dockerButton.isEnabled = true

            // change button background
            val btnBackground = holder.dockerButton.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.btn_lister), BlendMode.SRC)
            holder.dockerButton.background = btnBackground

        }
    }

    private fun setTransitioningStyle(
        statusText: String,
        currentItemNum: Int,
        holder: ContainerViewHolder,
    ) {
        pContainerList[currentItemNum].state = ContainerStateType.transitioning
        pContainerList[currentItemNum].status = statusText
        val currentItem = pContainerList[currentItemNum]

        if (holder.dockerNameView.text.toString().trim().capitalize() == currentItem.name.trim()
                .capitalize()
        ) {
            holder.cardHolderLayout.background.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.dis6), BlendMode.SRC)
            holder.dockerStatusView.text = currentItem.status.capitalize()
            holder.dockerButton.text = statusText
            holder.dockerButton.isEnabled = false

            // change button background
            val btnBackground = holder.dockerButton.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.dis6), BlendMode.SRC)
            holder.dockerButton.background = btnBackground
        }
    }

    fun setItems(newContainerList: List<PContainer>) {
        this.pContainerList = newContainerList
    }

    fun areItemsInTransitioningState(): Boolean {
        return pContainerList.any { x -> x.state == ContainerStateType.transitioning }
    }
}