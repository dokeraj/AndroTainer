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
import kotlinx.android.synthetic.main.fragment_delete_current_user_dialog.view.tv_diag_explanation
import kotlinx.android.synthetic.main.fragment_hidden_features_dialog.view.*

class ShowHiddenFeaturesDiag() : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val rootView: View =
            inflater.inflate(R.layout.fragment_hidden_features_dialog, container, false)

        val markwon = Markwon.builder(requireContext()).build()
        markwon.setMarkdown(rootView.tv_diag_explanation, getString(R.string.hiddenExplanations))

        rootView.clCancel.setOnClickListener {
            dismiss()
        }

        return rootView
    }
}