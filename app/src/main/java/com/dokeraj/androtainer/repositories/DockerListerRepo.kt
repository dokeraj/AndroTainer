package com.dokeraj.androtainer.repositories

import com.dokeraj.androtainer.interfaces.KontainerRetrofit
import com.dokeraj.androtainer.models.Kontainer
import com.dokeraj.androtainer.models.retrofit.NetworkMapper
import com.dokeraj.androtainer.models.retrofit.PContainersResponse
import com.dokeraj.androtainer.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DockerListerRepo constructor(
    private val kontainerRetrofit: KontainerRetrofit,
    private val networkMapper: NetworkMapper,
) {
    suspend fun getDocContainers(
        jwt: String?,
        url: String,
        isUsingApiKey: Boolean,
    ): Flow<DataState<List<Kontainer>>> = flow {
        emit(DataState.Loading)
        try {
            val networkKontainers: PContainersResponse = if (!isUsingApiKey)
                kontainerRetrofit.listDockerContainers(jwt, url, 1)
            else
                kontainerRetrofit.listDockerContainersApiKey(jwt, url, 1)

            val kontainers: List<Kontainer> =
                networkMapper.mapFromEntityList(networkKontainers).sortedBy { it.name }
            emit(DataState.Success(kontainers))
        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun startStopDokerContainer(
        jwt: String?,
        url: String,
        isUsingApiKey: Boolean,
        currentItemIndex: Int,
    ): Flow<DataState<List<Kontainer>>> = flow {
        emit(DataState.CardLoading(listOf<Kontainer>(), currentItemIndex))
        try {
            val so = if (!isUsingApiKey) kontainerRetrofit.startStopContainer(jwt,
                url) else kontainerRetrofit.startStopContainerApiKey(jwt, url)
            if (so.code() != 204) {
                emit(DataState.CardError(listOf<Kontainer>(), currentItemIndex))
            } else
                emit(DataState.CardSuccess(listOf<Kontainer>(), currentItemIndex))
        } catch (e: Exception) {
            emit(DataState.CardError(listOf<Kontainer>(), currentItemIndex))
        }
    }

    suspend fun restartContainer(
        jwt: String?,
        url: String,
        isUsingApiKey: Boolean,
        currentItemIndex: Int,
    ): Flow<DataState<List<Kontainer>>> = flow {
        emit(DataState.CardLoading(listOf<Kontainer>(), currentItemIndex))
        try {
            val so = if (!isUsingApiKey) kontainerRetrofit.restartContainer(jwt,
                url) else kontainerRetrofit.restartContainerApiKey(jwt, url)
            if (so.code() != 204) {
                emit(DataState.CardError(listOf<Kontainer>(), currentItemIndex))
            } else
                emit(DataState.CardSuccess(listOf<Kontainer>(), currentItemIndex))
        } catch (e: Exception) {
            emit(DataState.CardError(listOf<Kontainer>(), currentItemIndex))
        }
    }

    suspend fun deleteContainer(
        jwt: String?,
        url: String,
        isUsingApiKey: Boolean,
        selectedItem: Kontainer,
    ): Flow<DataState<List<Kontainer>>> = flow {
        emit(DataState.DeleteLoading(listOf<Kontainer>(), selectedItem))
        try {
            val res = if (!isUsingApiKey) kontainerRetrofit.deleteDockerContainer(jwt,
                url,
                true,
                1) else kontainerRetrofit.deleteDockerContainerApiKey(jwt, url, true, 1)
            if (res.code() != 204) {
                emit(DataState.Error(java.lang.Exception("cannot delete container!")))
            } else
                emit(DataState.DeleteSuccess(listOf<Kontainer>(), selectedItem))
        } catch (e: Exception) {
            emit(DataState.Error(java.lang.Exception("cannot delete container!")))
        }
    }

}