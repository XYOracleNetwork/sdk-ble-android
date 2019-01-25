package network.xyo.ble.sample.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_battery.*
import kotlinx.android.synthetic.main.fragment_song.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import network.xyo.ble.devices.XY3BluetoothDevice
import network.xyo.ble.devices.XY4BluetoothDevice
import network.xyo.ble.devices.XYBluetoothDevice
import network.xyo.ble.sample.R
import network.xyo.ble.sample.XYDeviceData
import network.xyo.ui.ui
import unsigned.Ubyte
import unsigned.Ushort
import unsigned.toUbyte
import java.nio.ByteBuffer

class SongFragment : XYDeviceFragment() {

    var currentSong = ""

    interface SlotItem {
        val value: ByteBuffer
    }

    /**********************************************/
    /**  VVPP PPPP PPPP PPPP DDDD DDDD DDDD DDDD **/
    /**********************************************/

    class Note(val pitch: Ushort, val volume: Short, val duration: Short): SlotItem {
        override val value: ByteBuffer
            get() {
                val byte1 = pitch.and(0x3fff).shr(8).toUbyte().or(Ubyte(volume).and(0x03).shl(6)).toByte()
                val byte0 = pitch.and(0xff).or(Ubyte(volume).and(0x03).toUshort()).toByte()
                val byte3 = Ushort(duration).shr(8).toByte()
                val byte2 = Ubyte(duration).and(0xff).toByte()
                return ByteBuffer.wrap(byteArrayOf(byte0, byte1, byte2, byte3))
            }
    }

    /**********************************************/
    /**  1111 1111 AAAA AAAA DDDD DDDD DDDD DDDD **/
    /**********************************************/

    class Action(val action: Ushort, val data: Int): SlotItem {
        override val value: ByteBuffer
            get() {
                val byte1 = action.shr(8).toByte()
                val byte0 = action.and(0xff).toByte()
                val byte3 = data.shr(8).toByte()
                val byte2 = data.and(0xff).toByte()

                return ByteBuffer.wrap(byteArrayOf(byte0, byte1, byte2, byte3))
            }
    }

    enum class Actions(val action: Ushort) {
        Stop(Ushort(0xf100)),
        Loop(Ushort(0xf101)),
        Volume(Ushort(0xf102)),
        VolumePlus(Ushort(0xf103)),
        VolumeMinus(Ushort(0xf104))
    }

    enum class Notes(val note: Ushort) {
         MIN	(Ushort(0)),
         B0		(Ushort(31)),
         C1 	(Ushort(33)),
         CS1 	(Ushort(35)),
         D1 	(Ushort(37)),
         DS1 	(Ushort(39)),
         E1 	(Ushort(41)),
         F1 	(Ushort(44)),
         FS1 	(Ushort(46)),
         G1 	(Ushort(49)),
         GS1 	(Ushort(52)),
         A1 	(Ushort(55)),
         AS1 	(Ushort(58)),
         B1 	(Ushort(62)),
         C2 	(Ushort(65)),
         CS2 	(Ushort(69)),
         D2 	(Ushort(73)),
         DS2 	(Ushort(78)),
         E2 	(Ushort(82)),
         F2		(Ushort(87)),
         FS2	(Ushort(93)),
         G2		(Ushort(98)),
         GS2 	(Ushort(104)),
         A2 	(Ushort(110)),
         AS2	(Ushort(117)),
         B2 	(Ushort(123)),
         C3 	(Ushort(131)),
         CS3	(Ushort(139)),
         D3		(Ushort(147)),
         DS3	(Ushort(156)),
         E3 	(Ushort(165)),
         F3 	(Ushort(175)),
         FS3	(Ushort(185)),
         G3 	(Ushort(196)),
         GS3	(Ushort(208)),
         A3 	(Ushort(220)),
         AS3	(Ushort(233)),
         B3 	(Ushort(247)),
         C4 	(Ushort(262)),
         CS4	(Ushort(277)),
         D4 	(Ushort(294)),
         DS4	(Ushort(311)),
         E4 	(Ushort(330)),
         F4 	(Ushort(349)),
         FS4	(Ushort(370)),
         G4 	(Ushort(392)),
         GS4	(Ushort(415)),
         A4 	(Ushort(440)),
         AS4	(Ushort(466)),
         B4 	(Ushort(494)),
         C5 	(Ushort(523)),
         CS5	(Ushort(554)),
         D5 	(Ushort(587)),
         DS5	(Ushort(622)),
         E5 	(Ushort(659)),
         F5 	(Ushort(698)),
         FS5	(Ushort(740)),
         G5 	(Ushort(784)),
         GS5	(Ushort(831)),
         A5 	(Ushort(880)),
         AS5	(Ushort(932)),
         B5 	(Ushort(988)),
         C6 	(Ushort(1047)),
         CS6	(Ushort(1109)),
         D6 	(Ushort(1175)),
         DS6	(Ushort(1245)),
         E6 	(Ushort(1319)),
         F6 	(Ushort(1397)),
         FS6	(Ushort(1480)),
         G6 	(Ushort(1568)),
         GS6	(Ushort(1661)),
         A6 	(Ushort(1760)),
         AS6	(Ushort(1865)),
         B6 	(Ushort(1976)),
         C7 	(Ushort(2093)),
         CS7	(Ushort(2217)),
         D7 	(Ushort(2349)),
         DS7	(Ushort(2489)),
         E7 	(Ushort(2637)),
         F7 	(Ushort(2794)),
         FS7	(Ushort(2960)),
         G7 	(Ushort(3136)),
         GS7	(Ushort(3322)),
         A7 	(Ushort(3520)),
         AS7	(Ushort(3729)),
         B7 	(Ushort(3951)),
         C8 	(Ushort(4186)),
         CS8	(Ushort(4435)),
         D8 	(Ushort(4699)),
         DS8	(Ushort(4978)),
         MAX	(Ushort(10000))
    }

