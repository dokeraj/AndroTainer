package com.dokeraj.androtainer

import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dokeraj.androtainer.Interfaces.ApiInterface
import com.dokeraj.androtainer.adapter.DockerContainerAdapter
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.models.ContainerStateType
import com.dokeraj.androtainer.models.PContainer
import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import com.dokeraj.androtainer.network.RetrofitInstance
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.drawer_lister_header.*
import kotlinx.android.synthetic.main.fragment_docker_lister.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DockerListerFragment : Fragment(R.layout.fragment_docker_lister) {
    private val args: DockerListerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.dis2)

        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars: GlobalApp = (globActivity.application as GlobalApp)

        setDrawerInfo(globalVars)

        val hamburgerMenu = ActionBarDrawerToggle(activity,
            drawerLister,
            toolbarMenu,
            R.string.nav_app_bar_open_drawer_description,
            R.string.navigation_drawer_close)

        hamburgerMenu.drawerArrowDrawable.color =
            ContextCompat.getColor(requireContext(), R.color.disText2)
        drawerLister.addDrawerListener(hamburgerMenu)
        hamburgerMenu.syncState()

        // todo:: when deleting the current user -> display popup and say if you delete the current user you will be logged out - and then log out

        // transfer data from login
        val containers: List<PContainer> = args.dContainers.containers

        val recyclerAdapter =
            DockerContainerAdapter(containers, globalVars.currentUser!!.serverUrl, globalVars.currentUser!!.jwt!!, requireContext())
        recycler_view.adapter = recyclerAdapter
        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.setHasFixedSize(true)

        btnLogout.setOnClickListener {
            logout(globActivity)
        }

        btnAbout.setOnClickListener {
            if (tvAboutInfo.visibility == View.VISIBLE)
                tvAboutInfo.visibility = View.INVISIBLE
            else
                tvAboutInfo.visibility = View.VISIBLE
        }

        swiperLayout.setOnRefreshListener {
            if (globActivity.isJwtValid()) {

                // don't refresh if there are any items that are transitioning between states
                if (recyclerAdapter.areItemsInTransitioningState())
                    swiperLayout.isRefreshing = false
                else {
                    getPortainerContainers(globalVars.currentUser!!.serverUrl,
                        globalVars.currentUser!!.jwt!!,
                        recyclerAdapter,
                        globActivity)
                }
            } else {
                logout(globActivity, "Session has expired! Please log in again.")
            }
        }
    }

    private fun getPortainerContainers(
        url: String,
        jwt: String,
        recyclerAdapter: DockerContainerAdapter,
        mainActivity: MainActiviy,
    ) {
        val fullUrl =
            getString(R.string.getDockerContainers).replace("{baseUrl}", url.removeSuffix("/"))
        val header = "Bearer $jwt"

        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)
        api.listDockerContainers(header, fullUrl, 1)
            .enqueue(object : Callback<PContainersResponse?> {
                override fun onResponse(
                    call: Call<PContainersResponse?>,
                    response: Response<PContainersResponse?>,
                ) {
                    val pcResponse: PContainersResponse? = response.body()

                    if (pcResponse != null) {
                        // remap from retrofit model to regular data class / filter out containers in unknown state
                        val pcs: List<PContainer> = pcResponse.mapNotNull { pcr ->
                            // return null if state of container is not one of the listed in ContainerStateType enum
                            ContainerStateType.values().firstOrNull { cst -> cst.name == pcr.State }
                                ?.let { cst ->
                                    PContainer(pcr.Id, pcr.Names[0].drop(1).trim().capitalize(),
                                        pcr.Status, cst
                                    )
                                }
                        }

                        recyclerAdapter.setItems(pcs)
                        recyclerAdapter.notifyDataSetChanged()
                        swiperLayout.isRefreshing = false
                    } else {
                        swiperLayout.isRefreshing = false
                        logout(mainActivity, "Issue with Portainer! Please login again.")
                    }
                }

                override fun onFailure(call: Call<PContainersResponse?>, t: Throwable) {
                    swiperLayout.isRefreshing = false
                    logout(mainActivity, "Issue with Portainer! Please login again.")
                }
            })
    }


    private fun setDrawerInfo(globalVars: GlobalApp) {
        // set the name of the logged in user and the server url
        tvLoggedUsername.text = globalVars.currentUser!!.username
        tvLoggedUrl.text = globalVars.currentUser!!.serverUrl

        //get version name of app
        val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val appVersion: String = pInfo.versionName

        // use Markwon to format the text
        val markwon = Markwon.builder(requireContext())
            .usePlugin(object : AbstractMarkwonPlugin() {
                override fun configureTheme(builder: MarkwonTheme.Builder) {
                    builder
                        .codeTextColor(ContextCompat.getColor(requireContext(), R.color.blue_main))
                        .linkColor(ContextCompat.getColor(requireContext(), R.color.teal_200))

                }
            }).usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS))
            .build()

        // get the text from the string resources and add the version number
        markwon.setMarkdown(tvAboutInfo, getString(R.string.about_app, appVersion))
    }

    private fun logout(mainActiviy: MainActiviy, logoutMsg: String? = null) {
        mainActiviy.invalidateJwt()
        mainActiviy.setLogoutMsg(logoutMsg)
        val action =
            DockerListerFragmentDirections.actionDockerListerFragmentToHomeFragment()
        findNavController().navigate(action)
    }

}