package com.dokeraj.androtainer.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.dokeraj.androtainer.MainActiviy
import com.dokeraj.androtainer.ManageUsersListerFragmentDirections
import com.dokeraj.androtainer.R
import com.dokeraj.androtainer.models.Credential
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.fragment_delete_current_user_dialog.view.*

class DeleteCurrentUserDiag(
    val selectedUser: Credential,
) : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val rootView: View =
            inflater.inflate(R.layout.fragment_delete_current_user_dialog, container, false)
        val mainActivity: MainActiviy = (activity as MainActiviy?)!!

        val markwon = Markwon.builder(requireContext()).build()
        markwon.setMarkdown(rootView.tv_diag_explanation, getString(R.string.dialogWarning))

        rootView.btn_delete_user_cancel.setOnClickListener {
            dismiss()
        }

        // if the user chooses continue, then delete the currently logged in user (if there are other saved users, get the other latest used user and set that user as current user) and return to the login page
        rootView.btn_delete_user_continue.setOnClickListener {
            mainActivity.deleteUser(selectedUser)
            mainActivity.setLogoutMsg("Logged in user `${selectedUser.username}` was deleted")

            dismiss()
            val action =
                ManageUsersListerFragmentDirections.actionUsersListerFragmentToHomeFragment()
            findNavController().navigate(action)
        }
        return rootView
    }
}