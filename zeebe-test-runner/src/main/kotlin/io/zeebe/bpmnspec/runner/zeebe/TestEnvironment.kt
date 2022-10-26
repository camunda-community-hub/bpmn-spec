package io.zeebe.bpmnspec.runner.zeebe

import io.camunda.zeebe.client.ZeebeClient
import io.zeebe.bpmnspec.runner.zeebe.zeeqs.ZeeqsClient

interface TestEnvironment {
    val zeebeClient: ZeebeClient
    val zeeqsClient: ZeeqsClient
    val zeebeEventRepository: ZeebeEventRepository
    val zeebeService: ZeebeService

    val isRunning: Boolean

    fun setup()

    fun cleanUp()
}
