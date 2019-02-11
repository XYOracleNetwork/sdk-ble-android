package network.xyo.ble

abstract class XYPromise<T> {
    open fun resolve(value: T?) {}
    open fun reject(error: String) {}
}