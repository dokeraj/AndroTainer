package com.dokeraj.androtainer.models

import com.google.gson.*
import java.lang.reflect.Type

data class DockerEndpoint(val id: Int = -1, val name: String, val url: String)

class DockerEndpointDeserializer: JsonDeserializer<DockerEndpoint> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): DockerEndpoint {
        json as JsonObject

        val id = json.get("id")?.asInt ?: -1
        val name = json.get("name")?.asString ?: "INVALID NAME"
        val url = json.get("url")?.asString ?: "INVALID URL"

        return DockerEndpoint(id,name,url)
    }
}