# XY SDK for Android

Add it to your build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and:

```gradle
dependencies {
    compile 'network.xyo.ble.android:{latest version}'
}
```
## Using the SDK

Initialize the XYSDKManager, XYApiSession, and XYSmartScan with the app context:

```java
/* Initialize SDK */
XYSDKManager manager = new XYSDKManager(MyApplication.getAppContext());
/* Initialize the XY Bluetooth Scanner */
XYSmartScan.Instance().init(this);
```
Add a listener for the XYSmartScanner:

```java
        XYSmartScan.instance.addListener(String.valueOf(this.hashCode()), new XYSmartScan.Listener() {
            @Override
            public void entered(XYDevice device) {
            }

            @Override
            public void exited(XYDevice device) {
            }

            @Override
            public void detected(XYDevice device) {
            }

            @Override
            public void buttonPressed(XYDevice device, XYDevice.ButtonType buttonType) {
            }

            @Override
            public void buttonRecentlyPressed(XYDevice device, XYDevice.ButtonType buttonType) {
            }

            @Override
            public void statusChanged(XYSmartScan.Status status) {
            }

            @Override
            public void updated(XYDevice device) {
            }
        });
```
Start the bluetooth scanner

```java
XYSmartScan.instance.startAutoScan(context, 2000/*interval*/, 1000/*duration*/);
```

## Using different Classes offered by XY SDK for Android

### XYDevice

#### Definitions

```java
static final int BATTERYLEVEL_INVALID = 0;
static final int BATTERYLEVEL_CHECKED = -1;
static final int BATTERYLEVEL_NOTCHECKED = -2;
static final int BATTERYLEVEL_SCHEDULED = -3;

static final HashMap<UUID, XYDevice.Family> uuid2family;

static final HashMap<XYDevice.Family, UUID> family2uuid;

static final HashMap<XYDevice.Family, String> family2prefix;

static Comparator<XYDevice> Comparator = new Comparator<XYDevice>() {
        @Override
        public int compare(XYDevice lhs, XYDevice rhs) {
            return lhs._id.compareTo(rhs._id);
	}
};
```
Used to compare the ids of devices.  
Returns a 0 if they are the same and a positive number is the previous device's id is greater than the next device.  
It will return a negative number if the previous device's id is lower than the next device.  
This can be used to sort devices by id.  

```java
public final static Comparator<XYDevice> DistanceComparator = new Comparator<XYDevice>() {
        @Override
        public int compare(XYDevice lhs, XYDevice rhs) {
            return Integer.compare(lhs.getRssi(), rhs.getRssi());
        }
};
```
Used to compare distances of devices.  
This can be used to sort devices by distances from host device.  

```java
static final int STATE_CONNECTED = BluetoothProfile.STATE_CONNECTED;
static final int STATE_DISCONNECTED = BluetoothProfile.STATE_DISCONNECTED;
static final int STATE_DISCONNECTING = BluetoothProfile.STATE_DISCONNECTING;
```

#### XYDevice

```java
XYDevice(String id);
```

Starts an instance of XYDevice.

##### Parameters

String id - a unique id to identify a device.

##### Example

```java
XYDevice device = new XYDevice("uniqueId");
```

#### getInstanceCount

```java
static int getInstanceCount();
```

Returns the number of XYDevice instances.

##### Example

```java
XYDevice.getInstanceCount();
```

#### stayConnected

```java
void stayConnected(final Context context, boolean value);
```

Turns the Stay Connected feature of a device on or off.  

##### Parameters

final Context context - the context where this method is called.  

boolean value - true turns Stay Connected on and false turns it off.  

##### Example

```java
XYDevice device = new XYDevice("id");
device.stayConnected(currentActivity.this, true);
```

#### getDistance

```java
double getDistance();
```

Returns distance between device and host device.

##### Example

```java
XYDevice device = new XYDevice("id");
device.getDistance();
```

#### getBeaconAddress

```java
String getBeaconAddress();
```

Returns the address of a beacon.

##### Example

```java
XYDevice device = new XYDevice("id");
device.getBeaconAddress();
```

#### getFirmwareVersion

```java
String getFirmwareVersion();
```

Returns the firmware version of a device.

##### Example

```java
XYDevice device = new XYDevice("id");
device.getFirmwareVersion();
```

#### isConnected

```java
boolean isConnected();
```

Returns true if device is connected.  
Returns false if device is not connected.  

