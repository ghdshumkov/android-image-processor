package com.cft.android.test.feature.model

import android.net.Uri


/**
 * Created by administrator <dshumkov@icerockdev.com> on 27.10.18.
 */
sealed class OperationStatus

class StatusProgress(val progress: Int): OperationStatus()
class StatusResult(val uri: Uri): OperationStatus()