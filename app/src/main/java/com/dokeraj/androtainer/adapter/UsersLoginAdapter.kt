package com.dokeraj.androtainer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.dokeraj.androtainer.MainActiviy
import com.dokeraj.androtainer.R
import com.dokeraj.androtainer.models.Credential
import kotlinx.android.synthetic.main.users_card_item.view.*

class UsersLoginAdapter(
    private val credentials: List<Credential>,
    val usersDrawerLayout: DrawerLayout,
    val homeView: View,
    val mainActiviy: MainActiviy,
    val context: Context,
) :
    RecyclerView.Adapter<UsersLoginAdapter.UsersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.users_card_item, parent, false)

        return UsersViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val currentItem: Credential = credentials[position]

        holder.serverUrl.text = currentItem.serverUrl
        holder.username.text = currentItem.username

        holder.cardLayout.setOnClickListener {
            val etUrl: TextView = homeView.findViewById(R.id.etUrl)
            val etUser: TextView = homeView.findViewById(R.id.etUser)
            val etPass: TextView = homeView.findViewById(R.id.etPass)

            etUrl.text = currentItem.serverUrl
            etUser.text = currentItem.username
            etPass.text = currentItem.pwd

            mainActiviy.showGenericSnack(context,homeView,"Loaded `${currentItem.username}` credentials!", R.color.blue_main, R.color.dis3)
            usersDrawerLayout.close()
        }
    }

    override fun getItemCount() = credentials.size

    class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serverUrl: TextView = itemView.tvServerUrl
        val username: TextView = itemView.tvCardUsername
        val cardLayout: ConstraintLayout = itemView.usersCardHolderLayout
    }
}