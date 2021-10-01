package com.dokeraj.androtainer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.dokeraj.androtainer.viewmodels.DockerListerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DockerLogging: Fragment(R.layout.fragment_logging) {

    private val args: DockerLoggingArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val contId = args.containerId

        println("ADASDSADASD e: ${contId}")

        /** how to instantiate a viewModel object*/
        //val model = ViewModelProvider(requireActivity()).get(DockerListerViewModel::class.java)
    }


}