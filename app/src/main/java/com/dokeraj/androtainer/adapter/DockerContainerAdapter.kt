package com.dokeraj.androtainer.adapter

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
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
                //setStateForRunning(currentItem.status.capitalize(), position, holder)
                println("U RANING")

                setCardStyle(containerState = ContainerStateType.running,
                    statusText = currentItem.status.capitalize(),
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.disGreen,
                    buttonText = "STOP",
                    buttonIsEnabled = true,
                    buttonColor = R.color.btn_lister,
                    statusIconImage = R.drawable.docker_status_icon,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )

            }
            ContainerStateType.exited -> {
                //setStateForExited(currentItem.status.capitalize(), position, holder)

                println("U EXITED")

                setCardStyle(containerState = ContainerStateType.exited,
                    statusText = currentItem.status.capitalize(),
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.disRed,
                    buttonText = "START",
                    buttonIsEnabled = true,
                    buttonColor = R.color.btn_lister,
                    statusIconImage = R.drawable.docker_status_icon,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )

            }
            ContainerStateType.transitioning -> {
                //setStateForTransitioning(currentItem.status, position, holder)

                println("U TRANSITIONING")

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
                //setStateForError(currentItem.status, position, holder)

                println("U ERROERD")

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
        val statusIconView: ImageView = itemView.statusIcon
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

        // setStateForTransitioning(transitioningText, currentItemNum, holder)
        println("E AJDE TRANZICIJA")
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
                        println("VO 204 i action-ot so e praten: ${actionType.name}")
                        when (actionType) {
                            ContainerActionType.START -> {
                                /*setStateForRunning("Started just now",
                                    currentItemNum,
                                    holder)*/
                                setCardStyle(containerState = ContainerStateType.running,
                                    statusText = "Started just now",
                                    statusTextColor = R.color.disText1,
                                    cardBckColor = R.color.disGreen,
                                    buttonText = "STOP",
                                    buttonIsEnabled = true,
                                    buttonColor = R.color.btn_lister,
                                    statusIconImage = R.drawable.docker_status_icon,
                                    statusIconColor = R.color.disText1,
                                    currentItemNum = currentItemNum,
                                    holder = holder
                                )
                            }
                            ContainerActionType.STOP -> {/*
                                setStateForExited("Exited just now",
                                    currentItemNum,
                                    holder)*/
                                setCardStyle(containerState = ContainerStateType.exited,
                                    statusText = "Exited just now",
                                    statusTextColor = R.color.disText1,
                                    cardBckColor = R.color.disRed,
                                    buttonText = "START",
                                    buttonIsEnabled = true,
                                    buttonColor = R.color.btn_lister,
                                    statusIconImage = R.drawable.docker_status_icon,
                                    statusIconColor = R.color.disText1,
                                    currentItemNum = currentItemNum,
                                    holder = holder
                                )
                            }
                        }
                    }
                    else -> {
                        //setStateForError("Refresh by swiping down", currentItemNum, holder)
                        println("ERROR: response code: ${response.code()}")
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
                // todo:: snackbar to logout manually!!
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

        println("AJDE OVDE SME!! holder text: ${holder.dockerNameView.text} i currentItemText: ${currentItem.name}")
        if (holder.dockerNameView.text.toString() == currentItem.name) {

            println("A VNATRE????")

            // change cardHolderLayout background
            holder.cardHolderLayout.background.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    cardBckColor), BlendMode.SRC)

            // statusView text and color
            holder.dockerStatusView.text = currentItem.status.capitalize()
            holder.dockerStatusView.setTextColor(ContextCompat.getColor(contekst,
                statusTextColor))

            // change button background
            holder.dockerButton.text = buttonText
            holder.dockerButton.isEnabled = buttonIsEnabled
            val btnBackground = holder.dockerButton.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    buttonColor), BlendMode.SRC)
            holder.dockerButton.background = btnBackground

            // Status Icon
            holder.statusIconView.setImageResource(statusIconImage)
            holder.statusIconView.setColorFilter(ContextCompat.getColor(contekst,
                statusIconColor))
        }
    }

    private fun setStateForExited(
        statusText: String,
        currentItemNum: Int,
        holder: ContainerViewHolder,
    ) {
        pContainerList[currentItemNum].state = ContainerStateType.exited
        pContainerList[currentItemNum].status = statusText
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
            holder.dockerStatusView.setTextColor(ContextCompat.getColor(contekst,
                R.color.disText1))

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
        statusText: String,
        currentItemNum: Int,
        holder: ContainerViewHolder,
    ) {
        pContainerList[currentItemNum].state = ContainerStateType.running
        pContainerList[currentItemNum].status = statusText
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
            holder.dockerStatusView.setTextColor(ContextCompat.getColor(contekst,
                R.color.disText1))

            // change button background
            val btnBackground = holder.dockerButton.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.btn_lister), BlendMode.SRC)
            holder.dockerButton.background = btnBackground

        }
    }

    private fun setStateForTransitioning(
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
            holder.dockerStatusView.setTextColor(ContextCompat.getColor(contekst,
                R.color.disText1))

            // change button background
            val btnBackground = holder.dockerButton.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.dis6), BlendMode.SRC)
            holder.dockerButton.background = btnBackground
        }
    }

    private fun setStateForError(
        // todo:: add to class the API logo
        statusText: String,
        currentItemNum: Int,
        holder: ContainerViewHolder,
    ) {
        pContainerList[currentItemNum].state = ContainerStateType.errored
        pContainerList[currentItemNum].status = statusText
        val currentItem = pContainerList[currentItemNum]

        if (holder.dockerNameView.text.toString().trim().capitalize() == currentItem.name.trim()
                .capitalize()
        ) {
            holder.cardHolderLayout.background.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.disYellow), BlendMode.SRC)
            holder.dockerStatusView.text = currentItem.status.capitalize()
            holder.dockerButton.text = "Error"
            holder.dockerButton.isEnabled = false
            holder.dockerStatusView.setTextColor(ContextCompat.getColor(contekst,
                R.color.disText3))

            // change button background
            val btnBackground = holder.dockerButton.background
            btnBackground.mutate()
            btnBackground.colorFilter =
                BlendModeColorFilter(ContextCompat.getColor(contekst,
                    R.color.disYellow), BlendMode.SRC)
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