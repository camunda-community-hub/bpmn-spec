package io.zeebe.bpmnspec

import java.io.File
import java.io.FilenameFilter
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.isDirectory

class ClasspathResourceResolver(
    private val classLoader: ClassLoader,
    private val rootDirectory: String = ""
) : ResourceResolver {

    override fun getResource(resourceName: String): InputStream {

        val resource = if (rootDirectory.isBlank()) {
            resourceName
        } else {
            "$rootDirectory/$resourceName"
        }

        return classLoader.getResourceAsStream(resource)
            ?: throw RuntimeException("no resource found with name '$resource' in the classpath '${classLoader.name}'")
    }

    override fun getResources(): List<File> {
        return classLoader.getResource(rootDirectory)
            ?.let { Path.of(it.toURI()) }
            ?.takeIf { it.isDirectory() }
            ?.let { dir ->
                dir.toFile().listFiles(deploymentFilter())
                    ?.toList()
            }
            ?: throw RuntimeException("No resource directory found with name '${rootDirectory}'")
    }

    private fun deploymentFilter(): FilenameFilter {
        return FilenameFilter { _, name ->
            name.endsWith(".bpmn") || name.endsWith(".dmn")
        }
    }
}