##### Example

```java
XYDevice device = new XYDevice("id");
device.isConnected();
```

#### getBatteryLevel

```java
int getBatteryLevel();
```

Returns the battery level of a beacon as an int from 0 to 100.  

##### Example

```java
XYDevice device = new XYDevice("id");
device.getBatteryLevel();
```

#### checkBatteryAndVersionInFuture

```java
void checkBatteryAndVersionInFuture(final Context context);
```

Starts a timer to check the battery and version 6-12 minutes after the function is called.  
This can be used when a user is signing up or first logging in so that load speeds are faster.  

##### Parameters

final Context context - current context where this method is called.  

##### Example

```java
XYDevice device = new XYDevice("id");
device.checkBatteryAndVersionInFuture(currentActivity.this);
```

#### checkBattery

```java
void checkBattery(final Context context)
```

Checks the battery level.

##### Parameters

final Context context - current context where this method is called.

##### Example

```java
XYDevice device = new XYDevice("id");
device.checkBattery(currentActivity.this);
```

#### checkVersion

```java
void checkVersion(final Context context)
```

Checks the version of firmware.

##### Parameters

final Context context - current context where this method is called.

##### Example

```java
XYDevice device = new XYDevice("id");
device.checkVersion(currentActivity.this);
```

#### getId

```java
String getId();
```

Returns the beacon id string.

##### Example

```java
XYDevice device = new XYDevice("id");
device.getId();
```

#### isUpdateSignificant

```java
boolean isUpdateSignificant();
```

Returns true if the update will be significant (long time since last update).

##### Example

```java
XYDevice device = new XYDevice("id");
device.isUpdateSignificant();
```

#### getFamily

```java
Family getFamily();
```

Returns the family of a device. (ie: XY3 or Mobile)

##### Example

```java
XYDevice device = new XYDevice("id");
device.getFamily();
```

#### getProximity

```java
Proximity getProximity();
```

Returns a proximity describing distance between beacon and host device. (ie: Far, Medium, Near, etc)

##### Example

```java
XYDevice device = new XYDevice("id");
device.getProximity();
```

#### addListener

```java
void addListener(String key, Listener listener);
```

Used to add a listener to the XYDevice class.

##### Parameters

String key - key to identify where listener is attached to.  
Listener listener - see Listener interface.  

##### Example

```java
XYDevice device = new XYDevice("id");
device.addListener("key", listener);
```

#### removeListener

```java
void removeListener(String key);
```

Used to remove a listener from the XYDevice class.

##### Parameters

String key - key to identify where listener is attached to.

##### Example

```java
XYDevice device = new XYDevice("id");
device.removeListener("key");
```

#### Family

```java
enum Family {
        Unknown,
        XY1,
        XY2,
        XY3,
        Mobile,
        Gps,
        Near
}
```

Family types for devices.

#### ButtonType

```java
public enum ButtonType {
        None,
        Single,
        Double,
        Long
}
```

Button types for ifttt and zapier.

#### Proximity

```java
public enum Proximity {
        None,
        OutOfRange,
        VeryFar,
        Far,
        Medium,
        Near,
        VeryNear,
        Touching
}
```

Proximity values for how close a beacon is to a KeepNear device.

#### Listener

```java
public interface Listener {
	void entered(final XYDevice device);

        void exited(final XYDevice device);

        void detected(final XYDevice device);

        void buttonPressed(final XYDevice device, final ButtonType buttonType);

        void buttonRecentlyPressed(final XYDevice device, final ButtonType buttonType);

        void connectionStateChanged(final XYDevice device, int newState);

        void updated(final XYDevice device);
}
```

An interface for Listener to use with the XYDevice class.

### XYDeviceCharacteristic

#### Bluetooth Characteristic Definitions

