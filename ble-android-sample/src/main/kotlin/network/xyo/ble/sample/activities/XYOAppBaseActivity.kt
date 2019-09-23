package network.xyo.ble.sample.activities

import network.xyo.ble.sample.XYApplication
import network.xyo.ble.generic.scanner.XYSmartScan
import network.xyo.ui.XYBaseActivity

@kotlin.ExperimentalUnsignedTypes
abstract class XYOAppBaseActivity : XYBaseActivity() {

    val scanner: XYSmartScan
        get() {
            return (this.applicationContext as XYApplication).scanner
        }

}