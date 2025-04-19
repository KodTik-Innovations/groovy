package groovy.runner

import android.app.Application

class MyApp : Application() {
    companion object {
        lateinit var instance: MyApp
    }

    override fun onCreate() {
        instance = this

        AppLogger.initialize(this)

        super.onCreate()
    }
}
