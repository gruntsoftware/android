package com.brainwallet.presenter.base

abstract class BasePresenter<out V : BaseView>(var view: BaseView?) {
    init {
        inject()
    }

    private fun inject() {
    }

    abstract fun subscribe()

    abstract fun unsubscribe()

    fun detach() {
        view = null
    }

    fun isAttached() = view != null
}
