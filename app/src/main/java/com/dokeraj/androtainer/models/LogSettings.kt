package com.dokeraj.androtainer.models

data class LogSettings(
    val autoRefresh: Boolean,
    val timestamp: Boolean,
    val wrapLines: Boolean,
    val linesCount: Int,
)
