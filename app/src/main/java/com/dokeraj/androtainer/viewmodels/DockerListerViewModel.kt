package com.dokeraj.androtainer.viewmodels

import androidx.lifecycle.*
import com.dokeraj.androtainer.models.ContainerActionType
import com.dokeraj.androtainer.models.ContainerStateType
import com.dokeraj.androtainer.models.Kontainer
import com.dokeraj.androtainer.repositories.DockerListerRepo
import com.dokeraj.androtainer.util.DataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DockerListerViewModel
@Inject constructor(
    private val dockerListerRepo: DockerListerRepo,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _dataState: MutableLiveData<DataState<List<Kontainer>>> = MutableLiveData()
    private var currentList: List<Kontainer> = listOf()

    val dataState: LiveData<DataState<List<Kontainer>>>
        get() = _dataState

    @ExperimentalCoroutinesApi
    fun setStateEvent(mainStateEvent: MainStateEvent) {
        viewModelScope.launch {
            when (mainStateEvent) {
                is MainStateEvent.GetKontejneri ->
                    dockerListerRepo.getDocContainers(mainStateEvent.jwt,
                        mainStateEvent.url,
                        mainStateEvent.isUsingApiKey)
                        .onEach { dlDataState ->
                            when (dlDataState) {
                                is DataState.Success -> {
                                    currentList = dlDataState.data
                                    _dataState.value = dlDataState
                                }
                                is DataState.Error -> {
                                    _dataState.value = dlDataState
                                }
                                is DataState.Loading -> {
                                    _dataState.value = dlDataState
                                }
                                else -> {}
                            }
                        }.launchIn(viewModelScope)


                is MainStateEvent.StartStopKontejneri -> {
                    dockerListerRepo.startStopDokerContainer(mainStateEvent.jwt,
                        mainStateEvent.url,
                        mainStateEvent.isUsingApiKey,
                        mainStateEvent.currentItem)
                        .onEach { ssDataState ->
                            when (ssDataState) {

                                is DataState.CardLoading -> {
                                    /** change the state and status to transitioning */
                                    val modifiedKontainers: List<Kontainer> =
                                        currentList.mapIndexed { ind, itemToChange ->
                                            if (ind == mainStateEvent.currentItem)
                                                itemToChange.copy(status = if (mainStateEvent.containerActionType == ContainerActionType.START) "Starting" else "Exiting",
                                                    state = ContainerStateType.TRANSITIONING)
                                            else
                                                itemToChange
                                        }

                                    currentList = modifiedKontainers
                                    /** result back to View */
                                    _dataState.value = DataState.CardLoading(modifiedKontainers,
                                        ssDataState.itemIndex)
                                }

                                is DataState.CardSuccess -> {
                                    val modifiedKontainers: List<Kontainer> =
                                        currentList.mapIndexed { ind, itemToChange ->
                                            if (ind == mainStateEvent.currentItem)
                                                itemToChange.copy(status = if (mainStateEvent.containerActionType == ContainerActionType.START) "Started just now" else "Exited just now",
                                                    state = if (mainStateEvent.containerActionType == ContainerActionType.START) ContainerStateType.RUNNING else ContainerStateType.EXITED)
                                            else
                                                itemToChange
                                        }

                                    currentList = modifiedKontainers
                                    /** result back to View */
                                    _dataState.value = DataState.CardSuccess(
                                        modifiedKontainers, ssDataState.itemIndex)
                                }
                                is DataState.CardError -> {
                                    val modifiedKontainers: List<Kontainer> =
                                        currentList.mapIndexed { ind, curItem ->
                                            if (ind == mainStateEvent.currentItem)
                                                curItem.copy(status = "Refresh by swiping down",
                                                    state = ContainerStateType.ERRORED)
                                            else
                                                curItem
                                        }

                                    currentList = modifiedKontainers
                                    /** result back to View */
                                    _dataState.value = DataState.CardError(modifiedKontainers,
                                        mainStateEvent.currentItem)
                                }
                                else -> {}
                            }

                        }.launchIn(viewModelScope)
                }

                is MainStateEvent.RestartKontejneri -> {
                    dockerListerRepo.restartContainer(mainStateEvent.jwt,
                        mainStateEvent.url,
                        mainStateEvent.isUsingApiKey,
                        mainStateEvent.currentItem)
                        .onEach { ssDataState ->
                            when (ssDataState) {

                                is DataState.CardLoading -> {
                                    /** change the state and status to transitioning */
                                    val modifiedKontainers: List<Kontainer> =
                                        currentList.mapIndexed { ind, itemToChange ->
                                            if (ind == mainStateEvent.currentItem)
                                                itemToChange.copy(status = "Restarting",
                                                    state = ContainerStateType.RESTARTING)
                                            else
                                                itemToChange
                                        }

                                    currentList = modifiedKontainers
                                    /** result back to View */
                                    _dataState.value = DataState.CardLoading(modifiedKontainers,
                                        ssDataState.itemIndex)
                                }

                                is DataState.CardSuccess -> {
                                    val modifiedKontainers: List<Kontainer> =
                                        currentList.mapIndexed { ind, itemToChange ->
                                            if (ind == mainStateEvent.currentItem)
                                                itemToChange.copy(status = "Restarted just now",
                                                    state = ContainerStateType.RUNNING)
                                            else
                                                itemToChange
                                        }

                                    currentList = modifiedKontainers
                                    /** result back to View */
                                    _dataState.value = DataState.CardSuccess(
                                        modifiedKontainers, ssDataState.itemIndex)
                                }
                                is DataState.CardError -> {
                                    val modifiedKontainers: List<Kontainer> =
                                        currentList.mapIndexed { ind, curItem ->
                                            if (ind == mainStateEvent.currentItem)
                                                curItem.copy(status = "Refresh by swiping down",
                                                    state = ContainerStateType.ERRORED)
                                            else
                                                curItem
                                        }

                                    currentList = modifiedKontainers
                                    /** result back to View */
                                    _dataState.value = DataState.CardError(modifiedKontainers,
                                        mainStateEvent.currentItem)
                                }
                                else -> {}
                            }

                        }.launchIn(viewModelScope)
                }

                is MainStateEvent.InitializeView -> {
                    currentList = mainStateEvent.lista
                    _dataState.value = DataState.Success(mainStateEvent.lista)
                }

                is MainStateEvent.DeleteContaier -> {
                    dockerListerRepo.deleteContainer(mainStateEvent.jwt,
                        mainStateEvent.url,
                        mainStateEvent.isUsingApiKey,
                        mainStateEvent.selectedItem)
                        .onEach { ssState ->
                            when (ssState) {

                                is DataState.DeleteLoading -> {
                                    /** result back to View */
                                    _dataState.value =
                                        DataState.DeleteLoading(currentList, ssState.item)
                                }

                                is DataState.DeleteSuccess -> {
                                    val modifiedKontainers: List<Kontainer> =
                                        currentList.filterNot { i ->
                                            i.id == ssState.item.id
                                        }

                                    currentList = modifiedKontainers
                                    /** result back to View */
                                    _dataState.value = DataState.Success(modifiedKontainers)
                                }
                                is DataState.Error -> {
                                    /** result back to View */
                                    _dataState.value = DataState.Error(ssState.exception)
                                }
                                else -> {}
                            }
                        }.launchIn(viewModelScope)
                }

                is MainStateEvent.SetNone -> {
                    _dataState.value = DataState.None
                }
                is MainStateEvent.SetSuccess -> {
                    _dataState.value = DataState.Success(currentList)
                }
            }
        }
    }
}

sealed class MainStateEvent {
    data class GetKontejneri(val jwt: String?, val url: String, val isUsingApiKey: Boolean) :
        MainStateEvent()

    data class StartStopKontejneri(
        val jwt: String?,
        val url: String,
        val isUsingApiKey: Boolean,
        val currentItem: Int,
        val containerActionType: ContainerActionType,
    ) : MainStateEvent()

    data class RestartKontejneri(
        val jwt: String?,
        val url: String,
        val isUsingApiKey: Boolean,
        val currentItem: Int,
        //val containerActionType: ContainerActionType,
    ) : MainStateEvent()

    data class InitializeView(val lista: List<Kontainer>) : MainStateEvent()

    data class DeleteContaier(
        val jwt: String?,
        val url: String,
        val isUsingApiKey: Boolean,
        val selectedItem: Kontainer,
    ) :
        MainStateEvent()

    object SetNone : MainStateEvent()
    object SetSuccess : MainStateEvent()
}