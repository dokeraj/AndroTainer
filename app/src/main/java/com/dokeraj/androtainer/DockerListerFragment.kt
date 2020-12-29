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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dokeraj.androtainer.Interfaces.ApiInterface
import com.dokeraj.androtainer.adapter.DockerContainerAdapter
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.models.PContainer
import com.dokeraj.androtainer.models.ContainerStateType
import com.dokeraj.androtainer.models.PContainersResponse
import com.dokeraj.androtainer.network.RetrofitInstance
import io.noties.markwon.Markwon
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.drawer_header.*
import kotlinx.android.synthetic.main.fragment_docker_lister.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class DockerListerFragment : Fragment(R.layout.fragment_docker_lister) {
    private val args: DockerListerFragmentArgs by navArgs()

    private var kaunter = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.dis2)

        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars: GlobalApp = (globActivity.application as GlobalApp)

        setDrawerInfo(globalVars)


        val hamburgerMenu = ActionBarDrawerToggle(activity,
            drawerMoj,
            toolbarMenu,
            R.string.nav_app_bar_open_drawer_description,
            R.string.navigation_drawer_close)
        val ss = context?.let { ContextCompat.getColor(it, R.color.disText2) }
        hamburgerMenu.drawerArrowDrawable.color = ss!!
        drawerMoj.addDrawerListener(hamburgerMenu)
        hamburgerMenu.syncState()

        // transfer data from login
        val containers: List<PContainer> = args.dContainers.containers


        val nni = (1..100).map { x ->
            PContainer(x.toString(), x.toString(), "trt", ContainerStateType.exited)
        }

        val allContainers = (containers + nni)

        // todo:: make logic to check if the jwt has expired by timestamp - and call the authenticate agian
        //recycler_view.adapter = DockerContainerAdapter(allContainers, globalVars.url, globalVars.jwt, requireContext())

        val recyclerAdapterC =
            DockerContainerAdapter(allContainers, globalVars.url, globalVars.jwt, requireContext())
        recycler_view.adapter = recyclerAdapterC
        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.setHasFixedSize(true)


        btnLogout.setOnClickListener {
            logout(globActivity)
        }


        btnAbout.setOnClickListener(View.OnClickListener {
            if (tvAboutInfo.visibility == View.VISIBLE)
                tvAboutInfo.visibility = View.INVISIBLE
            else
                tvAboutInfo.visibility = View.VISIBLE
        })



        swiperLayout.setOnRefreshListener {
            if (globActivity.isJwtValid()) {
                // don't refresh if there are any items that are transitioning between states
                if (recyclerAdapterC.areItemsInTransitioningState())
                    swiperLayout.isRefreshing = false
                else {
                    getPortainerContainers(globalVars.url!!,
                        globalVars.jwt!!,
                        recyclerAdapterC,
                        swiperLayout,
                        globActivity)
                }
            } else {
                logout(globActivity, "Session has expired! Please log in again.")
                /*globActivity.invalidateJwt()
                globActivity.setLogoutMsg("Session has expired! Please log in again.")
                val action =
                    DockerListerFragmentDirections.actionDockerListerFragmentToHomeFragment()
                findNavController().navigate(action)*/
            }
        }
    }

    private fun getPortainerContainers(
        url: String,
        jwt: String,
        recyclerAdapter: DockerContainerAdapter,
        swiperLayout: SwipeRefreshLayout,
        mainActivity: MainActiviy,
    ) {
        val fullUrl = "${url.removeSuffix("/")}/api/endpoints/1/docker/containers/json"
        val header = "Bearer $jwt"

        println("POVIK DO GET DOKER KONTEJNERI OD SWIPERo!")

        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)
        api.listDockerContainers(header, fullUrl, 1)
            .enqueue(object : Callback<PContainersResponse?> {
                override fun onResponse(
                    call: Call<PContainersResponse?>,
                    response: Response<PContainersResponse?>,
                ) {
                    val pcResponse: PContainersResponse? = response.body()

                    if (pcResponse != null) {
                        println("ODGOVOR OD Swiper retrofit")
                        // remap from retrofit model to regular data class
                        val pcs: List<PContainer> = pcResponse.mapNotNull { pcr ->
                            ContainerStateType.values().firstOrNull { xx -> xx.name == pcr.State }
                                ?.let { cst ->
                                    PContainer(pcr.Id, pcr.Names[0].drop(1).capitalize(),
                                        pcr.Status, cst
                                    )
                                }
                        }

                        recyclerAdapter.setItems(pcs)
                        recyclerAdapter.notifyDataSetChanged()
                        swiperLayout.isRefreshing = false
                    } else {
                        swiperLayout.isRefreshing = false
                        logout(mainActivity,"Issue with Portainer! Please login again.")
                    }
                }

                override fun onFailure(call: Call<PContainersResponse?>, t: Throwable) {
                    swiperLayout.isRefreshing = false
                    logout(mainActivity,"Issue with Portainer! Please login again.")
                }
            })
    }


    private fun setDrawerInfo(globalVars: GlobalApp) {
        // set the name of the logged in user and the server url
        tvLoggedUsername.text = globalVars.user
        tvLoggedUrl.text = globalVars.url

        //get version name of app
        val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        val appVersion: String = pInfo.versionName

        // use Markwon to format the text
        val markwon = Markwon.builder(requireContext())
            .usePlugin(LinkifyPlugin.create(Linkify.EMAIL_ADDRESSES or Linkify.WEB_URLS))
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