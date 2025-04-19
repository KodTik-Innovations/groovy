package groovy.runner.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import groovy.runner.Main.deleteDirectory
import groovy.runner.R
import groovy.runner.databinding.ActivityMainBinding
import groovy.runner.executeAsyncProvideError
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.title = getString(R.string.app_name)

        val outputDir = File(filesDir, "build/groovy-classes")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outBaos = ByteArrayOutputStream()
        val errBaos = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err

        System.setOut(PrintStream(outBaos))
        System.setErr(PrintStream(errBaos))

        val future =
            executeAsyncProvideError(
                {
                    val groovySourceCode =
                        """
                        class Main {
                            static void main(String[] args) {
                                System.out.println('Hello World!')
                            }
                        }
                        """.trimIndent()

                    println("Initializing compiler configuration...")

                    val configuration =
                        CompilerConfiguration().apply {
                            targetBytecode = CompilerConfiguration.JDK8
                            targetDirectory = outputDir
                        }

                    println("Creating compilation unit...")

                    val unit = CompilationUnit(configuration)
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
                },
                { _, _ -> },
            )

        future.whenComplete { result, error ->
            val outputBuilder = StringBuilder()

            if (error != null) {
                System.setErr(originalErr)

                val sw = StringWriter()
                error.printStackTrace(PrintWriter(sw))
                outputBuilder.append("\n[STDERR]\n").append(originalErr.toString())

                outputBuilder.append("\n[BUILD ERROR]\n").append(sw.toString())
            } else {
                System.setOut(originalOut)
                outputBuilder.append("\n[BUILD RESULT] $originalOut")
            }

            runOnUiThread {
                binding.textView.text = outputBuilder.toString()
            }
        }
    }
}
