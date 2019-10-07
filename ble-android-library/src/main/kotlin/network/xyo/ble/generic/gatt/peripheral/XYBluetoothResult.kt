package network.xyo.ble.generic.gatt.peripheral

open class XYBluetoothResult<T> {

    enum class ErrorCode(val status: Short) {
        None(0x00),
        Unsupported(0x01),
        Timeout(0x02),
        Disconnected(0x03),
        NoGatt(0x04),
        FailedToRefreshGatt(0x05),
        NoDevice(0x06),
        FailedToFindService(0x07),
        ServicesNotDiscovered(0x08),
        FailedToConnect(0x09),
        NullValue(0x0a),
        FailedToConnectGatt(0xab),
        ConnectFailedToStart(0xac),
        GattFailure(0x0d),
        ServiceDiscoveryFailed(0x0e),
        DiscoverServicesFailedToStart(0x0f),
        ReadCharacteristicFailedToStart(0x10),
        WriteCharacteristicFailedToStart(0x11),
        DiscriptorWriteFailed(0x12),
        DiscriptorWriteFailedToStart(0x13),
        CharacteristicWriteFailed(0x14),
        CharacteristicReadFailed(0x15),
        AdvertisingScanResponseNotSupported(0x16),
        AdvertisingAlreadyStarted(0x17),
        AdvertisingDataTooLarge(0x18),
        AdvertisingNotSupported(0x19),
        AdvertisingInternalError(0x1a),
        TooManyAdvertisers(0x1b),
        NoAdvertiser(0x1c),
        Unknown(0xff)
    }

    var value: T? = null
    var error: ErrorCode = ErrorCode.None

    constructor(value: T?, error: ErrorCode = ErrorCode.None) {
        this.value = value
        this.error = error
    }

    constructor(value: T?) {
        this.value = value
        this.error = ErrorCode.None
    }

    constructor(error: ErrorCode) {
        this.value = null
        this.error = error
    }

    override fun toString(): String {
        return "XYBluetoothResult: V: $value, E: ${error.name}"
    }

    open fun format(): String {
        return (value ?: error.name).toString()
    }

    fun hasError(): Boolean {
        return error != ErrorCode.None
    }
}
