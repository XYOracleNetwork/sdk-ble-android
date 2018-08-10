package network.xyo.ble.sample.activities

import network.xyo.ble.sample.XYApplication
import network.xyo.ble.scanner.XYFilteredSmartScan
import network.xyo.ui.XYBaseActivity

abstract class XYOAppBaseActivity : XYBaseActivity() {
    val scanner: XYFilteredSmartScan
        get() {
            return (this.applicationContext as XYApplication).scanner
        }
}