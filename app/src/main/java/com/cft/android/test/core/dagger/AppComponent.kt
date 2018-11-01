package com.cft.android.test.core.dagger

import android.content.Context
import com.cft.android.test.core.dagger.module.AppModule
import com.cft.android.test.feature.model.IOperationManager
import dagger.Component
import javax.inject.Singleton


/**
 * Created by administrator <dshumkov@icerockdev.com> on 26.10.18.
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    companion object {
        lateinit var instance: AppComponent
    }

    fun getOperationManager(): IOperationManager
}