```java
static final UUID ControlBuzzer = UUID.fromString("F014FFF1-0439-3000-E001-00001001FFFF");
static final UUID ControlHandshake = UUID.fromString("F014FFF2-0439-3000-E001-00001001FFFF");
static final UUID ControlVersion = UUID.fromString("F014FFF4-0439-3000-E001-00001001FFFF");
static final UUID ControlBuzzerSelect = UUID.fromString("F014FFF6-0439-3000-E001-00001001FFFF");
static final UUID ControlSurge = UUID.fromString("F014FFF7-0439-3000-E001-00001001FFFF");
static final UUID ControlButton = UUID.fromString("F014FFF8-0439-3000-E001-00001001FFFF");
static final UUID ControlDisconnect = UUID.fromString("F014FFF9-0439-3000-E001-00001001FFFF");

static final UUID ExtendedConfigVirtualBeaconSettings = UUID.fromString("F014FF02-0439-3000-E001-00001001FFFF");
static final UUID ExtendedConfigTone = UUID.fromString("F014FF03-0439-3000-E001-00001001FFFF");
static final UUID ExtendedConfigRegistration = UUID.fromString("F014FF05-0439-3000-E001-00001001FFFF");
static final UUID ExtendedConfigInactiveVirtualBeaconSettings = UUID.fromString("F014FF06-0439-3000-E001-00001001FFFF");
static final UUID ExtendedConfigInactiveInterval = UUID.fromString("F014FF07-0439-3000-E001-00001001FFFF");
static final UUID ExtendedConfigGPSInterval = UUID.fromString("2ABBAA000-4393-000E-0010-0001001FFFF");
static final UUID ExtendedConfigGPSMode = UUID.fromString("2A99AA000-4393-000E-0010-0001001FFFF");

static final UUID BasicConfigLockStatus = UUID.fromString("F014EE01-0439-3000-E001-00001001FFFF");
static final UUID BasicConfigLock = UUID.fromString("F014EE02-0439-3000-E001-00001001FFFF");
static final UUID BasicConfigUnlock = UUID.fromString("F014EE03-0439-3000-E001-00001001FFFF");
static final UUID BasicConfigUUID = UUID.fromString("F014EE04-0439-3000-E001-00001001FFFF");
static final UUID BasicConfigMajor = UUID.fromString("F014EE05-0439-3000-E001-00001001FFFF");
static final UUID BasicConfigMinor = UUID.fromString("F014EE06-0439-3000-E001-00001001FFFF");
static final UUID BasicConfigInterval = UUID.fromString("F014EE07-0439-3000-E001-00001001FFFF");

static final UUID ExtendedControlSIMStatus = UUID.fromString("2AEEAA00-0439-3000-E001-00001001FFFF");
static final UUID ExtendedControlLED = UUID.fromString("2AAAAA00-0439-3000-E001-00001001FFFF");

static final UUID BatteryLevel = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
```

### XYDeviceService

#### Bluetooth Service Definitions

```java
static final UUID Control = UUID.fromString("F014ED15-0439-3000-E001-00001001FFFF");
static final UUID BasicConfig = UUID.fromString("F014EE00-0439-3000-E001-00001001FFFF");
static final UUID ExtendedConfig = UUID.fromString("F014FF00-0439-3000-E001-00001001FFFF");
static final UUID CsrOta = UUID.fromString("00001016-D102-11E1-9B23-00025B00A5A5");
static final UUID BatteryStandard = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
static final UUID ExtendedControl = UUID.fromString("F014AA00-0439-3000-E001-00001001FFFF");
```

### XYSmartScan

#### Definitions

```java
final static XYSmartScan instance;
```

#### getInterval

```java
int getInterval();
```

Returns the autoscan interval.
This is the time between when a period of scanning finishes and begins.

##### Example

```java
XYSmartScan.instance.getInterval();
```

#### getPeriod

```java
int getPeriod();
```

Returns autoscan period.
The is the duration of a scan.

##### Example

```java
XYSmartScan.instance.getPeriod();
```

#### getOutOfRangePulsesMissed

```java
int getOutOfRangePulsesMissed();
```

Returns the number of times a peiod occurred without a connection being made to the device.  

##### Example

```java
XYSmartScan.instance.getOutOfRangePulsesMissed();
```

#### init

```java
void init(Context context, long currentDeviceId, int missedPulsesForOutOfRange);
```

Start a smart scan using bluetooth.

##### Parameters

Context context - current context.  
long currentDeviceId - the unique id of a device.  
int missedPulsesForOutOfRange - maximum number of missed pulses before device will be considered out of range.    

##### Example

```java
XYSmartScan.instance.init(currentActivity.this, deviceId, 30);
```

#### cleanup

```java
void cleanup(Context context)
```

Unregisters receiver and stops auto scanning.

##### Parameters

Context context - current context.

##### Example

```java
XYSmartScan.instance.cleanup(this);
```

#### getMissedPulsesForOutOfRange

```java
int getMissedPulsesForOutOfRange();
```

