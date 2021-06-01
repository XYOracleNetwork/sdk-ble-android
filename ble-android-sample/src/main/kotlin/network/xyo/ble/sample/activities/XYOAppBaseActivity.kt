package network.xyo.ble.sample.activities

import androidx.appcompat.app.AppCompatActivity
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ble.sample.XYApplication

abstract class XYOAppBaseActivity : AppCompatActivity() {

    val scanner: XYSmartScan
        get() {
            return (this.applicationContext as XYApplication).scanner
        }

}
