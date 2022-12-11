package com.dokeraj.androtainer.util

import com.dokeraj.androtainer.DockerLogging
import com.dokeraj.androtainer.globalvars.GlobalApp
import kotlinx.android.synthetic.main.fragment_logging.*
import kotlinx.coroutines.*

class LogTimer : CoroutineScope by MainScope() {

    private lateinit var job: Job

    private fun startCoroutineTimer(
        delayMillis: Long = 0,
        repeatMillis: Long = 0,
        action: () -> Unit,
    ) = launch {
        delay(delayMillis)
        if (repeatMillis > 0) {
            while (true) {
                action()
                delay(repeatMillis)
            }
        } else {
            action()
        }
    }

    fun startTimer(
        logFrag: DockerLogging,
        baseUrl: String,
        endpointId:Int,
        contId: String,
        token: String,
        globalVars: GlobalApp,
        isUsingApiKey:Boolean
    ) {
        job =
            startCoroutineTimer(delayMillis = 0, repeatMillis = globalVars.logSettings?.autoRefreshInterval ?: 6000L) {
                launch(Dispatchers.Main) {
                    logFrag.getLogFromRetro(
                        baseUrl,
                        contId,
                        token,
                        endpointId,
                        globalVars.logSettings?.linesCount ?: 100,
                        logFrag.chpTimestamp.isChecked,
                        isUsingApiKey
                    )
                }
            }
    }

    fun cancelTimer() {
        if (this::job.isInitialized) {
            job.cancel()
        }
    }
}