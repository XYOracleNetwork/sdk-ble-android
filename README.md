[logo]: https://www.xy.company/img/home/logo_xy.png "XYAccount"

![logo]

# XY/XYO Bluetooth SDK (sdk-ble-android)

[![BCH compliance](https://bettercodehub.com/edge/badge/XYOracleNetwork/sdk-ble-android?branch=master)](https://bettercodehub.com/)

| Branches        | Status           |
| ------------- |:-------------:|
| Master      | [![](https://circleci.com/gh/XYOracleNetwork/sdk-ble-android.svg?style=shield)](https://circleci.com/gh/XYOracleNetwork/sdk-ble-android) |
| Develop      | [![](https://circleci.com/gh/XYOracleNetwork/sdk-ble-android/tree/develop.svg?style=shield)](https://circleci.com/gh/XYOracleNetwork/sdk-ble-android/tree/develop)      |

A robust Bluetooth solution for Android. This BLE SDK was written from ground-up, in Kotlin,
 to help developers with the agonizing issues with Android the BLE stack.
Not only will this SDK make XYO apps better, but bring XYO functionality to existing apps.  In adition to generalized BLE support, the SDK also has specific support for XY spacific hardware.

## Prerequisites

* JDK 1.8
* Android SDK
  * Kotlin
  * Build Tools 27+
  
## Installing

You can add sdk-ble-android to your existing app by cloning the project and manually adding it
to your build.gradle:

```bash
git clone git@github.com:XYOracleNetwork/sdk-ble-android.git
```

or by using jitPack:

```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

```gradle
dependencies {
    implementation 'com.github.XYOracleNetwork:sdk-ble-android:master-SNAPSHOT'
}
```

## Using the SDK

A full Working example is included in the project. Look at the sdk-android-sample folder for more information.

Configure and create an instance of the scanner:

```kotlin
// enable the device types you want to listen for
// you can create your own custom listener
// for any type of BLE device by creating a Class that extends XYBluetoothDevice
XYAppleBluetoothDevice.enable(true)     //Apple devices
XYIBeaconBluetoothDevice.enable(true)   //iBeacon device
XYFinderBluetoothDevice.enable(true)    //XY device
XY4BluetoothDevice.enable(true)         //XY4+ Find It device
XY3BluetoothDevice.enable(true)         //XY3 Find It device
XY2BluetoothDevice.enable(true)         //XY2 Find It device
XYGpsBluetoothDevice.enable(true)       //XY GPS device
myCustomDevice.enable(true)

val scanner: XYFilteredSmartScan = XYFilteredSmartScanModern(MyApplication.getAppContext())
```

Add a listener for the XYFilteredSmartScan:

```kotlin
scanner.addListener("myTAG", object : XYFilteredSmartScan.Listener() {
    override fun entered(device: XYBluetoothDevice) {
        super.entered(device)
        checkMyDeviceTypeAddListener(device)
    }

    override fun exited(device: XYBluetoothDevice) {
        super.exited(device)
    }

    override fun detected(device: XYBluetoothDevice) {
        super.detected(device)
    }

    override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
        super.connectionStateChanged(device, newState)
    }

})
```

Start scanning for BLE devices:

```kotlin
scanner.start()
```

Once we know the device type, add a specific device type listener

```kotlin
private fun checkMyDeviceTypeAddListener(device: XYBluetoothDevice) {
    (device as? XY4BluetoothDevice)?.addListener("myTAG", object : XY4BluetoothDevice.Listener(){
        override fun buttonSinglePressed(device: XYFinderBluetoothDevice) {
            super.buttonSinglePressed(device)
        }

        override fun buttonDoublePressed(device: XYFinderBluetoothDevice) {
            super.buttonDoublePressed(device)
        }

        override fun buttonLongPressed(device: XYFinderBluetoothDevice) {
            super.buttonLongPressed(device)
        }

        override fun entered(device: XYBluetoothDevice) {
            super.entered(device)
        }

        override fun exited(device: XYBluetoothDevice) {
            super.exited(device)
        }

        override fun detected(device: XYBluetoothDevice) {
            super.detected(device)
        }

        override fun connectionStateChanged(device: XYBluetoothDevice, newState: Int) {
            super.connectionStateChanged(device, newState)
        }
    })

    (device as? XY3BluetoothDevice)?.addListener("myTAG", object : XY3BluetoothDevice.Listener(){
        //add your logic here
    })
    (device as? XY2BluetoothDevice)?.addListener("myTAG", object : XY2BluetoothDevice.Listener(){
    //add your logic here
    })
    (device as? XYIBeaconBluetoothDevice)?.addListener("myTAG", object : XYIBeaconBluetoothDevice.Listener(){
    //add your logic here
    })

    //or create a custom listener for a custom BLE device
    (device as? MyCustomBluetoothDevice)?.addListener("myTAG", object : MyCustomBluetoothDevice.Listener(){
    //add your logic here
    })

}
```

Connecting to a BLE device:

```kotlin
fun connectXY4Device(device: XY4BluetoothDevice) {
    device.connection {
        // within this connection, you can do multiple tasks
        // the connection will stay open until all tasks are completed
        // the connection will auto-disconnect after 5 seconds of inactivity.

        var batteryLevel = device.batteryService.level.get().await()
        var firmwareVersion = device.deviceInformationService.firmwareRevisionString.get().await()
        var deviceName = device.genericAccessService.deviceName.get().await()
    }
}
```

IMPORTANT:

```kotlin
//make sure you stop the scanner when no longer needed
scanner.stop()

//remmove all device listeners when shutting down an activity:
 override fun onStop() {
    super.onStop()
    device!!.removeListener(TAG)
}
```

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details

## Credits

<p align="center">Made with  ❤️  by [<b>XY - The Persistent Company</b>] (https://xy.company)</p>
