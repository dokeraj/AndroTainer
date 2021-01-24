package com.dokeraj.androtainer.util

sealed class StartStopRepoState {
    object Success: StartStopRepoState()
    object Error: StartStopRepoState()
    object Loading: StartStopRepoState()
}