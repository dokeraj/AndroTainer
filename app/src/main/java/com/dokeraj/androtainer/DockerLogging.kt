package com.dokeraj.androtainer

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dokeraj.androtainer.adapter.LoggingAdapter
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.interfaces.ApiInterface
import com.dokeraj.androtainer.models.LogItem
import com.dokeraj.androtainer.network.RetrofitBinaryInstance
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_logging.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

@AndroidEntryPoint
class DockerLogging : Fragment(R.layout.fragment_logging) {

    private val args: DockerLoggingArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val globActivity: MainActiviy = (activity as MainActiviy?)!!
        val globalVars: GlobalApp = (globActivity.application as GlobalApp)

        tbContainerLogging.navigationIcon =
            ContextCompat.getDrawable(requireActivity(), R.drawable.ic_back)

        tvContainerLoggingTitle.text = "${args.containerName} Logs"

        val contId = args.containerId
        val baseUrl = globalVars.currentUser!!.serverUrl
        val token: String = globalVars.currentUser!!.jwt!!

        // pull from global var and setup the chips state
        initChipsState(globalVars)

        /** how to instantiate a viewModel object*/
        //val model = ViewModelProvider(requireActivity()).get(DockerListerViewModel::class.java)

        srlLogging.isEnabled = true
        srlLogging.isRefreshing = true

        srlLogging.setOnRefreshListener {
            getLogFromRetro(baseUrl,
                contId,
                token,
                globalVars.logSettings?.linesCount ?: 1000,
                chpTimestamp.isChecked)

            println("SWAJPAAAAAAAAAAAAAAA")
        }

        chpAutoRefresh.setOnClickListener {
            setLogSettings(globActivity, globalVars, null)
            // todo:: call logic to call retro
        }

        chpTimestamp.setOnClickListener {
            setLogSettings(globActivity, globalVars, null)
        }

        chpWrapLines.setOnClickListener {
            //toggleErrorTextView(tvGetLogError)
            setLogSettings(globActivity, globalVars, null)
        }

        chpWrapLines.setOnLongClickListener {
            val currentLinesCount = globalVars.logSettings?.linesCount ?: 1000
            val nextLineCount = getNextLineCount(currentLinesCount)
            setLogSettings(globActivity, globalVars, nextLineCount)

            globActivity.showGenericSnack(requireContext(),
                requireView(),
                "Now displaying the last ${nextLineCount} lines of the log",
                R.color.blue_main,
                R.color.dis3)
            true
        }

        tbContainerLogging.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        getLogFromRetro(baseUrl,
            contId,
            token,
            globalVars.logSettings?.linesCount ?: 1000,
            chpTimestamp.isChecked)
    }

    fun getLogFromRetro(
        baseUrl: String,
        containerId: String,
        jwt: String,
        numOfRows: Int,
        useTimestamp: Boolean,
    ) {
        fun readFromStream(
            body: ResponseBody?,
        ) {
            if (body != null) {

                val oo = String(body.bytes(), Charsets.UTF_8)
                val lines: List<String> = oo.split("\n")
                /*val filtered: List<String> = lines.map { x ->
                  val nonAscii = Regex("[^\\x00-\\x7F]").replace(x, "")
                     val ctrlChars = Regex("[\\p{Cntrl}&&[^\r\n\t]]").replace(nonAscii, "")
                     val nonPrintable = Regex("\\p{C}").replace(ctrlChars, "")
                    //nonPrintable
                    x
                }*/


                //val result = filtered.joinToString("\n")

                ////////////////////////////////
                ////////////////////////////////

                val logItems: List<LogItem> = lines.map{ line ->
                    LogItem(line.replace("\n",""))
                }

                rvLogging.adapter = LoggingAdapter(logItems)
                rvLogging.layoutManager = LinearLayoutManager(activity)
                rvLogging.setHasFixedSize(true)

                ///////////////////////////////
                ///////////////////////////////


                srlLogging.isRefreshing = false

                //tvNoWrap.text = result
                //tvWrapped.text = result

                toggleErrorTextView(
                    false,
                    null)
            } else {
                srlLogging.isRefreshing = false
                toggleErrorTextView(
                    true,
                    "ERROR: Cannot read log data!")
            }
        }

        val fullPath =
            getString(R.string.getLog)
                .replace("{baseUrl}", baseUrl.removeSuffix("/"))
                .replace("{containerId}", containerId)
        val api = RetrofitBinaryInstance.retrofitInstance!!.create(ApiInterface::class.java)

        api.getLog(fullPath, "Bearer ${jwt}", 0, 1, 1, numOfRows, if (useTimestamp) 1 else 0)
            .enqueue(object : retrofit2.Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (response.code() == 200) {
                        readFromStream(response.body())
                    } else {
                        srlLogging.isRefreshing = false
                        toggleErrorTextView(
                            true,
                            "ERROR: Response code: ${response.code()}; ${
                                response.errorBody()?.string()
                            }")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    srlLogging.isRefreshing = false
                    toggleErrorTextView(
                        true,
                        "ERROR failure: ${t.message}")
                }
            })
    }


    private fun setLogSettings(globActivity: MainActiviy, globalVars: GlobalApp, linesCount: Int?) {
        val logLinesCount: Int = linesCount ?: globalVars.logSettings?.let {
            it.linesCount
        } ?: 1000

        globActivity.setGlobalLoggingSettings(chpAutoRefresh.isChecked,
            chpTimestamp.isChecked,
            chpWrapLines.isChecked,
            logLinesCount)
    }

    private fun initChipsState(
        globalVars: GlobalApp,
    ) {
        globalVars.logSettings?.let {
            chpAutoRefresh.isChecked = it.autoRefresh
            chpTimestamp.isChecked = it.timestamp
        }
    }

    private fun getNextLineCount(currentLineCount: Int): Int {
        val predefined = listOf(1000, 5000, 100)

        val indexOfCurrent = predefined.indexOf(currentLineCount)
        val indexOfNext = (indexOfCurrent + 1) % predefined.size
        return predefined[indexOfNext]
    }

    private fun toggleErrorTextView(
        show: Boolean,
        errorMsg: String?,
    ) {
        if (show) {
            errorMsg?.let {
                tvLogError.text = it
            }
            tvLogError.visibility = View.VISIBLE
            rvLogging.visibility = View.GONE
        } else {
            tvLogError.visibility = View.GONE
            rvLogging.visibility = View.VISIBLE
        }
    }
}