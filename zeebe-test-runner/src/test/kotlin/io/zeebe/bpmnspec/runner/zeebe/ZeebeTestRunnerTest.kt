package io.zeebe.bpmnspec.runner.zeebe

import io.zeebe.bpmnspec.ClasspathResourceResolver
import io.zeebe.bpmnspec.SpecRunner
import io.zeebe.bpmnspec.api.runner.ProcessInstanceState
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.jupiter.api.Test

class ZeebeTestRunnerTest {

    private val resourceResolver =
        ClasspathResourceResolver(classLoader = ZeebeTestRunnerTest::class.java.classLoader)
    private val specRunner = SpecRunner(
        testRunner = ZeebeTestRunner(),
        resourceResolver = resourceResolver
    )

    @Test
    fun `ZeebeRunner should work standalone`() {

        val runner = ZeebeTestRunner()

        runner.beforeEach()

        val bpmnXml = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo.bpmn")
        runner.deployProcess("demo.bpmn", bpmnXml)

        val wfContext = runner.createProcessInstance("demo", "{}")

        runner.completeTask("a", "{}")
        runner.completeTask("b", "{}")
        runner.completeTask("c", "{}")

        await.untilAsserted {
            assertThat(runner.getProcessInstanceState(wfContext))
                .isEqualTo(ProcessInstanceState.COMPLETED)
        }

        runner.afterEach()
    }

    @Test
    fun `Runner with ZeebeTestRunner should run the YAML spec`() {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `Runner with ZeebeTestRunner should run the Kotlin spec`() {

        val spec = DemoTestSpecBuilder.demo()
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should run the YAML spec with message`() {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo3.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should run the Kotlin spec with message`() {

        val spec = DemoTestSpecBuilder.demo3()
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }


    @Test
    fun `should run the YAML spec with incident`() {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo-incident.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }

    @Test
    fun `should run the Kotlin spec with incident`() {

        val spec = DemoTestSpecBuilder.demoIncident()
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)
    }


    @Test
    fun `should fail verification`() {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/failed-test-case.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)

        val testResult = result.testResults[0]
        assertThat(testResult.success).isFalse()
        assertThat(testResult.message).isEqualTo("Expected the element with name 'B'  to be in state 'COMPLETED' but was 'ACTIVATED'.")

        assertThat(testResult.testCase.verifications).hasSize(3)
        assertThat(testResult.successfulVerifications).containsExactly(testResult.testCase.verifications[0])
        assertThat(testResult.failedVerification).isEqualTo(testResult.testCase.verifications[1])
    }

    @Test
    fun `should collect output`() {

        val spec = ZeebeTestRunnerTest::class.java.getResourceAsStream("/demo.yaml")
        val result = specRunner.runSpec(spec)

        assertThat(result.testResults).hasSize(1)

        val testResult = result.testResults[0]
        assertThat(testResult.success).isTrue()
        assertThat(testResult.output).hasSize(1)

        val testOutput = testResult.output[0];
        assertThat(testOutput.state).isEqualTo(ProcessInstanceState.COMPLETED)
        assertThat(testOutput.elementInstances).hasSize(10)
        assertThat(testOutput.variables).isEmpty()
        assertThat(testOutput.incidents).isEmpty()
    }

}