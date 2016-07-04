package com.motiz88.rctmidi;

import android.support.annotation.*;

import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.util.*;

import com.motiz88.rctmidi.webmidi.*;
import com.motiz88.rctmidi.webmidi.impl.MIDIAccessImpl;
import com.motiz88.rctmidi.webmidi.impl.MIDIOptions;
import com.motiz88.rctmidi.webmidi.events.*;
import com.motiz88.rctmidi.MapWriter;

class MidiModule extends ReactContextBaseJavaModule {
  /**
   * statechange events from the {@link MIDIAccess} singleton are emitted with this event name. 
   */
  private static final String EVENT_MIDIACCESS_ONSTATECHANGE = "com.motiz88.rctmidi.webmidi.MIDIAccess#statechange";

  /**
   * statechange events from a {@link MIDIPort} will be emitted with this event name, followed directly by the port's ID.
   * {@see MIDIPort#getId} 
   */
  private static final String EVENT_MIDIPORT_ONSTATECHANGE = "com.motiz88.rctmidi.webmidi.MIDIPort#statechange&id=";

  /**
   * midimessage events from a {@link MIDIInput} will be emitted with this event name, followed directly by the port's ID.
   * {@see MIDIPort#getId} 
   */
  private static final String EVENT_MIDIINPUT_ONMIDIMESSAGE = "com.motiz88.rctmidi.webmidi.MIDIInput#midimessage&id=";

  private MIDIAccessImpl midiAccess;

  // Native module interface 

  public MidiModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("EVENT_MIDIACCESS_ONSTATECHANGE", EVENT_MIDIACCESS_ONSTATECHANGE);
    constants.put("EVENT_MIDIPORT_ONSTATECHANGE", EVENT_MIDIPORT_ONSTATECHANGE);
    constants.put("EVENT_MIDIINPUT_ONMIDIMESSAGE", EVENT_MIDIINPUT_ONMIDIMESSAGE);
    return constants;
  }

  @Override
  public String getName() {
    return "MidiAndroid";
  }

  // RCTDeviceEventEmitter helper
  private void sendEvent(String eventName,
                        @Nullable WritableMap params) {
    getReactApplicationContext()
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  /**
   * Native implementation of Navigator.requestMIDIAccess
   * 
   * Initializes the {@link MIDIAccess} singleton and asynchronously returns its data fields as a {@link WritableMap} to the JS thread. 
   */
  @ReactMethod
  public void requestMIDIAccess(ReadableMap options, Promise promise) {
    try {
      if (midiAccess == null)
        midiAccess = new MIDIAccessImpl(getCurrentActivity(), new MIDIOptions(options));
      promise.resolve(MapWriter.toWritableMap(midiAccess));
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }

  /**
   * Asynchronously returns the {@link MIDIAccess} singleton's data fields as a {@link WritableMap} to the JS thread. 
   */
  @ReactMethod
  public void MIDIAccess_get_data(Promise promise) {
    try {
      promise.resolve(MapWriter.toWritableMap(midiAccess));
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }

  /**
   * Start or stop sending statechange events to the JS thread from the {@link MIDIAccess} singleton. 
   */
  @ReactMethod
  public void MIDIAccess_setOnStateChange(final boolean attach, Promise promise) {
    try {
      if (!attach)
        midiAccess.setOnStateChange(null);
      else
        midiAccess.setOnStateChange(new StateChangeListener() {
          @Override
          public void onStateChange(MIDIPort port) {
            sendEvent(EVENT_MIDIACCESS_ONSTATECHANGE, MapWriter.toWritableMap(port));
          }
        });
      promise.resolve(null);
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }

  /**
   * Opens a {@link MIDIPort} and asynchronously returns its data fields as a {@link WritableMap} to the JS thread. 
   */
  @ReactMethod
  public void MIDIPort_open(final String id, Promise promise) {
    try {
      MIDIPort port = midiAccess.getAllPorts().get(id);
      promise.resolve(MapWriter.toWritableMap(port.open()));
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }

  /**
   * Closes a {@link MIDIPort} and asynchronously returns its data fields as a {@link WritableMap} to the JS thread. 
   */
  @ReactMethod
  public void MIDIPort_close(final String id, Promise promise) {
    try {
      MIDIPort port = midiAccess.getAllPorts().get(id);
      promise.resolve(MapWriter.toWritableMap(port.close()));
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }

  /**
   * Start or stop sending statechange events to the JS thread from a {@link MIDIPort} specified by ID. 
   */
  @ReactMethod
  public void MIDIPort_setOnStateChange(final String id, final boolean attach, Promise promise) {
    try {
      MIDIPort port = midiAccess.getAllPorts().get(id);
      if (!attach)
        port.setOnStateChange(null);
      else
        port.setOnStateChange(new StateChangeListener() {
          @Override
          public void onStateChange(MIDIPort self) {
            sendEvent(EVENT_MIDIPORT_ONSTATECHANGE + id, MapWriter.toWritableMap(self));
          }
        });
      promise.resolve(null);
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }
  
  /**
   * Start or stop sending midimessage events to the JS thread from a {@link MIDIinput} specified by ID. 
   */
  @ReactMethod
  public void MIDIInput_setOnMidiMessage(final String id, final boolean attach, Promise promise) {
    try {
      MIDIInput port = (MIDIInput) midiAccess.getAllPorts().get(id);
      if (!attach)
        port.setOnMIDIMessage(null);
      else
        port.setOnMIDIMessage(new MIDIMessageListener() {
          @Override
          public void onMIDIMessage(byte[] data, double timestamp) {
            WritableMap event = Arguments.createMap();
            int[] idata = new int[data.length];
            for (int i = 0; i < data.length; ++i) { // FIXME: likely slow as heck
              idata[i] = (int) (data[i] & 0xFF);
            }
            event.putArray("data", Arguments.fromArray(idata));
            event.putDouble("timestamp", timestamp);
            sendEvent(EVENT_MIDIINPUT_ONMIDIMESSAGE + id, event);
          }
        });
      promise.resolve(null);
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }

  /**
   * Send the specified MIDI message to a {@link MIDIOutput} specified by ID. 
   */
  @ReactMethod
  public void MIDIOutput_send(final String id, ReadableArray data, double timestamp, Promise promise) {
    try {
      MIDIOutput port = (MIDIOutput) midiAccess.getAllPorts().get(id);
      byte[] bdata = new byte[data.size()];
      for (int i = 0; i < bdata.length; ++i) { // FIXME: likely slow as heck
        bdata[i] = (byte) data.getInt(i);
      }
      port.send(bdata, timestamp);
      promise.resolve(null);
    }
    catch(Throwable e) {
      promise.reject(e);
    }
  }
}
