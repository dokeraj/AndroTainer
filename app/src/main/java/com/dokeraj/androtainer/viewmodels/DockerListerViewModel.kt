package com.dokeraj.androtainer.viewmodels

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.dokeraj.androtainer.models.*
import com.dokeraj.androtainer.repositories.DockerListerRepo
import com.dokeraj.androtainer.util.DataState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.lang.Exception

class DockerListerViewModel
@ViewModelInject constructor(
    private val dockerListerRepo: DockerListerRepo,
    @Assisted private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _dataState: MutableLiveData<DataState<List<Kontainer>>> = MutableLiveData()
    private var currentList: List<Kontainer> = listOf()

    val dataState: LiveData<DataState<List<Kontainer>>>
        get() = _dataState

    @ExperimentalCoroutinesApi
    fun setStateEvent(mainStateEvent: MainStateEvent) {
        viewModelScope.launch {
            when (mainStateEvent) {
                is MainStateEvent.GetosKontejneri ->
                    dockerListerRepo.getDokeri(mainStateEvent.jwt, mainStateEvent.url)
                        .onEach { dlDataState ->

                            when (dlDataState) {
                                is DataState.Success -> {
                                    currentList = dlDataState.data

                                    /** result back to View */
                                    _dataState.value = dlDataState
                                }
                                is DataState.Error -> {
                                    /** result back to View */
                                    _dataState.value = dlDataState
                                }
                            }


                        }.launchIn(viewModelScope)


                is MainStateEvent.StartStopKontejneri -> {
                    dockerListerRepo.startStopDokerContainer(mainStateEvent.jwt,
                        mainStateEvent.url, mainStateEvent.currentItem)
                        .onEach { ssDataState ->

                            println("BURN FOR ME i dataSostojba e = ${ssDataState}")

                            when (ssDataState) {
                                is DataState.CardLoading -> {

                                    println("U LOADING ind: ${mainStateEvent.currentItem}, ${mainStateEvent.containerActionType}")


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
                                    println("U successss ind: ${mainStateEvent.currentItem}, ${mainStateEvent.containerActionType}")


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

                                    println("U >ERO< ind: ${mainStateEvent.currentItem}, ${mainStateEvent.containerActionType}")

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
                                else -> {
                                    println("VLEzE U elsaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                                }
                            }

                        }.launchIn(viewModelScope)
                }

                is MainStateEvent.InitializeView -> {
                    println("---------------- smeni _datastate.value u inicijalizirano")
                    currentList = mainStateEvent.lista
                    _dataState.value = DataState.Success(mainStateEvent.lista)
                }

                is MainStateEvent.DeleteContaier -> {
                    dockerListerRepo.deleteContainer(mainStateEvent.jwt,
                        mainStateEvent.url,
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
                            }
                        }.launchIn(viewModelScope)
                }

                is MainStateEvent.SetNone -> {
                    println("KOJ E SEGA BE NONE TAKA LI!>!>!>!>")
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
    data class GetosKontejneri(val jwt: String?, val url: String) : MainStateEvent()
    data class StartStopKontejneri(
        val jwt: String?,
        val url: String,
        val currentItem: Int,
        val containerActionType: ContainerActionType,
    ) : MainStateEvent()

    data class InitializeView(val lista: List<Kontainer>) : MainStateEvent()

    data class DeleteContaier(val jwt: String?, val url: String, val selectedItem: Kontainer) :
        MainStateEvent()

    object SetNone : MainStateEvent()
    object SetSuccess : MainStateEvent()

}