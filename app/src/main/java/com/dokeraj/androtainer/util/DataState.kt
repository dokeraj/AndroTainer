package com.dokeraj.androtainer.util

import com.dokeraj.androtainer.models.Kontainer
import java.lang.Exception

sealed class DataState<out R> {
    data class Success<out T>(val data: T): DataState<T>()
    data class Error(val exception: Exception): DataState<Nothing>()
    object Loading: DataState<Nothing>()

    data class CardSuccess<out T>(val data: T, val itemIndex: Int): DataState<T>()
    data class CardLoading<out T>(val data: T, val itemIndex: Int): DataState<T>()
    data class CardError<out T>(val data: T, val itemIndex: Int): DataState<T>()

    data class DeleteSuccess<out T>(val data: T, val item: Kontainer): DataState<T>()
    data class DeleteLoading<out T>(val data: T, val item: Kontainer): DataState<T>()

    object None: DataState<Nothing>()
}