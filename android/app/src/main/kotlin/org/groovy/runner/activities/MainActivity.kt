package org.groovy.runner.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import org.groovy.runner.R
import org.groovy.runner.databinding.ActivityMainBinding
import org.groovy.runner.executeAsyncProvideError

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolBar.title = getString(R.string.app_name)

        val future =
            executeAsyncProvideError(
                {
                },
                { _, _ -> },
            )

        future.whenComplete { result, error ->
            if (error != null) {
            } else {
            }
            runOnUiThread {
            }
        }
    }
}
