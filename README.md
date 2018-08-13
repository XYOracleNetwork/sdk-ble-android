# sdk-ble-android

A robust Bluetooth solution for Android. This BLE SDK was written from ground-up, in Kotlin,
 to help developers with the agonizing issues with Android the BLE stack. 
Not only will this SDK make XYO apps better, but bring XYO functionality to existing apps.

### PREREQUISITES

* JDK 1.8
* Android SDK
  - Kotlin
  - Build Tools 27+
  
### Installing

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
in progress...

### License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details