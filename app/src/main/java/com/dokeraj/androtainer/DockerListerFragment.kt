package com.dokeraj.androtainer

import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dokeraj.androtainer.adapter.DockerContainerAdapter
import com.dokeraj.androtainer.models.PContainer
import kotlinx.android.synthetic.main.fragment_docker_lister.*


class DockerListerFragment : Fragment(R.layout.fragment_docker_lister) {
    private val args: DockerListerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //(activity as AppCompatActivity?)!!.setSupportActionBar(toolbarMenu

        val wwqz: MainActiviy = (activity as MainActiviy?)!!


        val hamburgerMenu = ActionBarDrawerToggle(activity,
            drawerMoj,
            toolbarMenu,
            R.string.nav_app_bar_open_drawer_description,
            R.string.navigation_drawer_close)
        val ss = context?.let { ContextCompat.getColor(it, R.color.blue_main) }
        hamburgerMenu.drawerArrowDrawable.color = ss!!
        drawerMoj.addDrawerListener(hamburgerMenu)
        hamburgerMenu.syncState()

        // transfer data from login
        val containers: List<PContainer> = args.dContainers.containers


        val nni = (1..100).map { x ->
            PContainer(x.toString(), x.toString(), "trt", "mrt")
        }

        val allContainers = (containers + nni)

        recycler_view.adapter = DockerContainerAdapter(allContainers)
        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.setHasFixedSize(true)

        /** za vrakjanje nazad na login egkranot
        btnTest.setOnClickListener(View.OnClickListener {
            val action = DockerListerFragmentDirections.actionDockerListerFragmentToHomeFragment()
            findNavController().navigate(action)

        })*/

        /*try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionx = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }*/

        //get version name of app

        btnAbout.setOnClickListener(View.OnClickListener {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val versionx: String = pInfo.versionName

            //tvAboutInfo.text = Html.fromHtml(getString(R.string.welcome_messages, versionx), Html.FROM_HTML_MODE_COMPACT)

            if (tvAboutInfo.visibility == View.VISIBLE)
                tvAboutInfo.visibility = View.INVISIBLE
            else
                tvAboutInfo.visibility = View.VISIBLE

        })

    }


}