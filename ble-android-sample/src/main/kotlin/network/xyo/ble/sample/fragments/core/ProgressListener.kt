package network.xyo.ble.sample.fragments.core

interface ProgressListener {
    fun hideProgress()
    fun showProgress()
    fun isInProgress() : Boolean
}