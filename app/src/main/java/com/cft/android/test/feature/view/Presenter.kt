package com.cft.android.test.feature.view


/**
 * Created by administrator <dshumkov@icerockdev.com> on 20.10.18.
 */
abstract class Presenter<View> {

    protected var view: View? = null

    open fun onAttachViewInterface(view: View) {
        if (this.view != null) {
            throw RuntimeException("View is already attached")
        }
        this.view = view
    }

    open fun onDetachViewInterface() {
        if (view == null) {
            throw RuntimeException("View is not attached")
        }
        view = null
    }
}