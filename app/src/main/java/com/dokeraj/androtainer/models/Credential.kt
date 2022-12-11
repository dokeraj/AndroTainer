package com.dokeraj.androtainer.models

import com.google.gson.*
import java.lang.reflect.Type

data class Credential(
    val serverUrl: String,
    val username: String,
    val pwd: String,
    val jwt: String? = null, // this is where the API key will be stored if user is logged in with API key
    val jwtValidUntil: Long? = null,
    val lastActivity: Long? = null,
    val currentEndpoint: DockerEndpoint,
    val listOfEndpoints: List<DockerEndpoint>,
    val isUsingApiKey: Boolean, // check against this field to know if you need to call methods that use API key
)

class CredentialDeserializer : JsonDeserializer<Credential> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): Credential {
        json as JsonObject

        val serverUrl: String = json.get("serverUrl").asString
        val username: String = json.get("username").asString
        val pwd: String = json.get("pwd").asString
        val jwt: String? = json.get("jwt")?.asString
        val jwtValidUntil: Long? = json.get("jwtValidUntil")?.asLong
        val lastActivity: Long? = json.get("lastActivity")?.asLong
        val isUsingApi: Boolean = json.get("isUsingApiKey")?.asBoolean ?: false

        val currentEndpoint: DockerEndpoint = json.get("currentEndpoint")?.asJsonObject?.let {
            val curEndpointGson = GsonBuilder().registerTypeAdapter(DockerEndpoint::class.java,
                DockerEndpointDeserializer()).create()
            curEndpointGson.fromJson(it, DockerEndpoint::class.java)
        } ?: DockerEndpoint(id = -1, name = "INVALID ENDPOINT", "INVALID URL")

        val listOfEndpoints: Array<DockerEndpoint> = json.get("listOfEndpoints")?.asJsonArray?.let {
            val curEndpointGson = GsonBuilder().registerTypeAdapter(DockerEndpoint::class.java,
                DockerEndpointDeserializer()).create()
            curEndpointGson.fromJson(it, Array<DockerEndpoint>::class.java)
        } ?: arrayOf<DockerEndpoint>()


        return Credential(
            serverUrl = serverUrl,
            username = username,
            pwd = pwd,
            jwt = jwt,
            jwtValidUntil = jwtValidUntil,
            lastActivity = lastActivity,
            currentEndpoint = currentEndpoint,
            listOfEndpoints = listOfEndpoints.toList(),
            isUsingApiKey = isUsingApi
        )
    }
}