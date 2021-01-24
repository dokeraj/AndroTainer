package com.dokeraj.androtainer.repositories

import com.dokeraj.androtainer.Interfaces.KontainerRetrofit
import com.dokeraj.androtainer.models.*
import com.dokeraj.androtainer.models.retrofit.NetworkMapper
import com.dokeraj.androtainer.util.DataState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DockerListerRepo constructor(
    private val kontainerRetrofit: KontainerRetrofit,
    private val networkMapper: NetworkMapper,
) {
    suspend fun getDokeri(jwt: String?, url: String): Flow<DataState<List<Kontainer>>> = flow {
        emit(DataState.Loading)
        try {
            val networkKontainers = kontainerRetrofit.listDockerContainers(jwt, url, 1)
            val kontainers = networkMapper.mapFromEntityList(networkKontainers)
            emit(DataState.Success(kontainers))
        } catch (e: Exception) {
            emit(DataState.Error(e))
        }
    }

    suspend fun startStopDokerContainer(
        jwt: String?,
        url: String,
        currentItemIndex: Int,
    ): Flow<DataState<List<Kontainer>>> = flow {
        emit(DataState.CardLoading(listOf<Kontainer>(), currentItemIndex))
        try {
            println("PRIJE!?!?")
            val so = kontainerRetrofit.startStopContainer(jwt, url)
            println("ZAVRSI?!?!")
            if (so.code() != 204) {
                println("EVE dupla komanda!!")
                emit(DataState.CardError(listOf<Kontainer>(), currentItemIndex))
            } else
                emit(DataState.CardSuccess(listOf<Kontainer>(), currentItemIndex))
        } catch (e: Exception) {
            println("AJDE U GRESKOS?!?!??!?!?!?! ${e.message}")
            emit(DataState.CardError(listOf<Kontainer>(), currentItemIndex))
        }
    }

    suspend fun deleteContainer(
        jwt: String?,
        url: String,
        selectedItem: Kontainer,
    ): Flow<DataState<List<Kontainer>>> = flow {
        emit(DataState.DeleteLoading(listOf<Kontainer>(), selectedItem))
        try {
            val res = kontainerRetrofit.deleteDockerContainer(jwt, url, true, 1)
            if (res.code() != 204) {
                println("e so e greskata? kod: ${res.code()} i greska: ${res.errorBody()} i obdi: ${res.body()}")
                emit(DataState.Error(java.lang.Exception("cannot delete container!")))
            } else
                emit(DataState.DeleteSuccess(listOf<Kontainer>(), selectedItem))
        } catch (e: Exception) {
            println("FATAL ERORR!? : ${e}")
            emit(DataState.Error(java.lang.Exception("cannot delete container!")))
        }
    }

}