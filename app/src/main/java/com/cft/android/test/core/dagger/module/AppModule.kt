package com.cft.android.test.core.dagger.module

import android.content.Context
import com.cft.android.test.feature.model.IOperationManager
import com.cft.android.test.feature.model.OperationManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


/**
 * Created by administrator <dshumkov@icerockdev.com> on 26.10.18.
 */

@Module
class AppModule(private val mAppContext: Context) {

    @Provides
    @Singleton
    fun provideAppContext(): Context = mAppContext

    @Provides
    @Singleton
    fun provideOperationManager(context: Context): IOperationManager = OperationManager(context)
}