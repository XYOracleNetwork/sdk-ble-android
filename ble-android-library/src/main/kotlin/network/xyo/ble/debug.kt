package network.xyo.ble

import network.xyo.base.hasDebugger

fun debug(
    block: () -> Unit
) {
    if (hasDebugger) block()
}
