package network.xyo.ble.gatt

import java.util.UUID

/**
 * Created by arietrouw on 12/31/16.
 */

object XYDeviceService {
    val Control = UUID.fromString("F014ED15-0439-3000-E001-00001001FFFF")
    val BasicConfig = UUID.fromString("F014EE00-0439-3000-E001-00001001FFFF")
    val ExtendedConfig = UUID.fromString("F014FF00-0439-3000-E001-00001001FFFF")
    val CsrOta = UUID.fromString("00001016-D102-11E1-9B23-00025B00A5A5")
    val BatteryStandard = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
    val DeviceStandard = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")
    val ExtendedControl = UUID.fromString("F014AA00-0439-3000-E001-00001001FFFF")
    val Sensor = UUID.fromString("F014DD00-0439-3000-E001-00001001FFFF")
    val XY4Primary = UUID.fromString("a44eacf4-0104-0001-0000-5f784c9977b5")
    val AccessStandard = UUID.fromString("00001800-0000-1000-8000-00805F9B34FB")
    val Eddystone = UUID.fromString("0000feaa-0000-1000-8000-00805F9B34FB")
    val EddystoneConfig = UUID.fromString("ee0c2080-8786-40ba-ab96-99b91ac981d8")
    val AttributeStandard = UUID.fromString("00001801-0000-1000-8000-00805F9B34FB")
    val PowerStandard = UUID.fromString("00001804-0000-1000-8000-00805F9B34FB")
    val AlertStandard = UUID.fromString("00001802-0000-1000-8000-00805F9B34FB")
    val LinkLossStandard = UUID.fromString("00001803-0000-1000-8000-00805F9B34FB")
    val TimeStandard = UUID.fromString("00001805-0000-1000-8000-00805F9B34FB")

    //dialog ota service
    val SPOTA_SERVICE_UUID = UUID.fromString("0000fef5-0000-1000-8000-00805f9b34fb")
}
