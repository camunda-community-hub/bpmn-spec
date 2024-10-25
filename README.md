# Zeebe Spec

[![](https://img.shields.io/badge/Community%20Extension-An%20open%20source%20community%20maintained%20project-FF4700)](https://github.com/camunda-community-hub/community)
[![](https://img.shields.io/badge/Lifecycle-Unmaintained-lightgrey)](https://github.com/Camunda-Community-Hub/community/blob/main/extension-lifecycle.md#Unmaintained-)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

[![Compatible with: Camunda Platform 8](https://img.shields.io/badge/Compatible%20with-Camunda%20Platform%208-0072Ce)](https://github.com/camunda-community-hub/community/blob/main/extension-lifecycle.md#compatiblilty)

> [!IMPORTANT]
> This community extension is **unmaintained** and outdated. It may not support new Camunda versions.
>
> Thank you to all [contributors](https://github.com/camunda-community-hub/zeebe-spec/graphs/contributors) for making it a great extension. :tada:

A tool to run tests for BPMN processes on [Zeebe](https://github.com/camunda/zeebe).

![The idea](docs/bpmn-spec.png)

## Install

The tests can be run by calling
the [SpecRunner](/core/src/main/kotlin/org/camunda/community/zeebe/spec/SpecRunner.kt) directly in
code, or by
using the JUnit integration.

### JUnit Integration

1) Add the Maven dependencies:

```
<dependency>
  <groupId>org.camunda.community</groupId>
  <artifactId>zeebe-spec-junit-extension</artifactId>
  <version>3.0.0</version>
  <scope>test</scope>
</dependency>
```    

2) Put the spec and BPMN files in the resource folder (e.g. `/src/test/resources/`)

3) Write a JUnit test class like the following (here in Kotlin):

```
package my.package

import org.camunda.community.zeebe.spec.SpecRunner
import org.camunda.community.zeebe.spec.assertj.SpecAssertions.assertThat
import org.junit.jupiter.params.ParameterizedTest

@ZeebeSpecRunner
class BpmnTest(private val specRunner: SpecRunner) {
 
    @ParameterizedTest
    @ZeebeSpecSource(specDirectory = "specs")
    fun `should pass all tests`(spec: ZeebeSpecTestCase) {

        val testResult = specRunner.runSingleTestCase(spec.testCase)

        assertThat(testResult).isSuccessful()
    }

}
```

4) Run the JUnit test class

![Junit test results](docs/bpmn-spec-junit.png)

## Usage

Example spec with one test case:

*YAML Spec*

```yaml
testCases:
  - name: fulfill-condition
    description: should fulfill the condition and enter the upper task
    instructions:
      - action: create-instance
        args:
          bpmn_process_id: exclusive-gateway
      - action: complete-task
        args:
          job_type: a
          variables: '{"x":8}'
      - verification: element-instance-state
        args:
          element_name: B
          state: activated
``` 

*Kotlin Spec*

```kotlin
val testSpecFulfillCondition =
    testSpec {
        testCase(
            name = "fulfill-condition",
            description = "should fulfill the condition and enter the upper task"
        ) {
            createInstance(bpmnProcessId = "exclusive-gateway")

            completeTask(jobType = "a", variables = mapOf("x" to 8))

            verifyElementInstanceState(
                selector = byName("B"),
                state = ElementInstanceState.ACTIVATED
            )
        }
    }
```

### The Spec

A spec is written in a YAML text format, or alternative in Kotlin code. It contains the following
elements:

* `testCases`: a list of test cases, each test case contains the following elements
    * `name`: the (short) name of the test case
    * `description`: (optional) an additional description of the test case
    * `instructions`: a list of actions and verifications that are applied in order

### Actions

Actions drive the test case forward until the result is checked using the verifications. The
following actions are available:

### create-instance

Create a new instance of a process.

* `bpmn_process_id`: the BPMN process id of the process
* `variables`: (optional) initial variables/payload to create the instance with
* `process_instance_alias`: (optional) an alias that can be used in following actions and
  verifications to reference this instance. This can be useful if multiple instances are created.

```
      - action: create-instance
        args:
          bpmn_process_id: demo
          variables: '{"x":1}'
```

### complete-task

Complete tasks of a given type.

* `job_type`: the type or identifier of the job/task
* `variables`: (optional) variables/payload to complete the tasks with

```
      - action: complete-task
        args:
          job_type: a
          variables: '{"y":2}'
```

### throw-error

Throw error events for tasks of a given type.

* `job_type`: the type or identifier of the job/task
* `error_code`: the error code that is used to correlate the error to an catch event
* `error_message`: (optional) an additional message of the error event

```
      - action: throw-error
        args:
          job_type: b
          error_code: error-1
```

### publish-message

Publish a new message event.

* `message_name`: the name of the message
* `correlation_key`: the key that is used to correlate the message to a process instance
* `variables`: (optional) variables/payload to publish the message with

```
      - action: publish-message
        args:
          message_name: message-1
          correlation_key: key-1
          variables: '{"z":3}'
```

### cancel-instance

Cancel/terminate a process instance.

* `process_instance`: (optional) the alias of a process instance that is canceled. The alias is
  defined in the `create-instance` action. If only one instance is created then the alias is not
  required.

```
      - action: cancel-instance
        args:
          process_instance: process-1
```

### await-element-instance-state

Await until an element of the process instance is in the given state.

* `state`: the state of the element to wait for. Must be one
  of: `activated | completed | terminated | taken`
* `element_name`: (optional) the name of the element in the process
* `element_id`: (optional) as an alternative to the name, the element can be identified by its id in
  the process
* `process_instance`: (optional) the alias of a process instance. The alias is defined in
  the `create-instance` action. If only one instance is created then the alias is not required.

```
      - action: await-element-instance-state
        args:
          element_name: B
          state: activated
```

### Verifications

Verifications check the result of the test case after all actions are applied. The following
verifications are available:

### process-instance-state

Check if the process instance is in a given state.

* `state`: the state of the process instance. Must be one of: `activated | completed | terminated`
* `process_instance`: (optional) the alias of a process instance. The alias is defined in
  the `create-instance` action. If only one instance is created then the alias is not required.

```
      - verification: process-instance-state
        args:
          state: completed
```

### element-instance-state

Check if an element of the process instance is in a given state.

* `state`: the state of the element. Must be one of: `activated | completed | terminated | taken`
* `element_name`: (optional) the name of the element in the process
* `element_id`: (optional) as an alternative to the name, the element can be identified by its id in
  the process
* `process_instance`: (optional) the alias of a process instance. The alias is defined in
  the `create-instance` action. If only one instance is created then the alias is not required.

```
      - verification: element-instance-state
        args:
          element_name: B
          state: activated
```

### process-instance-variable

Check if the process instance has a variable with the given name and value. If the element name or
id is set then it checks only for (local) variables in the scope of the element.

* `name`: the name of the variable
* `value`: the value of the variable
* `element_name`: (optional) the name of the element in the process that has the variable in its
  scope
* `element_id`: (optional) as an alternative to the name, the element can be identified by its id in
  the process
* `process_instance`: (optional) the alias of a process instance. The alias is defined in
  the `create-instance` action. If only one instance is created then the alias is not required.

```
      - verification: process-instance-variable
        args:
          name: x
          value: '1'
```

### no-process-instance-variable

Check if the process instance has no variable with the given name. If the element name or id is set
then it checks only for (local) variables in the scope of the element.

* `name`: the name of the variable
* `element_name`: (optional) the name of the element in the process that has the variable in its
  scope
* `element_id`: (optional) as an alternative to the name, the element can be identified by its id in
  the process
* `process_instance`: (optional) the alias of a process instance. The alias is defined in
  the `create-instance` action. If only one instance is created then the alias is not required.

```
      - verification: no-process-instance-variable
        args:
          name: y
          element_name: B
```

### incident-state

Check if the process instance has an incident in the given state. If the element name or id is set
then it checks only for incidents in the scope of the element.

* `state`: the state of the incident. Must be one of: `created | resolved`
* `errorType`: the type/classifier of the incident
* `errorMessage`: (optional) the error message of the incident
* `element_name`: (optional) the name of the element in the process that has the incident in its
  scope
* `element_id`: (optional) as an alternative to the name, the element can be identified by its id in
  the process
* `process_instance`: (optional) the alias of a process instance. The alias is defined in
  the `create-instance` action. If only one instance is created then the alias is not required.

```
      - verification: incident-state
        args:
          error_type: EXTRACT_VALUE_ERROR
          error_message: "failed to evaluate expression 'key': no variable found for name 'key'"
          state: created
          element_name: B
```