Returns the number of missed pulses currently set to declare a device out of range.

##### Example

```java
XYSmartScan.instance.getMissedPulsesForOutOfRange();
```

#### setMissedPulsesForOutOfRange

```java
int setMissedPulsesForOutOfRange
```

Set the number of pulses missed in order to declare a device out of range.

##### Example

```java
XYSmartScan.instance.setMissedPulsesForOutOfRange(30);
```

#### enableBluetooth

```java
void enableBluetooth(Context context)
```

Enables bluetooth adapter for device.

##### Example

```java
XYSmartScan.instance.enableBluetooth(currentActivity.this);
```

#### deviceFromId

```java
XYDevice deviceFromId(String id);
```

Returns an XYDevice after passing in an id for that device.

##### Parameters

String id - id of device.

##### Example

```java
XYDevice device = XYSmartScan.instance.deviceFromId("id");
```

#### getCurrentDeviceId

```java
long getCurrentDeviceId();
```

Returns id of current instance of XYSmartScan device.

##### Example

```java
XYSmartScan.instance.getCurrentDeviceId();
```

#### getCurrentDevice

```java
XYDevice getCurrentDevice();
```

Returns current instance of XYDevice.

##### Example

```java
XYDevice device = XYSmartScan.instance.getCurrentDevice();
```

#### startAutoScan

```java
void startAutoScan(final Context context, int interval, int period);
```

Starts a new timer for an auto scan.

##### Parameters

final Context context - current context.  
int interval - how often to scan for device.  
int period - duration of scan.  

##### Example

```java
XYSmartScan.instance.startAutoScan(currentActivity.this, 1500, 1000);
```

#### stopAutoScan

```java
void stopAutoScan();
```

Stops scanning for devices.

##### Example

```java
XYSmartScan.instance.stopAutoScan()l
```

#### isLocationAvailable

```java
static boolean isLocationAvailable(@NonNull Context context);
```

Returns true if location services are available.  
Returns false if location are not available.  

##### Parameters

@NonNull Context context - current context.  Cannot be null.  

##### Example

```java
XYSmartScan.isLocationAvailable(currentActivity);
```

#### getStatus

```java
Status getStatus(Context context, boolean refresh);
```

Returns a Status.  See Status enum below in documentation.

##### Parameters

Context context - current context.  
boolean refresh - if true is entered default bluetooth adapter will be used.  

##### Example

```java
XYSmartScan.Status status = XYSmartScan.instance.getStatus(this, true);
```

#### getDevices

```java
List<XYDevice> getDevices();
```

Returns a list of all devices.

##### Example

```java
List devices = XYSmartScan.instance.getDevices();
```

#### addListener

```java
void addListener(final String key, final Listener listener);
```

Adds a Listener for the XYSmartScan class.

##### Parameters

String key - key to identify Listener.  
Listener listener - see Listener interface at bottom of this section.  

##### Example

```java
XYSmartScan.instance.addListener("key", listener);
```

#### removeListener

```java
void removeListener(String key);
```

Removes Listener for XYSmartScan.

##### Parameters

String key - key to identify Listener.

##### Example

```java
XYSmartScan.instance.removeListener("key");
```

#### Listener

```java
public interface Listener {
        void entered(XYDevice device);

        void exited(XYDevice device);

        void detected(XYDevice device);

        void buttonPressed(XYDevice device, XYDevice.ButtonType buttonType);

        void buttonRecentlyPressed(XYDevice device, XYDevice.ButtonType buttonType);

        void statusChanged(Status status);

        void updated(XYDevice device);
}
```

An interface for Listener to use with the XYSmartScan class.

#### Overrided Methods of XYDevice Listener

```java
@Override
public void entered(XYDevice device) {reportEntered(device);}

@Override
public void exited(XYDevice device) {reportExited(device);}

@Override
public void detected(XYDevice device) {reportDetected(device);}

@Override
public void buttonPressed(XYDevice device, XYDevice.ButtonType buttonType) {reportButtonPressed(device, buttonType);}

@Override
public void buttonRecentlyPressed(XYDevice device, XYDevice.ButtonType buttonType) {reportButtonRecentlyPressed(device, buttonType);}

@Override
public void connectionStateChanged(XYDevice device, int newState) {

}

@Override
public void updated(XYDevice device) {

}
```

#### Status

