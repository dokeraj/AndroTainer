package com.dokeraj.androtainer.adapter

import android.R.attr
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
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.dokeraj.androtainer.DockerListerFragment
import com.dokeraj.androtainer.DockerListerFragmentDirections
import com.dokeraj.androtainer.R
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.models.ContainerActionType
import com.dokeraj.androtainer.models.ContainerStateType
import com.dokeraj.androtainer.models.Kontainer
import com.dokeraj.androtainer.models.KontainerFilterPref
import com.dokeraj.androtainer.viewmodels.DockerListerViewModel
import com.dokeraj.androtainer.viewmodels.MainStateEvent
import kotlinx.android.synthetic.main.docker_card_item.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import android.R.attr.right

import android.R.attr.left




class DockerContainerAdapter(
    private var pContainerList: List<Kontainer>,
    private val baseUrl: String,
    private val jwt: String,
    private val isUsingApiKey: Boolean,
    private val endpointId: Int,
    private val globalApp: GlobalApp,
    private val context: Context,
    private val frag: DockerListerFragment,
    private val dataViewModel: DockerListerViewModel,
) :
    RecyclerView.Adapter<DockerContainerAdapter.ContainerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContainerViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.docker_card_item, parent, false)

        return ContainerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContainerViewHolder, position: Int) {
        val currentItem: Kontainer = pContainerList[position]

        holder.dockerNameView.text = currentItem.name

        when (currentItem.state) {
            ContainerStateType.RUNNING -> {
                /** set style for running docker container */
                setCardStyle(containerState = ContainerStateType.RUNNING,
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.disGreen,
                    buttonText = "STOP",
                    buttonIsEnabled = true,
                    buttonColor = R.color.blue_main,
                    statusIconImage = R.drawable.ic_docker_status,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )
                setVisibilityByFilter(globalApp.appSettings!!.kontainerFilter, currentItem.state, holder)
            }
            ContainerStateType.EXITED -> {
                /** set style for stopped docker container */
                setCardStyle(containerState = ContainerStateType.EXITED,
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.disRed,
                    buttonText = "START",
                    buttonIsEnabled = true,
                    buttonColor = R.color.blue_main,
                    statusIconImage = R.drawable.ic_docker_status,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )
                setVisibilityByFilter(globalApp.appSettings!!.kontainerFilter, currentItem.state, holder)
            }
            ContainerStateType.TRANSITIONING -> {
                /** set style for container that is either starting or stopping */
                setCardStyle(containerState = ContainerStateType.TRANSITIONING,
                    statusTextColor = R.color.disText1,
                    cardBckColor = R.color.dis6,
                    buttonText = currentItem.status,
                    buttonIsEnabled = false,
                    buttonColor = R.color.dis6,
                    statusIconImage = R.drawable.ic_docker_status,
                    statusIconColor = R.color.disText1,
                    currentItemNum = position,
                    holder = holder
                )
                setVisibilityByFilter(globalApp.appSettings!!.kontainerFilter, currentItem.state, holder)
            }
            ContainerStateType.ERRORED -> {
                /** set style for docker container that has received error from portainer api */
                setCardStyle(containerState = ContainerStateType.ERRORED,
                    statusTextColor = R.color.disText3,
                    cardBckColor = R.color.disYellow,
                    buttonText = "ERROR",
                    buttonIsEnabled = false,
                    buttonColor = R.color.disYellow,
                    statusIconImage = R.drawable.ic_warning,
                    statusIconColor = R.color.disText3,
                    currentItemNum = position,
                    holder = holder
                )
                setVisibilityByFilter(globalApp.appSettings!!.kontainerFilter, currentItem.state, holder)
            }
            ContainerStateType.CREATED -> {
                /** set style for docker container that is in the created state */
                setCardStyle(containerState = ContainerStateType.CREATED,
                    statusTextColor = R.color.disText2,
                    cardBckColor = R.color.teal_700,
                    buttonText = "START",
                    buttonIsEnabled = true,
                    buttonColor = R.color.blue_main,
                    statusIconImage = R.drawable.ic_created,
                    statusIconColor = R.color.disText2,
                    currentItemNum = position,
                    holder = holder
                )
                setVisibilityByFilter(globalApp.appSettings!!.kontainerFilter, currentItem.state, holder)
            }
        }

        holder.dockerButton.setOnClickListener {
            callStartStopContainer(currentItemIndex = position,
                containerId = currentItem.id,
                actionType = if (pContainerList[position].state == ContainerStateType.RUNNING) ContainerActionType.STOP else ContainerActionType.START)
        }

        holder.cardHolderLayout.setOnClickListener {
            dataViewModel.setStateEvent(MainStateEvent.SetNone)
            val action =
                DockerListerFragmentDirections.actionDockerListerFragmentToDockerContainerDetailsFragment(
                    currentItem)
            findNavController(frag).navigate(action)
        }

        holder.cardHolderLayout.setOnLongClickListener {
            val action = DockerListerFragmentDirections.actionDockerListerFragmentToDockerLogging(
                currentItem.id,
                currentItem.name)
            findNavController(frag).navigate(action)
            true
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

    @ExperimentalCoroutinesApi
    private fun callStartStopContainer(
        currentItemIndex: Int,
        containerId: String,
        actionType: ContainerActionType,
    ) {
        val fullUrl = context.getString(R.string.StartStopContainer)
            .replace("{baseUrl}", baseUrl.removeSuffix("/"))
            .replace("{containerId}", containerId)
            .replace("{actionType}", actionType.name.toLowerCase())
            .replace("{endpointId}", endpointId.toString())

        dataViewModel.setStateEvent(MainStateEvent.StartStopKontejneri(jwt = jwt,
            url = fullUrl,
            isUsingApiKey = isUsingApiKey,
            currentItem = currentItemIndex,
            containerActionType = actionType))
    }

    private fun setCardStyle(
        containerState: ContainerStateType,
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
            holder.dockerButton.isEnabled = buttonIsEnabled
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
                if (containerState == ContainerStateType.TRANSITIONING) View.VISIBLE else View.GONE
        }
    }

    fun setItems(newContainerList: List<Kontainer>) {
        this.pContainerList = newContainerList
    }

    fun areItemsInTransitioningState(): Boolean {
        return pContainerList.any { pCont -> pCont.state == ContainerStateType.TRANSITIONING }
    }

    private fun setVisibilityByFilter(
        kontainerFilterPref: KontainerFilterPref,
        holderState: ContainerStateType,
        holder: ContainerViewHolder,
    ) {
        when (kontainerFilterPref) {
            KontainerFilterPref.RUNNING -> when (holderState) {
                ContainerStateType.CREATED -> showHideCard(true, holder)
                ContainerStateType.RUNNING -> showHideCard(true, holder)
                ContainerStateType.ERRORED -> showHideCard(false, holder)
                ContainerStateType.EXITED -> showHideCard(false, holder)
                ContainerStateType.TRANSITIONING -> showHideCard(true, holder)
            }

            KontainerFilterPref.TOTAL -> when (holderState) {
                ContainerStateType.CREATED -> showHideCard(true, holder)
                ContainerStateType.RUNNING -> showHideCard(true, holder)
                ContainerStateType.ERRORED -> showHideCard(true, holder)
                ContainerStateType.EXITED -> showHideCard(true, holder)
                ContainerStateType.TRANSITIONING -> showHideCard(true, holder)
            }

            KontainerFilterPref.STOPPED_OR_ERRORED -> when (holderState) {
                ContainerStateType.CREATED -> showHideCard(false, holder)
                ContainerStateType.RUNNING -> showHideCard(false, holder)
                ContainerStateType.ERRORED -> showHideCard(true, holder)
                ContainerStateType.EXITED -> showHideCard(true, holder)
                ContainerStateType.TRANSITIONING -> showHideCard(true, holder)
            }
        }
    }

    private fun showHideCard(showCard: Boolean, holder: ContainerViewHolder) {
        if (showCard) {
            holder.itemView.visibility = View.VISIBLE

            val params = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

            params.bottomMargin = 8

            holder.itemView.layoutParams = params
        } else {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
    }
}