package ltd.grunt.brainwallet.presenter.base

interface BaseView {
    fun showProgress() {}

    fun hideProgress() {}

    fun onTokenExpired() {}

    fun showError(error: String)

    fun showError(errorId: Int)
}
