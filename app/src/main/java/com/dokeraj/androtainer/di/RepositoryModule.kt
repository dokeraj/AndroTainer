package com.dokeraj.androtainer.di

import com.dokeraj.androtainer.Interfaces.KontainerRetrofit
import com.dokeraj.androtainer.models.retrofit.NetworkMapper
import com.dokeraj.androtainer.repositories.DockerListerRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

    @Singleton
    @Provides
    fun provideDockerListerRepository(
        kontainerRetrofit: KontainerRetrofit,
        networkMapper: NetworkMapper,
    ): DockerListerRepo {
        return DockerListerRepo(kontainerRetrofit, networkMapper)
    }
}