```java
public enum Status {
        None,
        Enabled,
        BluetoothUnavailable,
        BluetoothUnstable,
        BluetoothDisabled,
        LocationDisabled
}
```

Different statuses available within the XYSmartScan class.  

### XYDeviceAction

All of the java files in the action folder represent different actions you can use.  
They are all based off of the XYDeviceAction class.  
To implement any of these other classes, reference this class to see how different methods are being used.  
XYDeviceAction is an abstract class.  For implementation see specific action Class you wish to use.  
Find list of all actions at bottom of this section with parameters.  

#### Definitions

```java
static final int STATUS_QUEUED = 1;
static final int STATUS_STARTED = 2;
static final int STATUS_SERVICE_FOUND = 3;
static final int STATUS_CHARACTERISTIC_FOUND = 4;
static final int STATUS_CHARACTERISTIC_READ = 5;
static final int STATUS_CHARACTERISTIC_WRITE = 6;
static final int STATUS_CHARACTERISTIC_UPDATED = 7;
static final int STATUS_COMPLETED = 8;
```

#### XYDeviceAction

```java
XYDeviceAction(XYDevice device);
```

Used to create an instance of XYDeviceAction.

##### Parameters

XYDevice device - an instance of an XYDevice object.

##### Example

```java
XYDeviceAction action = new XYDeviceAction(XYSmartScan.instance.deviceFromId("deviceId"));
```

#### getDevice

```java
XYDevice getDevice();
```

Returns XYDevice object.

##### Example

```java
XYDeviceAction action = new XYDeviceAction(XYSmartScan.instance.deviceFromId("deviceId"));
action.getDevice();
```

#### getKey

```java
String getKey();
```

Returns a hashed key for the XYDeviceAction class.  

##### Example

```java
XYDeviceAction action = new XYDeviceAction(XYSmartScan.instance.deviceFromId("deviceId"));
action.getKey();
```

#### start

```java
void start(final Context context);
```

Executes the action.  

##### Parameters

final Context context - current context.  

##### Example

```java
XYDeviceAction action = new XYDeviceAction(XYSmartScan.instance.deviceFromId("deviceId"));
action.start(currentActivity.this);
```

#### statusChanged

```java
boolean statusChanged(int status, BluetoothGatt gatt, BlueToothGattCharacteristic characteristic, boolean success);
```

Returns true if status changed and false otherwise.  

##### Parameters

int status - a status that is defined at top of this section under definitions.    
BluetoothGatt gatt - a bluetooth gatt.  
BlueToothGattCharacteristic characteristic - a bluetooth gatt characteristic.  
boolean success - you can pass true to immediately confirm status is changed or false to check if status changed.  

For further reading on bluetooth see this link: https://learn.adafruit.com/introduction-to-bluetooth-low-energy/gatt   

##### Example

Override this method when using an action.  
Look at documentation below to see how to override for a particular action.  

```java
XYDeviceAction action = new XYDeviceAction(device) {

	@Override
	public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
		boolean result = super.statusChanged(status, gatt, characteristic, success);
		switch (status) {
			case STATUS_STARTED:
                       		if (success) {
                               		// do something ...
                       		} else {
                               		// do something ...
                       		}
                       		break;
                       	case STATUS_COMPLETED:
                       		if (success) {
                               		// do something ...
                       		} else {
                               		// do something ...
                       		}
                       		break;
                       	case STATUS_SERVICE_FOUND:
                       		// do something ...
                       		break;
                      	case STATUS_CHARACTERISTIC_FOUND:
                       		// do something ...
                       		break;
               	}
               	return result;
            }
};
```
#### List of actions

If the below action has a constructor listed beneath it, that means it requires different parameters than the abstract DeviceAction class.  
Otherwise, if no constructor is listed, the constructor is the same as XYDeviceAction.  
Make sure to Override the statusChanged method of each action.  
The implementations for overriding statusChanged can be found under their respective action.  

##### XYDeviceActionBuzz

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND:
                characteristic.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gatt.writeCharacteristic(characteristic);
                break;
	}
	return result;
}
```

##### XYDeviceActionBuzzSelect

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND:
                characteristic.setValue(_index, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gatt.writeCharacteristic(characteristic);
                break;
        }
	return result;
}
```

###### constructor

```java
XYDeviceActionBuzzSelect(XYDevice device, int index);
```

