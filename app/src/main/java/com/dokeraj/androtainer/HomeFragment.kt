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
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dokeraj.androtainer.adapter.UsersLoginAdapter
import com.dokeraj.androtainer.buttons.BtnLogin
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.interfaces.ApiInterface
import com.dokeraj.androtainer.interfaces.ApiInterfaceApiKey
import com.dokeraj.androtainer.models.Credential
import com.dokeraj.androtainer.models.DockerEndpoint
import com.dokeraj.androtainer.models.Kontainer
import com.dokeraj.androtainer.models.Kontainers
import com.dokeraj.androtainer.models.retrofit.DockerEndpointNetworkMapper
import com.dokeraj.androtainer.models.retrofit.Jwt
import com.dokeraj.androtainer.models.retrofit.PEndpointsResponse
import com.dokeraj.androtainer.models.retrofit.UserCredentials
import com.dokeraj.androtainer.network.RetrofitInstance
import com.dokeraj.androtainer.util.DataState
import com.dokeraj.androtainer.viewmodels.HomeFragmentViewModel
import com.dokeraj.androtainer.viewmodels.HomeMainStateEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_logging.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
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

        requireActivity().window.statusBarColor =
            ContextCompat.getColor(requireContext(), R.color.dis4)

        val detector = GestureDetectorCompat(requireContext(), UsersGestureListener())

        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars = (globActivity.application as GlobalApp)

        swUseApiKey.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                lnLayUsrPwd.visibility = View.INVISIBLE
                conApiKey.visibility = View.VISIBLE
            } else {
                lnLayUsrPwd.visibility = View.VISIBLE
                conApiKey.visibility = View.GONE
            }
        }

        etUrl.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {

                when (Patterns.WEB_URL.matcher(etUrl.text.toString())
                    .matches() && (etUrl.text.toString().toLowerCase()
                    .startsWith("http") || etUrl.text.toString().toLowerCase()
                    .startsWith("https"))) {
                    true -> {
                        etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_web_link,
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
                        etUrl.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_warning,
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
        globalVars.currentUser?.isUsingApiKey?.let { useApi ->
            swUseApiKey.isChecked = useApi
            globalVars.currentUser?.jwt?.let { etApiKey.setText(it) }
        }


        if (globActivity.hasJwt() && (globActivity.isJwtValid())) {
            btnLoginState.changeBtnState(false)
            callGetContainers(globalVars.currentUser!!.serverUrl,
                globalVars.currentUser!!.jwt!!,
                globalVars.currentUser!!.currentEndpoint.id, globalVars.currentUser!!.isUsingApiKey)
        } else if (globActivity.hasJwt() && (!globActivity.isJwtValid() && !globActivity.isUserUsingApiKey())) {
            btnLoginState.changeBtnState(false)
            if (globActivity.isUserUsingApiKey()) {
                authenticateApi(etUrl.text.toString(),
                    etApiKey.text.toString(),
                    btnLoginState)
            } else
                authenticate(etUrl.text.toString(),
                    etUser.text.toString(),
                    etPass.text.toString(),
                    btnLoginState)
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
            if (swUseApiKey.isChecked) {
                authenticateApi(etUrl.text.toString(),
                    etApiKey.text.toString(),
                    btnLoginState)
            } else if (Patterns.WEB_URL.matcher(etUrl.text.toString())
                    .matches() && (etUrl.text.toString()
                    .toLowerCase().startsWith("http") || etUrl.text.toString().toLowerCase()
                    .startsWith("https"))
            ) {
                authenticate(etUrl.text.toString(),
                    etUser.text.toString(),
                    etPass.text.toString(), btnLoginState)
            } else {

                val errText = if (!etUrl.text.toString().toLowerCase()
                        .startsWith("http") && !etUrl.text.toString().toLowerCase()
                        .startsWith("https")
                )
                    "The URL must start with http:// or https://"
                else
                    "Invalid URL!"

                btnLoginState.changeBtnState(true)
                globActivity.showGenericSnack(requireContext(),
                    requireView(),
                    errText,
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
                        "Cannot pull docker containers!",
                        R.color.red,
                        R.color.white)
                }
                is DataState.Loading -> {
                    disableDrawerSwipe = true
                    btnLoginState.changeBtnState(false)
                }
            }
        })
    }

    private fun authenticate(
        baseUrl: String,
        usr: String,
        pwd: String,
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
                    if (response.code() == 200) {
                        val jwtResponse: String? = response.body()?.jwt

                        jwtResponse?.let {
                            val jwtValidUntil: Long =
                                ZonedDateTime.now(ZoneOffset.UTC).plusHours(7).plusMinutes(59)
                                    .toInstant()
                                    .toEpochMilli()

                            getEndpointId(baseUrl = baseUrl,
                                usr = usr,
                                pwd = pwd,
                                btnLoginState = btnLoginState,
                                jwt = it,
                                jwtValidUntil = jwtValidUntil,
                                isUsingApiKey = false)
                        }
                    } else {
                        showResponseSnack(response.code().toString(), btnLoginState)
                    }
                }

                override fun onFailure(call: retrofit2.Call<Jwt?>, t: Throwable) {
                    onLoginError(btnLoginState, "Server not permitting communication! ${t.message}")
                }
            })
    }

    private fun authenticateApi(
        baseUrl: String,
        apiKey: String,
        btnLoginState: BtnLogin,
    ) {
        val fullPath =
            getString(R.string.status).replace("{baseUrl}", baseUrl.removeSuffix("/"))
        val api = RetrofitInstance.retrofitInstance!!.create(ApiInterfaceApiKey::class.java)
        api.getStatus(fullPath, apiKey)
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (response.code() == 200) {
                        val usr = apiKey.take(3) + ".." + apiKey.takeLast(3)
                        getEndpointId(baseUrl = baseUrl,
                            usr = usr,
                            pwd = "",
                            btnLoginState = btnLoginState,
                            jwt = apiKey,
                            jwtValidUntil = 0L,
                            isUsingApiKey = true)
                    } else {
                        showResponseSnack(response.code().toString(), btnLoginState)
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    onLoginError(btnLoginState, "Server not permitting communication! ${t.message}")
                }
            })
    }

    private fun getEndpointId(
        baseUrl: String,
        usr: String,
        pwd: String,
        btnLoginState: BtnLogin,
        jwt: String,
        jwtValidUntil: Long,
        isUsingApiKey: Boolean,
    ) {
        fun getDockerEndpoints(response: PEndpointsResponse): Pair<DockerEndpoint?, List<DockerEndpoint>> {
            val dockerEndpoints: List<DockerEndpoint> =
                DockerEndpointNetworkMapper.mapFromRetrofitModel(response)

            val dockerSock: DockerEndpoint? = dockerEndpoints.find {
                it.url == "unix:///var/run/docker.sock"
            }

            val sortedById: List<DockerEndpoint> = dockerEndpoints.sortedBy { it.id }

            return Pair(dockerSock ?: sortedById.getOrNull(0), sortedById)
        }


        val (api, authType) = if (!isUsingApiKey) {
            Pair(RetrofitInstance.retrofitInstance!!.create(ApiInterface::class.java),
                "Bearer ${jwt}")
        } else {
            Pair(RetrofitInstance.retrofitInstance!!.create(ApiInterfaceApiKey::class.java), jwt)
        }

        val fullPath =
            getString(R.string.getEnpointId).replace("{baseUrl}", baseUrl.removeSuffix("/"))


        api.getEnpointId(fullPath, authType, 10, 0)
            .enqueue(object : retrofit2.Callback<PEndpointsResponse> {
                override fun onResponse(
                    call: Call<PEndpointsResponse>,
                    response: Response<PEndpointsResponse>,
                ) {
                    disableDrawerSwipe = false

                    if (response.code() == 200 && response.body() != null) {
                        val dockerEndpoints: Pair<DockerEndpoint?, List<DockerEndpoint>> =
                            getDockerEndpoints(response = response.body()!!)

                        if (dockerEndpoints.first != null) {
                            val globActivity: MainActiviy = (activity as MainActiviy?)!!
                            globActivity.setGlobalCredentials(Credential(serverUrl = baseUrl,
                                username = usr,
                                pwd = pwd,
                                jwt = jwt,
                                jwtValidUntil = jwtValidUntil,
                                currentEndpoint = dockerEndpoints.first!!,
                                listOfEndpoints = dockerEndpoints.second,
                                isUsingApiKey = isUsingApiKey),
                                true)

                            callGetContainers(baseUrl,
                                jwt,
                                dockerEndpoints.first!!.id,
                                isUsingApiKey)
                        } else {
                            onLoginError(btnLoginState,
                                "There are no Portainer endpoints listed!")
                        }
                    } else {
                        onLoginError(btnLoginState,
                            "Cannot get portainer endpoint id! Error code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<PEndpointsResponse>, t: Throwable) {
                    onLoginError(btnLoginState, "Failed to get Portainer endpoint ID!")
                }
            })

    }

    private fun callGetContainers(
        url: String,
        jwt: String,
        endpointId: Int,
        isUsingApiKey: Boolean,
    ) {
        val fullUrl =
            getString(R.string.getDockerContainers).replace("{baseUrl}", url.removeSuffix("/"))
                .replace("{endpointId}", endpointId.toString())

        model.setStateEvent(HomeMainStateEvent.GetosKontejneri(jwt = jwt,
            url = fullUrl,
            isUsingApiKey = isUsingApiKey))
    }

    fun showResponseSnack(responseStatus: String, btnLoginState: BtnLogin) {
        when (responseStatus) {
            "502", "404" -> {
                onLoginError(btnLoginState,
                    "Wrong URL or service is down",
                    R.color.white,
                    R.color.red)
            }
            "422", "401" -> {
                onLoginError(btnLoginState,
                    "Invalid Credentials",
                    R.color.white,
                    R.color.orange_warning)
            }
            else -> {
                onLoginError(btnLoginState,
                    "Server response: Unknown error",
                    R.color.blue_main,
                    R.color.dis4)
            }
        }
    }

    private fun onLoginError(
        btnLoginState: BtnLogin,
        msg: String,
        textColor: Int = R.color.red,
        bckColor: Int = R.color.white,
    ) {
        disableDrawerSwipe = false
        btnLoginState.changeBtnState(true)
        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        globActivity.showGenericSnack(requireContext(),
            requireView(),
            msg,
            textColor,
            bckColor)
    }

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