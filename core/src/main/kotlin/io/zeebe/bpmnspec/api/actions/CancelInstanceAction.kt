package io.zeebe.bpmnspec.api.actions

import io.zeebe.bpmnspec.api.Action
import io.zeebe.bpmnspec.api.WorkflowInstanceContext
import io.zeebe.bpmnspec.api.runner.TestRunner

class CancelInstanceAction(
        val workflowInstance: String?
) : Action {

    override fun execute(runner: TestRunner, contexts: MutableMap<String, WorkflowInstanceContext>) {
        val context = workflowInstance?.let { contexts[workflowInstance] }
                ?: contexts.values.first()

        runner.cancelWorkflowInstance(context)
    }
}