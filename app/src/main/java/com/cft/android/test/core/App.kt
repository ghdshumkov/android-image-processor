package com.cft.android.test.core

import android.app.Application
import com.cft.android.test.core.dagger.AppComponent
import com.cft.android.test.core.dagger.DaggerAppComponent
import com.cft.android.test.core.dagger.module.AppModule


/**
 * Created by administrator <dshumkov@icerockdev.com> on 26.10.18.
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()

        AppComponent.instance = DaggerAppComponent.builder()
                .appModule(AppModule(applicationContext))
                .build()
    }
}