##### XYDeviceActionDisconnect

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
	Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(1, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionGetBatteryLevel

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                gatt.readCharacteristic(characteristic);
                break;
	}
	return result;
}
```

##### XYDeviceActionGetButtonState

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                gatt.readCharacteristic(characteristic);
                break;
        }
        return result;
}
```

##### XYDeviceActionGetGPSInterval

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getValue();
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetGPSMode

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetInactiveInterval

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetInactiveVirtualBeacon

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getValue();
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                gatt.readCharacteristic(characteristic);
                break;
        }
        return result;
}
```

##### XYDeviceActionGetInterval

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetLED

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getValue();
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetLockStatus

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                byte[] versionBytes = characteristic.getValue();
                if (versionBytes.length > 0) {
                    value = "";
                    for (byte b : versionBytes) {
                        value += String.format("%02x:", b);
                    }
                }
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetMajor

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetMinor

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetRegistration

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = (characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) != 0);
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                gatt.readCharacteristic(characteristic);
                break;
        }
        return result;
}
```

##### XYDeviceActionGetSIMStatus

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getValue();
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetUUID

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getValue();
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetVersion

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                byte[] versionBytes = characteristic.getValue();
                if (versionBytes.length > 0) {
                    value = "";
                    for (byte b : versionBytes) {
                        value += String.format("%02x:", b);
                    }
                }
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                if (!gatt.readCharacteristic(characteristic)) {
                    XYBase.logError(TAG, "Characteristic Read Failed");
                }
                break;
        }
        return result;
}
```

##### XYDeviceActionGetVirtualBeacon

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_READ:
                value = characteristic.getValue();
                break;
            case STATUS_CHARACTERISTIC_FOUND:
                gatt.readCharacteristic(characteristic);
                break;
        }
        return result;
}
```
##### XYDeviceActionSetGPSInterval

###### constructor

```java
public XYDeviceActionSetGPSInterval(XYDevice device, byte[] value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetGPSMode

###### constructor

```java
public XYDeviceActionSetGPSMode(XYDevice device, int value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetInactiveInterval

###### constructor

```java
XYDeviceActionSetInactiveInterval(XYDevice device, int value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetInactiveVirtualBeacon

###### constructor

```java
XYDeviceActionSetInactiveVirtualBeacon(XYDevice device, byte[] value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetInterval

###### constructor

```java
XYDeviceActionSetInterval(XYDevice device, int value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetLED

###### constructor

```java
XYDeviceActionSetLED(XYDevice device, int value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetLock

###### constructor

```java
XYDeviceActionSetLock(XYDevice device, byte[] value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetMajor

###### constructor

```java
XYDeviceActionSetMajor(XYDevice device, int value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetMinor

###### constructor

```java
XYDeviceActionSetMinor(XYDevice device, int value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetRegistration

###### constructor

```java
XYDeviceActionSetRegistration(XYDevice device, boolean value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                if (value) {
                    characteristic.setValue(0x01, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                } else {
                    characteristic.setValue(0x00, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                }
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetUUID

###### constructor

```java
XYDeviceActionSetUUID(XYDevice device, byte[] value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSetVirtualBeacon

###### constructor

```java
XYDeviceActionSetVirtualBeacon(XYDevice device, byte[] value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND: {
                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
                break;
            }
        }
        return result;
}
```

##### XYDeviceActionSubscribeButton

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_UPDATED: {
                Log.i(TAG, "statusChanged:Updated:" + characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                return true;
            }
            case STATUS_CHARACTERISTIC_FOUND: {
                Log.i(TAG, "statusChanged:Characteristic Found");
                if (!gatt.setCharacteristicNotification(characteristic, true)) {
                    XYBase.logError(TAG, "Characteristic Notification Failed");
                } else {
                    _gatt = gatt;
                    _characteristic = characteristic;
                }

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
                return true;
            }
        }
        return result;
}
```
##### XYDeviceActionUnlock

###### constructor

```java
XYDeviceActionUnlock(XYDevice device, byte[] value);
```

###### statusChanged

```java
@Override
public boolean statusChanged(int status, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean success) {
        Log.v(TAG, "statusChanged:" + status + ":" + success);
        boolean result = super.statusChanged(status, gatt, characteristic, success);
        switch (status) {
            case STATUS_CHARACTERISTIC_FOUND:
                characteristic.setValue(value);
                gatt.writeCharacteristic(characteristic);
                break;
        }
        return result;
}
```
