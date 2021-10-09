package com.dokeraj.androtainer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.util.Linkify
import android.view.View
import androidx.activity.addCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dokeraj.androtainer.adapter.DockerContainerAdapter
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.models.*
import com.dokeraj.androtainer.util.DataState
import com.dokeraj.androtainer.viewmodels.DockerListerViewModel
import com.dokeraj.androtainer.viewmodels.MainStateEvent
import dagger.hilt.android.AndroidEntryPoint
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import io.noties.markwon.core.MarkwonTheme
import io.noties.markwon.linkify.LinkifyPlugin
import kotlinx.android.synthetic.main.drawer_lister_header.*
import kotlinx.android.synthetic.main.fragment_docker_lister.*
import kotlinx.coroutines.ExperimentalCoroutinesApi

@AndroidEntryPoint
class DockerListerFragment : Fragment(R.layout.fragment_docker_lister) {
    private val args: DockerListerFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /** how to instantiate a viewModel object*/
        val model: DockerListerViewModel =
            ViewModelProvider(requireActivity()).get(DockerListerViewModel::class.java)

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

        if (globActivity.getIsLoginToDockerLister()) {
            globActivity.setIsLoginToDockerLister(false)
            model.setStateEvent(MainStateEvent.InitializeView(args.dContainers.containers))
        }

        // just give an empty list of containers when initializing the recyclerAdapter
        // we will fill the adapter when the modelview is initialized
        val containers: List<Kontainer> = listOf()

        val recyclerAdapter =
            DockerContainerAdapter(containers,
                globalVars.currentUser!!.serverUrl,
                globalVars.currentUser!!.jwt!!,
                requireContext(), this, model)
        recycler_view.adapter = recyclerAdapter
        recycler_view.layoutManager = LinearLayoutManager(activity)
        recycler_view.setHasFixedSize(true)

        btnLogout.setOnClickListener {
            logout(globActivity)
        }

        btnAbout.setOnClickListener {
            if (tvAboutInfo.visibility == View.VISIBLE) {
                tvAboutInfo.visibility = View.INVISIBLE
                btnDonate.visibility = View.INVISIBLE
            }
            else {
                tvAboutInfo.visibility = View.VISIBLE
                btnDonate.visibility = View.VISIBLE
            }
        }

        btnDonate.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://donate.dokeraj.cc"))
            startActivity(i)
        }

        btnManageUsers.setOnClickListener {
            val action =
                DockerListerFragmentDirections.actionDockerListerFragmentToUsersListerFragment()
            findNavController().navigate(action)
        }

        swiperLayout.setOnRefreshListener {
            callSwiperLogic(model, globActivity, globalVars, recyclerAdapter)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            // hijack the back button press and don't allow going back to login page (only close the drawer)
            drawerLister.close()
        }

        subscribeObservers(model, recyclerAdapter, globActivity)
    }

    private fun subscribeObservers(
        dataViewModel: DockerListerViewModel,
        recyclerAdapter: DockerContainerAdapter,
        mainActivity: MainActiviy,
    ) {
        dataViewModel.dataState.observe(viewLifecycleOwner, { ds ->
            when (ds) {
                is DataState.Success<List<Kontainer>> -> {
                    recyclerAdapter.setItems(ds.data)
                    recyclerAdapter.notifyDataSetChanged()
                    swiperLayout.isRefreshing = false
                }
                is DataState.Error -> {
                    swiperLayout.isRefreshing = false
                    logout(mainActivity, "Issue with Portainer! Please login again.")
                }
                is DataState.Loading -> {
                    swiperLayout.isRefreshing = true
                }
                /** below these is the logic for handling the idividual cards*/
                is DataState.CardLoading -> {
                    recyclerAdapter.setItems(ds.data)
                    recyclerAdapter.notifyItemChanged(ds.itemIndex)
                }
                is DataState.CardSuccess -> {
                    swiperLayout.isRefreshing = false
                    recyclerAdapter.setItems(ds.data)
                    recyclerAdapter.notifyItemChanged(ds.itemIndex)
                }
                is DataState.CardError -> {
                    recyclerAdapter.setItems(ds.data)
                    recyclerAdapter.notifyItemChanged(ds.itemIndex)
                }
            }
        })
    }

    @ExperimentalCoroutinesApi
    private fun callGetContainers(dataViewModel: DockerListerViewModel, url: String, jwt: String) {
        val fullUrl =
            getString(R.string.getDockerContainers).replace("{baseUrl}", url.removeSuffix("/"))

        dataViewModel.setStateEvent(MainStateEvent.GetKontejneri(jwt = jwt, url = fullUrl))
    }

    @ExperimentalCoroutinesApi
    private fun callSwiperLogic(
        dataViewModel: DockerListerViewModel,
        globActivity: MainActiviy,
        globalVars: GlobalApp,
        recyclerAdapter: DockerContainerAdapter,
    ) {
        if (globActivity.isJwtValid()) {
            // don't refresh if there are any items that are transitioning between states
            if (recyclerAdapter.areItemsInTransitioningState())
                swiperLayout.isRefreshing = false
            else {
                callGetContainers(dataViewModel,
                    globalVars.currentUser!!.serverUrl,
                    globalVars.currentUser!!.jwt!!)
            }
        } else {
            logout(globActivity, "Session has expired! Please log in again.")
        }
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

    private fun logout(mainActivity: MainActiviy, logoutMsg: String? = null) {
        mainActivity.invalidateJwt()
        mainActivity.setLogoutMsg(logoutMsg)

        val action =
            DockerListerFragmentDirections.actionDockerListerFragmentToHomeFragment()
        findNavController().navigate(action)
    }

}