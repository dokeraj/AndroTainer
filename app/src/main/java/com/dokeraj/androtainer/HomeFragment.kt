package com.dokeraj.androtainer

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.util.Patterns
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dokeraj.androtainer.Interfaces.ApiInterface
import com.dokeraj.androtainer.adapter.UsersLoginAdapter
import com.dokeraj.androtainer.buttons.BtnLogin
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.models.*
import com.dokeraj.androtainer.models.retrofit.*
import com.dokeraj.androtainer.network.RetrofitInstance
import com.dokeraj.androtainer.util.DataState
import com.dokeraj.androtainer.viewmodels.HomeFragmentViewModel
import com.dokeraj.androtainer.viewmodels.HomeMainStateEvent
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import java.time.ZoneOffset
import java.time.ZonedDateTime
import kotlin.math.abs

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {
    var disableDrawerSwipe: Boolean = false

    /** how to instantiate a viewModel object*/
    private val model: HomeFragmentViewModel by viewModels()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO:: REFACTOR the names of drawable icons to start with "ic_"

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.dis4)

        val detector = GestureDetectorCompat(requireContext(), UsersGestureListener())

        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars = (globActivity.application as GlobalApp)

        etUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {

                when (Patterns.WEB_URL.matcher(etUrl.text.toString()).matches()) {
                    true -> {
                        etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.web_link_logo,
                            0,
                            0,
                            0)
                        val background = etUrl.background
                        background.mutate()
                        background.colorFilter =
                            PorterDuffColorFilter(ContextCompat.getColor(requireContext(),
                                R.color.blue_main),
                                PorterDuff.Mode.SRC_ATOP)
                        etUrl.background = background
                    }
                    else -> {
                        etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.warning_logo,
                            0,
                            0,
                            0)
                        val background = etUrl.background
                        background.mutate()
                        background.colorFilter =
                            PorterDuffColorFilter(ContextCompat.getColor(requireContext(),
                                R.color.orange_warning),
                                PorterDuff.Mode.SRC_ATOP)
                        etUrl.background = background
                    }
                }
            }
        }

        val btnLoginState = BtnLogin(requireContext(), lgnBtn)

        if (globActivity.getLogoutMsg() != null) {
            globActivity.showGenericSnack(requireContext(),
                requireView(),
                globActivity.getLogoutMsg()!!,
                R.color.white,
                R.color.orange_warning)
            globActivity.setLogoutMsg(null)
        }

        globalVars.currentUser?.serverUrl?.let { etUrl.setText(it) }
        globalVars.currentUser?.username?.let { etUser.setText(it) }
        globalVars.currentUser?.pwd?.let { etPass.setText(it) }


        if (globActivity.hasJwt() && globActivity.isJwtValid()) {
            btnLoginState.changeBtnState(false)
            callGetContainers(globalVars.currentUser!!.serverUrl, globalVars.currentUser!!.jwt!!)
        } else if (globActivity.hasJwt() && !globActivity.isJwtValid()) {
            btnLoginState.changeBtnState(false)
            authenticate(etUrl.text.toString(),
                etUser.text.toString(),
                etPass.text.toString(),
                globActivity, btnLoginState)
        }

        view.setOnTouchListener { _, event ->
            detector.onTouchEvent(event)
            true
        }

        // on button back pressed - close the users drawer
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            users_lister.close()
        }

        // load the user credentials into the drawer recyclerview
        val savedUsers: List<Credential> = globalVars.credentials.map { (_, v) -> v }

        if (savedUsers.isNotEmpty()) {
            tvUsersNoContent.visibility = View.GONE
            val recyclerAdapter =
                UsersLoginAdapter(savedUsers, users_lister, view, globActivity, requireContext())
            rv_login_users.adapter = recyclerAdapter
            rv_login_users.layoutManager = LinearLayoutManager(activity)
            rv_login_users.setHasFixedSize(true)
        } else {
            rv_login_users.visibility = View.GONE
            tvUsersNoContent.visibility = View.VISIBLE
        }


        lgnBtn.setOnClickListener {
            disableDrawerSwipe = true
            btnLoginState.changeBtnState(false)
            if (Patterns.WEB_URL.matcher(etUrl.text.toString()).matches()) {
                authenticate(etUrl.text.toString(),
                    etUser.text.toString(),
                    etPass.text.toString(), globActivity, btnLoginState)
            } else {
                btnLoginState.changeBtnState(true)
                globActivity.showGenericSnack(requireContext(),
                    requireView(),
                    "Invalid URL!",
                    R.color.white,
                    R.color.orange_warning)
            }
        }

        subscribeObservers(btnLoginState, globActivity)
    }

    private fun subscribeObservers(btnLoginState: BtnLogin, mainActivity: MainActiviy) {
        model.dataState.observe(viewLifecycleOwner, { ds ->
            when (ds) {
                is DataState.Success<List<Kontainer>> -> {
                    println("HOMEFRAGMETN OBSERVER DATA success: ${ds.data}")

                    mainActivity.setIsLoginToDockerLister(true)
                    val action =
                        HomeFragmentDirections.actionHomeFragmentToDockerListerFragment(
                            Kontainers(ds.data))
                    findNavController().navigate(action)

                }
                is DataState.Error -> {
                    disableDrawerSwipe = false
                    btnLoginState.changeBtnState(true)
                    mainActivity.showGenericSnack(requireContext(),
                        requireView(),
                        "Server not permitting communication! Check URL.",
                        R.color.red,
                        R.color.white)

                    println("HOME exception bre: ${ds.exception}")

                }
                is DataState.Loading -> {
                    println("LOADING MOREEEEEEE!!")
                    disableDrawerSwipe = true
                    btnLoginState.changeBtnState(false)
                }
                else -> {}
            }
        })
    }

    private fun authenticate(
        baseUrl: String,
        usr: String,
        pwd: String,
        mainActiviy: MainActiviy,
        btnLoginState: BtnLogin,
    ) {
        val cred = UserCredentials(usr, pwd)

        val fullPath =
            getString(R.string.authenticate).replace("{baseUrl}", baseUrl.removeSuffix("/"))
        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java)
        api.loginRequest(cred, fullPath)
            .enqueue(object : retrofit2.Callback<Jwt?> {
                override fun onResponse(
                    call: retrofit2.Call<Jwt?>,
                    response: retrofit2.Response<Jwt?>,
                ) {
                    disableDrawerSwipe = false
                    val jwtResponse: String? = response.body()?.jwt
                    showResponseSnack(response.code().toString(), btnLoginState)

                    jwtResponse?.let {
                        val jwtValidUntil: Long =
                            ZonedDateTime.now(ZoneOffset.UTC).plusHours(7).plusMinutes(59)
                                .toInstant()
                                .toEpochMilli()

                        val globActivity: MainActiviy = (activity as MainActiviy?)!!
                        globActivity.setAllMasterVals(baseUrl, usr, pwd, it, jwtValidUntil)

                        //getPortainerContainers(url, it, mainActiviy, btnLoginState)
                        callGetContainers(baseUrl, it)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Jwt?>, t: Throwable) {
                    disableDrawerSwipe = false
                    btnLoginState.changeBtnState(true)
                    mainActiviy.showGenericSnack(requireContext(),
                        requireView(),
                        "Server not permitting communication! Check URL.",
                        R.color.red,
                        R.color.white)
                }

            })
    }

    private fun callGetContainers(url: String, jwt: String) {
        val fullUrl =
            getString(R.string.getDockerContainers).replace("{baseUrl}", url.removeSuffix("/"))

        model.setStateEvent(HomeMainStateEvent.GetosKontejneri(jwt = jwt, url = fullUrl))
    }

    fun showResponseSnack(responseStatus: String, btnLoginState: BtnLogin) {
        data class SnackStyle(val text: String, val textColor: Int, val bckColor: Int)
        // colors
        val cBlueMain = context?.let { ContextCompat.getColor(it, R.color.blue_main) }
        val cDGray = context?.let { ContextCompat.getColor(it, R.color.dis4) }
        val cRed = context?.let { ContextCompat.getColor(it, R.color.red) }
        val cWhite = context?.let { ContextCompat.getColor(it, R.color.white) }
        val cOrange = context?.let { ContextCompat.getColor(it, R.color.orange_warning) }

        val sbStyle: SnackStyle = when (responseStatus) {
            "200" -> SnackStyle("", cWhite!!, cRed!!)
            "502", "404" -> {
                btnLoginState.changeBtnState(true)
                SnackStyle("Wrong URL or service is down", cWhite!!, cRed!!)
            }
            "422" -> {
                btnLoginState.changeBtnState(true)
                SnackStyle("Invalid Credentials", cWhite!!, cOrange!!)
            }
            else -> {
                btnLoginState.changeBtnState(true)
                SnackStyle("Server response: Unknown error", cBlueMain!!, cDGray!!)
            }
        }

        val v: View? = activity?.findViewById(android.R.id.content)
        val snackbar = Snackbar.make(
            v!!,
            sbStyle.text,
            Snackbar.LENGTH_SHORT
        )

        val snackbarView: View = snackbar.view
        val snackbarTextId: Int = R.id.snackbar_text

        val textView = snackbarView.findViewById<View>(snackbarTextId) as TextView
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        textView.setTextColor(sbStyle.textColor)

        snackbarView.setBackgroundColor(sbStyle.bckColor)

        if (responseStatus != "200")
            snackbar.show()
    }

    /*private fun getPortainerContainers(
        url: String,
        jwt: String,
        mainActiviy: MainActiviy,
        btnLoginState: BtnLogin,
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
                    disableDrawerSwipe = false
                    val pcResponse: PContainersResponse? = response.body()

                    pcResponse?.let {
                        // remap PContainerResponse to List of PContainer
                        val pcs: List<PContainerResponse> = PContainerHelper.toListPContainer(it)

                        // go to the ListerFragment and transfer all found docker files
                        val transferContainer: PContainers = PContainers(pcs)
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToDockerListerFragment(
                                transferContainer)
                        findNavController().navigate(action)
                    }
                }

                override fun onFailure(call: Call<PContainersResponse?>, t: Throwable) {
                    disableDrawerSwipe = false
                    btnLoginState.changeBtnState(false)
                    mainActiviy.showGenericSnack(requireContext(),
                        requireView(),
                        "Server not permitting communication! Check URL.",
                        R.color.red,
                        R.color.white)
                }
            })
    }*/

    inner class UsersGestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            downEvent: MotionEvent?,
            moveEvent: MotionEvent?,
            velocityX: Float,
            velocityY: Float,
        ): Boolean {
            val diffx = moveEvent?.x?.minus(downEvent!!.x) ?: 0.0F
            val diffy = moveEvent?.y?.minus(downEvent!!.y) ?: 0.0F

            return if (abs(diffx) > abs(diffy)) {
                // this is a left or right swipe
                if (abs(diffx) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    // this is to differentiate between accidental and real swipe
                    if (diffx > 0) {
                        // this is a right swipe (from left to right) - open the drawer
                        openDrawer()
                        true
                    } else if (diffx < 0) {
                        // this is a left swipe - close the drawer
                        users_lister.close()
                        true
                    } else
                        super.onFling(downEvent, moveEvent, velocityX, velocityY)
                } else
                    super.onFling(downEvent, moveEvent, velocityX, velocityY)
            } else
                super.onFling(downEvent, moveEvent, velocityX, velocityY)
        }
    }

    private fun openDrawer() {
        if (!disableDrawerSwipe)
            users_lister.openDrawer(Gravity.LEFT)
    }
}