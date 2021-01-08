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
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.dokeraj.androtainer.MainActiviy
import com.dokeraj.androtainer.R
import com.dokeraj.androtainer.dialogs.DeleteCurrentUserDiag
import com.dokeraj.androtainer.models.Credential
import kotlinx.android.synthetic.main.users_card_item.view.*

class ManageUsersAdapter(
    private var credentials: List<Credential>,
    val curLoggedUserKey: String,
    val mainActivity: MainActiviy,
    private val context: Context,
    val fragmentManager: FragmentManager,
) :
    RecyclerView.Adapter<ManageUsersAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.users_card_item, parent, false)

        return UsersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val currentItem: Credential = credentials[position]

        holder.tvServerUrl.text = currentItem.serverUrl
        holder.tvUsername.text = currentItem.username
        holder.btnDelete.visibility = View.VISIBLE

        holder.cardHolderLayout.background.colorFilter =
            BlendModeColorFilter(ContextCompat.getColor(context,
                R.color.dis5), BlendMode.SRC)

        if ("${currentItem.serverUrl}.${currentItem.username}" == curLoggedUserKey)
            holder.tvCurrentUser.visibility = View.VISIBLE
        else
            holder.tvCurrentUser.visibility = View.GONE

        holder.btnDelete.setOnClickListener {

            if (mainActivity.isUserCurrentlyLoggedIn(currentItem)) {
                val dialog = DeleteCurrentUserDiag(currentItem)
                dialog.show(fragmentManager, "Choose wisely")
            } else {
                removeUser(currentItem, position)
                mainActivity.deleteUser(currentItem)
            }
        }

    }

    override fun getItemCount() = credentials.size

    class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvServerUrl: TextView = itemView.tvServerUrl
        val tvUsername: TextView = itemView.tvCardUsername
        val tvCurrentUser: TextView = itemView.tvLoggedUserWarrning
        val btnDelete: Button = itemView.btnUserDelete
        val cardHolderLayout: ConstraintLayout = itemView.usersCardHolderLayout
    }

    fun removeUser(currentItem: Credential, position: Int) {
        val newCreds: List<Credential> = credentials.filterNot {
            "${it.serverUrl}.${it.username}" == "${currentItem.serverUrl}.${currentItem.username}"
        }
        credentials = newCreds
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}