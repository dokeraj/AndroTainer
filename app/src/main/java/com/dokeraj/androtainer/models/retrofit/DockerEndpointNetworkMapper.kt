package com.dokeraj.androtainer.models.retrofit

import com.dokeraj.androtainer.models.DockerEndpoint
import com.dokeraj.androtainer.util.EntityMapper

object DockerEndpointNetworkMapper : EntityMapper<PortainerEndpoint, DockerEndpoint> {
    override fun mapFromEntity(entity: PortainerEndpoint): DockerEndpoint {
        return DockerEndpoint(id = entity.id, name = entity.name, url = entity.url)
    }

    // this will never be used
    override fun mapToEntity(domainModel: DockerEndpoint): PortainerEndpoint {
        return PortainerEndpoint(id = domainModel.id,
            name = domainModel.name,
            url = domainModel.url,
            type = 0)
    }

    fun mapFromRetrofitModel(entities: PEndpointsResponse): List<DockerEndpoint> {
        return entities.map {
            mapFromEntity(it)
        }
    }
}