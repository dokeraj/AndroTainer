package com.dokeraj.androtainer

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.models.Credential
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import kotlinx.android.synthetic.main.fragment_docke_container_details.*

class DockerContainerDetailsFragment : Fragment(R.layout.fragment_users_lister) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        tbContainerDetails.navigationIcon =
            ContextCompat.getDrawable(requireActivity(), R.drawable.backlogo)

        // todo:: add the docker container name in the title
        tvContainerDetailsTitle.text = "BLA BLA"

        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars: GlobalApp = (globActivity.application as GlobalApp)

        setContainerDetails()

        // todo:: use gathered info from getAllContainers retrofit function and display them here - easy peasy

        // on back pressed set the global var that the swiperRefresh should be turned on
        tbContainerDetails.setNavigationOnClickListener {
            globActivity.setIsBackToDockerLister(true)
            requireActivity().onBackPressed()
        }
    }

    private fun setContainerDetails() {
        val markwon = Markwon.builder(requireContext())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .codeTextColor(ContextCompat.getColor(requireContext(), R.color.dis6))
                }
            })
            .build()

        markwon.setMarkdown(tvContainerDetailsStatus, getString(R.string.users_manage_note))
    }
}