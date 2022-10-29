package com.dokeraj.androtainer.viewmodels

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.dokeraj.androtainer.models.Kontainer
import com.dokeraj.androtainer.repositories.DockerListerRepo
import com.dokeraj.androtainer.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class HomeFragmentViewModel
@ViewModelInject constructor(
    private val dockerListerRepo: DockerListerRepo,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _dataState: MutableLiveData<DataState<List<Kontainer>>> = MutableLiveData()

    val dataState: LiveData<DataState<List<Kontainer>>>
        get() = _dataState

    @ExperimentalCoroutinesApi
    fun setStateEvent(homeMainStateEvent: HomeMainStateEvent) {
        viewModelScope.launch {
            when (homeMainStateEvent) {
                is HomeMainStateEvent.GetosKontejneri ->
                    dockerListerRepo.getDocContainers(homeMainStateEvent.jwt,
                        homeMainStateEvent.url,
                        homeMainStateEvent.isUsingApiKey)
                        .onEach { dataState ->
                            _dataState.value = dataState
                        }.launchIn(viewModelScope)
            }
        }
    }
}

sealed class HomeMainStateEvent {
    data class GetosKontejneri(val jwt: String?, val url: String, val isUsingApiKey: Boolean) :
        HomeMainStateEvent()
}