package groovy.runner

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val groovySourceCode =
            """
            class Main {
                static void main(String[] args) {
                    System.out.println('Hello World!')
                }
            }
            """.trimIndent()

        val outputDir = File("build/groovy-classes")
        if (!outputDir.exists()) outputDir.mkdirs()

        try {
            println("Initializing compiler configuration...")

            val configuration =
                CompilerConfiguration().apply {
                    targetBytecode = CompilerConfiguration.JDK8
                    targetDirectory = outputDir
                }

            println("Creating compilation unit...")

            val unit = CompilationUnit(configuration)
            // Provide a name and use StringReader as the source
            unit.addSource("Main.groovy", groovySourceCode)

            println("Starting compilation...")
            unit.compile()

            println("Compilation successful! Output directory: ${outputDir.absolutePath}")

            // Optional: clean up
            if (deleteDirectory(outputDir)) {
                println("Deleted outputDir: ${outputDir.absolutePath}")
            } else {
                System.err.println("Failed to delete outputDir: ${outputDir.absolutePath}")
            }
        } catch (t: Throwable) {
            deleteDirectory(outputDir)
            System.err.println("Compilation failed:")
            t.printStackTrace()
        }
    }

    fun deleteDirectory(dir: File): Boolean {
        if (dir.isDirectory) {
            dir.listFiles()?.forEach { deleteDirectory(it) }
        }
        return dir.delete()
    }
}
