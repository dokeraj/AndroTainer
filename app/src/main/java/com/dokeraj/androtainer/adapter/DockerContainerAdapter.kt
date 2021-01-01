package com.dokeraj.androtainer.adapter

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
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
    private val baseUrl: String,
    private val jwt: String,
    private val context: Context,
) :
    RecyclerView.Adapter<DockerContainerAdapter.ContainerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.docker_card_item, parent, false)

        return ContainerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        val currentItem: PContainer = pContainerList[position]

        holder.dockerNameView.text = currentItem.name

        when (currentItem.state) {
            ContainerStateType.running -> {
                /** set style for running docker container */
                setCardStyle(containerState = ContainerStateType.running,
                    statusText = currentItem.status.capitalize(),
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.disGreen,
                    buttonText = "STOP",
                    buttonIsEnabled = true,
                    buttonColor = R.color.blue_main,
                    statusIconImage = R.drawable.docker_status_icon,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )
            }
            ContainerStateType.exited -> {
                /** set style for stopped docker container */
                setCardStyle(containerState = ContainerStateType.exited,
                    statusText = currentItem.status.capitalize(),
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.disRed,
                    buttonText = "START",
                    buttonIsEnabled = true,
                    buttonColor = R.color.blue_main,
                    statusIconImage = R.drawable.docker_status_icon,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )
            }
            ContainerStateType.transitioning -> {
                /** set style for container that is either starting or stopping */
                setCardStyle(containerState = ContainerStateType.transitioning,
                    statusText = currentItem.status,
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.dis6,
                    buttonText = currentItem.status,
                    buttonIsEnabled = false,
                    buttonColor = R.color.dis6,
                    statusIconImage = R.drawable.docker_status_icon,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )
            }
            ContainerStateType.errored -> {
                /** set style for docker container that has received error from portainer api */
                setCardStyle(containerState = ContainerStateType.errored,
                    statusText = "Refresh by swiping down",
                    statusTextColor = R.color.disText3,
                    cardBckColor = R.color.disYellow,
                    buttonText = "ERROR",
                    buttonIsEnabled = false,
                    buttonColor = R.color.disYellow,
                    statusIconImage = R.drawable.warning_logo,
                    statusIconColor = R.color.disText3,
                    currentItemNum = position,
                    holder = holder
                )
            }
        }

        holder.dockerButton.setOnClickListener {
            startStopDockerContainer(baseUrl,
                jwt,
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
        val dockerButton: View = itemView.btnStartStop
        val btnBackgroundView: ConstraintLayout = itemView.findViewById(R.id.clLister)
        val btnProgressBar: ProgressBar = itemView.findViewById(R.id.pbLister)
        val btnTextView: TextView = itemView.findViewById(R.id.tvLister)
        val cardHolderLayout: ConstraintLayout = itemView.cardHolderLayout
        val statusIconView: ImageView = itemView.statusIcon
    }

    private fun startStopDockerContainer(
        baseUrl: String,
        jwt: String,
        containerId: String,
        actionType: ContainerActionType,
        holder: ContainerViewHolder,
        currentItemNum: Int,
    ) {
        val header = "Bearer $jwt"

        val urlToCall = context.getString(R.string.StartStopContainer)
            .replace("{baseUrl}", baseUrl.removeSuffix("/"))
            .replace("{containerId}", containerId)
            .replace("{actionType}", actionType.name.toLowerCase())

        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)

        // change the style and disable button
        val transitioningText =
            if (actionType == ContainerActionType.START) "Starting" else "Exiting"

        setCardStyle(containerState = ContainerStateType.transitioning,
            statusText = transitioningText,
            statusTextColor = R.color.disText1,
            cardBckColor = R.color.dis6,
            buttonText = transitioningText,
            buttonIsEnabled = false,
            buttonColor = R.color.dis6,
            statusIconImage = R.drawable.docker_status_icon,
            statusIconColor = R.color.disText1,
            currentItemNum = currentItemNum,
            holder = holder
        )

        api.startStopContainer(header, urlToCall).enqueue(object : Callback<Unit?> {
            override fun onResponse(call: Call<Unit?>, response: Response<Unit?>) {
                when (response.code()) {
                    204 -> {
                        when (actionType) {
                            ContainerActionType.START -> {
                                setCardStyle(containerState = ContainerStateType.running,
                                    statusText = "Started just now",
                                    statusTextColor = R.color.disText1,
                                    cardBckColor = R.color.disGreen,
                                    buttonText = "STOP",
                                    buttonIsEnabled = true,
                                    buttonColor = R.color.blue_main,
                                    statusIconImage = R.drawable.docker_status_icon,
                                    statusIconColor = R.color.disText1,
                                    currentItemNum = currentItemNum,
                                    holder = holder
                                )
                            }
                            ContainerActionType.STOP -> {
                                setCardStyle(containerState = ContainerStateType.exited,
                                    statusText = "Exited just now",
                                    statusTextColor = R.color.disText1,
                                    cardBckColor = R.color.disRed,
                                    buttonText = "START",
                                    buttonIsEnabled = true,
                                    buttonColor = R.color.blue_main,
                                    statusIconImage = R.drawable.docker_status_icon,
                                    statusIconColor = R.color.disText1,
                                    currentItemNum = currentItemNum,
                                    holder = holder
                                )
                            }
                        }
                    }
                    else -> {
                        setCardStyle(containerState = ContainerStateType.errored,
                            statusText = "Refresh by swiping down",
                            statusTextColor = R.color.disText3,
                            cardBckColor = R.color.disYellow,
                            buttonText = "ERROR",
                            buttonIsEnabled = false,
                            buttonColor = R.color.disYellow,
                            statusIconImage = R.drawable.warning_logo,
                            statusIconColor = R.color.disText3,
                            currentItemNum = currentItemNum,
                            holder = holder
                        )
                    }
                }
            }

            override fun onFailure(call: Call<Unit?>, t: Throwable) {
                setCardStyle(containerState = ContainerStateType.errored,
                    statusText = "Refresh by swiping down",
                    statusTextColor = R.color.disText3,
                    cardBckColor = R.color.disYellow,
                    buttonText = "ERROR",
                    buttonIsEnabled = false,
                    buttonColor = R.color.disYellow,
                    statusIconImage = R.drawable.warning_logo,
                    statusIconColor = R.color.disText3,
                    currentItemNum = currentItemNum,
                    holder = holder
                )
            }
        })
    }

    private fun setCardStyle(
        containerState: ContainerStateType,
        statusText: String,
        statusTextColor: Int,
        cardBckColor: Int,
        buttonText: String,
        buttonIsEnabled: Boolean,
        buttonColor: Int,
        statusIconImage: Int,
        statusIconColor: Int,
        currentItemNum: Int,
        holder: ContainerViewHolder,
    ) {
        pContainerList[currentItemNum].state = containerState
        pContainerList[currentItemNum].status = statusText
        val currentItem = pContainerList[currentItemNum]

        if (holder.dockerNameView.text.toString() == currentItem.name) {
            // change cardHolderLayout background
            holder.cardHolderLayout.background.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(context,
                    cardBckColor), BlendMode.SRC)

            // statusView text and color
            holder.dockerStatusView.text = currentItem.status.capitalize()
            holder.dockerStatusView.setTextColor(ContextCompat.getColor(context,
                statusTextColor))

            // change button background
            holder.btnTextView.text = buttonText
            holder.dockerButton.isClickable = buttonIsEnabled
            val btnBackground = holder.btnBackgroundView.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(context,
                    buttonColor), BlendMode.SRC)
            holder.btnBackgroundView.background = btnBackground

            // Status Icon
            holder.statusIconView.setImageResource(statusIconImage)
            holder.statusIconView.setColorFilter(ContextCompat.getColor(context,
                statusIconColor))

            // Progress Bar
            holder.btnProgressBar.visibility =
                if (containerState == ContainerStateType.transitioning) View.VISIBLE else View.GONE
        }
    }

    fun setItems(newContainerList: List<PContainer>) {
        this.pContainerList = newContainerList
    }

    fun areItemsInTransitioningState(): Boolean {
        return pContainerList.any { pCont -> pCont.state == ContainerStateType.transitioning }
    }
}