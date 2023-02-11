package org.camunda.community.zeebe.spec.runner.zeeqs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class ZeeqsClient(
    private val zeeqsEndpoint: String = "http://localhost:9000/graphql"
) {

    private val logger = LoggerFactory.getLogger(ZeeqsClient::class.java)

    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    fun getProcessInstanceKeys(): List<Long> {

        val responseBody = sendRequest("{ processInstances { nodes  { key, state } } }")
        val response = objectMapper.readValue<ProcessInstancesResponse>(responseBody)

        return response.data.processInstances.nodes.map { it.key.toLong() }
    }

    fun getProcessInstanceState(processInstanceKey: Long): String? {

        val responseBody =
            sendRequest("{ processInstance(key: $processInstanceKey) { key, state } }")
        val response = objectMapper.readValue<ProcessInstanceResponse>(responseBody)

        return response.data.processInstance?.state
    }

    fun getElementInstances(processInstanceKey: Long): List<ElementInstanceDto> {

        val responseBody =
            sendRequest("{ processInstance(key: $processInstanceKey) { elementInstances { elementId, elementName, state } } }")
        val response = objectMapper.readValue<ElementInstancesResponse>(responseBody)

        return response.data.processInstance?.elementInstances ?: emptyList()
    }

    fun getProcessInstanceVariables(processInstanceKey: Long): List<VariableDto> {

        val responseBody =
            sendRequest("{ processInstance(key: $processInstanceKey) { variables { name, value, scope { elementId, elementName } } } }")
        val response = objectMapper.readValue<VariablesResponse>(responseBody)

        return response.data.processInstance?.variables ?: emptyList()
    }

    fun getIncidents(processInstanceKey: Long): List<IncidentDto> {

        val responseBody =
            sendRequest("{ processInstance(key: $processInstanceKey) { incidents { errorType, errorMessage, state, elementInstance { elementId, elementName } } } }")
        val response = objectMapper.readValue<IncidentsResponse>(responseBody)

        return response.data.processInstance?.incidents ?: emptyList()
    }

    private fun sendRequest(query: String): String {
        val requestBody = HttpRequest.BodyPublishers.ofString("""{ "query": "$query" }""")

        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://$zeeqsEndpoint"))
            .header("Content-Type", "application/json")
            .POST(requestBody)
            .build()

        logger.trace("Send query request to ZeeQS: {}", query)

        val response = httpClient
            .send(request, HttpResponse.BodyHandlers.ofString())

        val statusCode = response.statusCode()
        val responseBody = response.body()

        logger.trace(
            "Received query response from ZeeQS: [status-code: {}, body: {}]",
            statusCode,
            responseBody
        )

        if (statusCode != 200) {
            throw RuntimeException("Failed to query ZeeQS. [status-code: $statusCode, body: $responseBody]")
        }

        return responseBody
    }

    data class ProcessInstanceResponse(val data: ProcessInstanceDataDto)
    data class ProcessInstanceDataDto(val processInstance: ProcessInstanceDto?)
    data class ProcessInstanceDto(val key: String, val state: String)

    data class ProcessInstancesResponse(val data: ProcessInstancesDataDto)
    data class ProcessInstancesDataDto(val processInstances: ProcessInstancesDto)
    data class ProcessInstancesDto(val nodes: List<ProcessInstanceDto>)

    data class ElementInstancesResponse(val data: ElementInstancesDataDto)
    data class ElementInstancesDataDto(val processInstance: ElementInstancesDto?)
    data class ElementInstancesDto(val elementInstances: List<ElementInstanceDto>)
    data class ElementInstanceDto(
        val state: String,
        val elementId: String,
        val elementName: String?
    )

    data class VariablesResponse(val data: VariablesDataDto)
    data class VariablesDataDto(val processInstance: VariablesDto?)
    data class VariablesDto(val variables: List<VariableDto>)
    data class VariableDto(val name: String, val value: String, val scope: VariableScopeDto?)
    data class VariableScopeDto(val elementId: String, val elementName: String?)

    data class IncidentsResponse(val data: IncidentsDataDto)
    data class IncidentsDataDto(val processInstance: IncidentsDto?)
    data class IncidentsDto(val incidents: List<IncidentDto>)
    data class IncidentDto(
        val errorType: String,
        val errorMessage: String?,
        val state: String,
        val elementInstance: IncidentElementInstanceDto?
    )

    data class IncidentElementInstanceDto(val elementId: String, val elementName: String?)
}
