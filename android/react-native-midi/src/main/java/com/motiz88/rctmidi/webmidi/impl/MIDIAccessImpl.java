package com.motiz88.rctmidi.webmidi.impl;

import java.util.*;
import com.motiz88.rctmidi.MapWriter;
import com.motiz88.rctmidi.webmidi.events.*;
import com.motiz88.rctmidi.webmidi.errors.*;
import com.motiz88.rctmidi.webmidi.*;
import jp.kshoji.javax.sound.midi.*;
import android.content.Context;
import jp.kshoji.javax.sound.midi.listener.*;
import android.support.annotation.*;
import com.facebook.react.bridge.WritableMap;

public final class MIDIAccessImpl implements MIDIAccess, AutoCloseable, OnMidiDeviceAttachedListener, OnMidiDeviceDetachedListener {
  private final HashMap<String, MIDIInput> inputs = new HashMap();
  private final HashMap<String, MIDIOutput> outputs = new HashMap();
  private boolean sysexEnabled;

  private final Map<String, MIDIInput> inputsView = Collections.unmodifiableMap(inputs);
  private final Map<String, MIDIOutput> outputsView = Collections.unmodifiableMap(outputs);

  private UsbMidiSystem usbMidiSystem;
  private BleMidiSystem bleMidiSystem;

  private final HashMap<String, MIDIPortImpl> allPorts = new HashMap(); // We never remove ports from this collection
  private final Map<String, MIDIPortImpl> allPortsView = Collections.unmodifiableMap(allPorts);

  private final HashMap<MidiDevice.Info, DeviceLookupRecord> devices = new HashMap();

  private StateChangeListener stateChangeListener;

  private class DeviceLookupRecord {
    @Nullable
    public MIDIInputImpl input = null;
    @Nullable
    public MIDIOutputImpl output = null;
    public DeviceLookupRecord(MidiDevice.Info info) {
      String inputId = Devices.idAsInput(info);
      String outputId = Devices.idAsOutput(info);
      if (inputId != null)
        input = (MIDIInputImpl) inputs.get(inputId);
      if (outputId != null)
        output = (MIDIOutputImpl) outputs.get(outputId);
    }
  }

  public MIDIAccessImpl(Context context, MIDIOptions options) throws InvalidStateError {
    open(context, options);
  }

  public void open(Context context, MIDIOptions options) throws InvalidStateError {
    try {
      sysexEnabled = true;

      MidiSystem.addDeviceAttachedListener(this);
      MidiSystem.addDeviceDetachedListener(this);

      usbMidiSystem = new UsbMidiSystem(context);
      usbMidiSystem.initialize();

      bleMidiSystem = new BleMidiSystem(context);
      bleMidiSystem.initialize();
      bleMidiSystem.startScanDevice(); // Act as BLE MIDI Central(Server)

      final MidiDevice.Info[] midiDeviceInfos = MidiSystem.getMidiDeviceInfo();
      for (final MidiDevice.Info info : midiDeviceInfos) {
        addDevice(info);
      }
    }
    catch (MidiUnavailableException e) {
      throw new InvalidStateError();
    }
  }

  public void close() {
    if (usbMidiSystem != null) {
      usbMidiSystem.terminate();
    }
    if (bleMidiSystem != null) {
      bleMidiSystem.stopScanDevice();
      bleMidiSystem.terminate();
    }
    MidiSystem.removeDeviceAttachedListener(this);
    MidiSystem.removeDeviceDetachedListener(this);
  }

  public MIDIAccessImpl(Context context) throws InvalidStateError {
    this(context, null);
  }

  @Override
  public Map<String, MIDIInput> getInputs() {
    return inputsView;
  }

  @Override
  public Map<String, MIDIOutput> getOutputs() {
    return outputsView;
  }

  @Override
  public boolean getSysexEnabled() {
    return sysexEnabled;
  }

  public Map<String, MIDIPortImpl> getAllPorts() {
    return allPortsView;
  }

  void emitStateChange(MIDIPort changedPort) {
    if (stateChangeListener != null)
      stateChangeListener.onStateChange(changedPort);
  }

  @Override
  public void setOnStateChange(StateChangeListener listener) {
    stateChangeListener = listener;
  }

  private void addDevice(MidiDevice.Info info) throws MidiUnavailableException {
    MIDIInputImpl inputPort = null;
    MIDIOutputImpl outputPort = null;
    if (Devices.isInput(info))
      inputPort = new MIDIInputImpl(info, this);
    if (Devices.isOutput(info))
      outputPort = new MIDIOutputImpl(info, this);
    if (inputPort != null) {
      inputs.put(inputPort.getId(), inputPort);
      allPorts.put(inputPort.getId(), inputPort);
      inputPort.onConnected(); // Also triggers MIDIAccess.statechange
    }
    if (outputPort != null) {
      outputs.put(outputPort.getId(), outputPort);
      allPorts.put(inputPort.getId(), inputPort);
      outputPort.onConnected(); // Also triggers MIDIAccess.statechange
    }
    if (inputPort == null && outputPort == null)
      android.util.Log.e("MIDIAccessImpl", "MIDI device is neither input nor output");
    devices.put(info, new DeviceLookupRecord(info));
  }

  private void removeDevice(MidiDevice.Info info) {
    DeviceLookupRecord device = devices.get(info);
    if (device != null) {
      devices.remove(info);
      if (device.input != null) {
        inputs.remove(device.input.getId());
        device.input.onDisconnected(); // Also triggers MIDIAccess.statechange
      }
      if (device.output != null) {
        outputs.remove(device.output.getId());
        device.output.onDisconnected(); // Also triggers MIDIAccess.statechange
      }
    }
  }

  @Override
  public void onMidiDeviceAttached(MidiDevice.Info info) {
    android.util.Log.d("MIDIAccessImpl", "onMidiDeviceAttached");
    try {
      addDevice(info);
    }
    catch (MidiUnavailableException e) { }
  }

  @Override
  public void onMidiDeviceDetached(MidiDevice.Info info) {
    android.util.Log.d("MIDIAccessImpl", "onMidiDeviceDetached");
    removeDevice(info);
  }

  @Override
  public WritableMap toWritableMap(WritableMap result) {
    result.putMap("inputs", MapWriter.toWritableMap(getInputs()));
    result.putMap("outputs", MapWriter.toWritableMap(getOutputs()));
    result.putBoolean("sysexEnabled", getSysexEnabled());
    return result;
  }
}
