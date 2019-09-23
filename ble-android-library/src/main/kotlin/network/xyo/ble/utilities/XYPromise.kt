package network.xyo.ble.utilities

//we use this to allow java code to access our coroutines
abstract class XYPromise<T> {
    open fun resolve(value: T?) {}
    open fun reject(error: String) {}
}