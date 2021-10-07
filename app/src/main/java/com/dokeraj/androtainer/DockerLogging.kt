package com.dokeraj.androtainer

import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.dokeraj.androtainer.adapter.LoggingAdapter
import com.dokeraj.androtainer.globalvars.GlobalApp
import com.dokeraj.androtainer.interfaces.ApiInterface
import com.dokeraj.androtainer.models.LogItem
import com.dokeraj.androtainer.network.RetrofitBinaryInstance
import com.dokeraj.androtainer.util.LogTimer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_logging.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response


@AndroidEntryPoint
class DockerLogging : Fragment(R.layout.fragment_logging) {

    private val args: DockerLoggingArgs by navArgs()
    private val linesOfLog = listOf(1000, 5000, 100)
    private val autoRefreshIntervals = listOf(3000, 6000, 12000)
    private val eEgg = listOf("Y U Do Dis!?",
        "There is no hidden functionality here!",
        "Stop clicking me!",
        "Aren't you a nosy snowflake :)")

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

        val timer = LogTimer()

        if (chpAutoRefresh.isChecked) {
            timer.startTimer(this, baseUrl, contId, token, globalVars)
        } else {
            getLogFromRetro(baseUrl,
                contId,
                token,
                globalVars.logSettings?.linesCount ?: 1000,
                chpTimestamp.isChecked)
        }

        srlLogging.setOnRefreshListener {
            if (!chpAutoRefresh.isChecked) {
                getLogFromRetro(baseUrl,
                    contId,
                    token,
                    globalVars.logSettings?.linesCount ?: 1000,
                    chpTimestamp.isChecked)
            }
        }

        chpAutoRefresh.setOnClickListener {
            globActivity.setGlobalLoggingSettings(chpAutoRefresh.isChecked,
                chpTimestamp.isChecked,
                null,
                null
            )

            if (chpAutoRefresh.isChecked) {
                srlLogging.isEnabled = false
                timer.startTimer(this, baseUrl, contId, token, globalVars)
            } else {
                srlLogging.isEnabled = true
                timer.cancelTimer()
            }
        }

        chpAutoRefresh.setOnLongClickListener {
            // disable the auto refresh so next time when you turn it on - it will pull the correct auto refresh interval
            chpAutoRefresh.isChecked = false
            srlLogging.isEnabled = true
            timer.cancelTimer()

            val currentAutoRefreshInt: Long = globalVars.logSettings?.autoRefreshInterval ?: 6000L
            val nextArInterval =
                getNextListItem(currentAutoRefreshInt.toInt(), autoRefreshIntervals)
            globActivity.setGlobalLoggingSettings(chpAutoRefresh.isChecked,
                chpTimestamp.isChecked,
                null,
                nextArInterval.toLong()
            )

            globActivity.showGenericSnack(requireContext(),
                requireView(),
                "Auto refresh interval: ${nextArInterval / 1000} seconds",
                R.color.blue_main,
                R.color.dis3)

            true
        }

        chpTimestamp.setOnClickListener {
            globActivity.setGlobalLoggingSettings(chpAutoRefresh.isChecked,
                chpTimestamp.isChecked,
                null,
                null
            )
        }

        chpLinesCount.setOnClickListener {
            val currentLinesCount = globalVars.logSettings?.linesCount ?: 1000
            val nextLineCount = getNextListItem(currentLinesCount, linesOfLog)
            globActivity.setGlobalLoggingSettings(chpAutoRefresh.isChecked,
                chpTimestamp.isChecked,
                nextLineCount,
                null
            )
            chpLinesCount.text = "${nextLineCount} lines"
        }

        tbContainerLogging.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        chpLinesCount.setOnLongClickListener {
            val index = (0 until eEgg.size).random()

            globActivity.showGenericSnack(requireContext(),
                requireView(),
                eEgg[index],
                R.color.teal_200,
                R.color.dis3)
            true
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, true) {
            timer.cancelTimer()
            findNavController().popBackStack()
        }
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

                val logItems: List<LogItem> = lines.map { line ->
                    LogItem(line.replace("\n", ""))
                }

                rvLogging.adapter = LoggingAdapter(logItems)
                rvLogging.layoutManager = LinearLayoutManager(activity)
                rvLogging.setHasFixedSize(true)

                ///////////////////////////////
                ///////////////////////////////

                srlLogging.isRefreshing = false
                if (chpAutoRefresh.isChecked)
                    srlLogging.isEnabled = false


                rvLogging.scrollToPosition(logItems.size - 1);

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

    private fun initChipsState(
        globalVars: GlobalApp,
    ) {
        globalVars.logSettings?.let {
            chpAutoRefresh.isChecked = it.autoRefresh
            chpTimestamp.isChecked = it.timestamp
            chpLinesCount.text = "${it.linesCount} lines"
        }
    }

    private fun getNextListItem(selectedItem: Int, listOfItems: List<Int>): Int {
        val indexOfCurrent = listOfItems.indexOf(selectedItem)
        val indexOfNext = (indexOfCurrent + 1) % listOfItems.size
        return listOfItems[indexOfNext]
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