    val song1 = ByteBuffer.allocate(32 * 4) //Sweet Child of Mine

    init {
        song1.put(Note(Notes.D5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.D6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.FS5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.D5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.D6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.FS5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.E5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.D6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.FS5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.E5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.D6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.G6.note, 0x01, 0x100).value)
        song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Note(Notes.FS5.note, 0x01, 0x100).value)
        //song1.put(Note(Notes.A5.note, 0x01, 0x100).value)
        song1.put(Action(Actions.Stop.action, 0x0).value)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_song, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_song_refresh.setOnClickListener {
            readCurrentSong()
        }

        button_song_one.setOnClickListener {
            setSongOne()
        }
    }

    override fun onResume() {
        super.onResume()

        if (deviceData?.level.isNullOrEmpty() && progressListener?.isInProgress() == false) {
            readCurrentSong()
        } else {
            updateUI()
        }
    }

    private fun updateUI() {
        ui {
            text_current_song.text = currentSong
            progressListener?.hideProgress()
        }
    }

    fun readCurrentSong() {
        ui {
            progressListener?.showProgress()
        }

        when (device) {
            is XY4BluetoothDevice -> {
                val xy4 = (device as? XY4BluetoothDevice)
                xy4?.let {
                    getXY4Values(xy4)
                }
            }
            is XY3BluetoothDevice -> {
                val xy3 = (device as? XY3BluetoothDevice)
                xy3?.let {
                    getXY3Values(xy3)
                }
            }
            else -> {
                text_battery_level.text = getString(R.string.unknown_device)
            }
        }
    }

    fun setSongOne() {
        ui {
            progressListener?.showProgress()
        }

        when (device) {
            is XY4BluetoothDevice -> {
                val xy4 = (device as? XY4BluetoothDevice)
                xy4?.let {
                    GlobalScope.launch {
                        var hasConnectionError = true
                        it.connection {
                            hasConnectionError = false
                            val songData = song1.array()
                            var offset = 0
                            while (offset < (songData.size / 2)) {
                                val data = byteArrayOf(
                                        11,
                                        offset.toByte(),
                                        songData[offset * 2],
                                        songData[offset * 2 + 1],
                                        songData[offset * 2 + 2],
                                        songData[offset * 2 + 3],
                                        songData[offset * 2 + 4],
                                        songData[offset * 2 + 5],
                                        songData[offset * 2 + 6],
                                        songData[offset * 2 + 7]
                                )

                                log.info("setSongOne: ${offset}:${data.contentToString()} ")
                                it.primary.buzzerConfig.set(data).await()
                                offset += 4
                            }
                        }.await()
                        updateUI()
                        checkConnectionError(hasConnectionError)
                    }
                }
            }
            else -> {
                text_battery_level.text = getString(R.string.unknown_device)
            }
        }
    }

    private fun getXY4Values(device: XY4BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            device.connection {
                hasConnectionError = false
                var data = device.primary.buzzerConfig.get().await()
                currentSong = "${data.value?.size}"

            }.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    private fun getXY3Values(device: XY3BluetoothDevice) {
        GlobalScope.launch {
            var hasConnectionError = true

            val conn = device.connection {
                hasConnectionError = false
            }
            conn.await()

            updateUI()
            checkConnectionError(hasConnectionError)
        }
    }

    companion object {

        fun newInstance() =
                SongFragment()

        fun newInstance (device: XYBluetoothDevice?, deviceData : XYDeviceData?) : SongFragment {
            val frag = SongFragment()
            frag.device = device
            frag.deviceData = deviceData
            return frag
        }
    }

}
