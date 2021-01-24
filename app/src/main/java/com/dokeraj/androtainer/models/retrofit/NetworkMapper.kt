package com.dokeraj.androtainer.models.retrofit

import com.dokeraj.androtainer.models.ContainerStateType
import com.dokeraj.androtainer.models.HostConfig
import com.dokeraj.androtainer.models.Kontainer
import com.dokeraj.androtainer.models.MaintainerInfo
import com.dokeraj.androtainer.models.Mount
import com.dokeraj.androtainer.models.Port
import com.dokeraj.androtainer.util.EntityMapper
import javax.inject.Inject

class NetworkMapper @Inject constructor() : EntityMapper<PContainerResponse, Kontainer> {
    override fun mapFromEntity(entity: PContainerResponse): Kontainer {
        val state: ContainerStateType = try {
            ContainerStateType.valueOf(entity.state.toUpperCase())
        } catch (e: IllegalArgumentException) {
            ContainerStateType.ERRORED
        }

        val maintainerInfo: MaintainerInfo =
            MaintainerInfo(maintainer = entity.maintainerInfo.maintainer,
                url = entity.maintainerInfo.url)

        val hostConfig: HostConfig = HostConfig(networkMode = entity.hostConfig.networkMode)

        val mounts: List<Mount> = entity.mounts.map { mnt ->
            Mount(source = mnt.source, destination = mnt.destination, type = mnt.type)
        }

        val ports: List<Port> = entity.ports.map { pt ->
            Port(privatePort = pt.privatePort, publicPort = pt.publicPort, type = pt.type)
        }

        return Kontainer(
            id = entity.id,
            name = entity.names[0].drop(1).trim().capitalize(),
            status = entity.status,
            state = state,
            created = entity.created,
            pulledImage = entity.pulledImage,
            maintainerInfo = maintainerInfo,
            hostConfig = hostConfig,
            mounts = mounts,
            ports = ports
        )
    }

    override fun mapToEntity(domainModel: Kontainer): PContainerResponse {

        val maintainerInfo: com.dokeraj.androtainer.models.retrofit.MaintainerInfo =
            com.dokeraj.androtainer.models.retrofit.MaintainerInfo(maintainer = domainModel.maintainerInfo.maintainer,
                url = domainModel.maintainerInfo.url)

        val hostConfig: com.dokeraj.androtainer.models.retrofit.HostConfig =
            com.dokeraj.androtainer.models.retrofit.HostConfig(networkMode = domainModel.hostConfig.networkMode)

        val mount: List<com.dokeraj.androtainer.models.retrofit.Mount> =
            domainModel.mounts.map { mnt ->
                com.dokeraj.androtainer.models.retrofit.Mount(source = mnt.source,
                    destination = mnt.destination,
                    type = mnt.type)
            }

        val ports: List<com.dokeraj.androtainer.models.retrofit.Port> =
            domainModel.ports.map { pt ->
                com.dokeraj.androtainer.models.retrofit.Port(privatePort = pt.privatePort,
                    publicPort = pt.publicPort,
                    type = pt.type)
            }

        return PContainerResponse(
            id = domainModel.id,
            names = listOf(domainModel.name),
            status = domainModel.status,
            state = domainModel.state.name,
            created = domainModel.created,
            pulledImage = domainModel.pulledImage,
            maintainerInfo = maintainerInfo,
            hostConfig = hostConfig,
            mounts = mount,
            ports = ports
        )
    }

    fun mapFromEntityList(entities: PContainersResponse): List<Kontainer> {
        return entities.map {
            mapFromEntity(it)
        }
    }
}