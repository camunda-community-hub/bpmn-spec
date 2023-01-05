package io.zeebe.bpmnspec.runner

import io.zeebe.bpmnspec.ProcessInstanceKey
import java.io.InputStream


interface SpecActionExecutor : AutoCloseable {

    fun deployProcess(name: String, bpmnXml: InputStream)

    fun createProcessInstance(bpmnProcessId: String, variables: String): ProcessInstanceKey

    fun completeTask(jobType: String, variables: String)

    fun publishMessage(messageName: String, correlationKey: String, variables: String)

    fun throwError(jobType: String, errorCode: String, errorMessage: String)

    fun cancelProcessInstance(processInstanceKey: ProcessInstanceKey)

}