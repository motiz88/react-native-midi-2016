package com.motiz88.rctmidi.webmidi.impl;

import com.motiz88.rctmidi.webmidi.events.*;
import com.motiz88.rctmidi.webmidi.errors.*;
import com.motiz88.rctmidi.webmidi.MIDIPort;
import com.motiz88.rctmidi.webmidi.impl.MIDIAccessImpl;
import jp.kshoji.javax.sound.midi.*;
import com.facebook.react.bridge.WritableMap;
import java.util.Locale;

abstract class MIDIPortImpl implements MIDIPort {
  private MIDIAccessImpl midiAccess;
  private MidiDevice.Info deviceInfo;
  private MidiDevice device;

  private String id;
  private String manufacturer;
  private String name;
  private String version;
  private MIDIPort.State state = State.DISCONNECTED;
  private MIDIPort.ConnectionState connection = ConnectionState.CLOSED;

  protected MIDIAccessImpl getMIDIAccess() {
    return midiAccess;
  }

  protected MidiDevice getDevice() {
    return device;
  }

  public MidiDevice.Info getDeviceInfo() {
    return deviceInfo;
  }

  MIDIPortImpl(MidiDevice.Info deviceInfo, MIDIAccessImpl access) throws MidiUnavailableException {
    midiAccess = access;
    this.deviceInfo = deviceInfo;

    device = MidiSystem.getMidiDevice(deviceInfo);

    setManufacturer(deviceInfo.getVendor());
    setName(deviceInfo.getName() + " " + deviceInfo.getDescription());
    setVersion(deviceInfo.getVersion());
  }

  @Override
  public String getId() { return id; }
  protected void setId(String value) { id = value; }

  @Override
  public String getManufacturer() { return manufacturer; }
  private void setManufacturer(String value) { manufacturer = value; }

  @Override
  public String getName() { return name; }
  private void setName(String value) { name = value; }

  @Override
  public String getVersion() { return version; }
  private void setVersion(String value) { version = value; }

  @Override
  public MIDIPort.State getState() { return state; }

  protected abstract void openDirectionalPort() throws MidiUnavailableException;
  protected abstract void closeDirectionalPort();

  private void setState(MIDIPort.State value) {
    if (value == state)
      return;
    state = value;
    emitStateChange();
  }

  private StateChangeListener stateChangeListener;

  protected void emitStateChange() {
    if (stateChangeListener != null)
      stateChangeListener.onStateChange(this);
    getMIDIAccess().emitStateChange(this);
  }

  @Override
  public void setOnStateChange(StateChangeListener listener) {
    stateChangeListener = listener;
  }
  
  @Override
  public ConnectionState getConnection() {
    switch(connection) {
      case PENDING:
        return ConnectionState.PENDING;
      default:
        return getDevice().isOpen() ? ConnectionState.OPEN : ConnectionState.CLOSED;
    }
  }
  private void setConnection(MIDIPort.ConnectionState value) {
    if (value == getConnection())
      return;
    connection = value;
    emitStateChange();
  }
    
  @Override
  public MIDIPort close() {
    if (getConnection() == ConnectionState.CLOSED) {
      android.util.Log.d("MIDIPortImpl", "close(): already CLOSED");
      return this;
    }
    android.util.Log.d("MIDIPortImpl", "close(): closing");
    getDevice().close();
    setConnection(ConnectionState.CLOSED);
    android.util.Log.d("MIDIPortImpl", "close(): success");
    return this;
  }

  @Override
  public MIDIPort open() throws InvalidAccessError, InvalidStateError {
    if (getConnection() != ConnectionState.CLOSED) {
      android.util.Log.d("MIDIPortImpl", "open(): already OPEN or PENDING");
      try {
        openDirectionalPort();
      }
      catch (MidiUnavailableException e) {
        throw new InvalidStateError();
      }
      return this;
    }
    if (getState() == State.DISCONNECTED) {
      android.util.Log.d("MIDIPortImpl", "open(): state == DISCONNECTED -> connection = PENDING");
      setConnection(ConnectionState.PENDING);
    }
    else {
      try {
        android.util.Log.d("MIDIPortImpl", "open(): opening device");
        getDevice().open();
        android.util.Log.d("MIDIPortImpl", "open(): opening directional port");
        openDirectionalPort();
      }
      catch (MidiUnavailableException e) {
        throw new InvalidStateError();
      }
      catch (SecurityException e) {
        throw new InvalidAccessError();
      }
      setConnection(ConnectionState.OPEN);
      android.util.Log.d("MIDIPortImpl", "open(): success");
    }
    return this;
  }

  void onConnected() {
    setState(State.CONNECTED);
  }
  void onDisconnected() {
    setState(State.DISCONNECTED);
  }

  @Override
  public WritableMap toWritableMap(WritableMap result) {
    result.putString("id", getId());
    result.putString("manufacturer", getManufacturer());
    result.putString("name", getName());
    result.putString("version", getVersion());
    result.putString("type", getType().name().toLowerCase(Locale.ENGLISH));
    result.putString("state", getState().name().toLowerCase(Locale.ENGLISH));
    result.putString("connection", getConnection().name().toLowerCase(Locale.ENGLISH));
    return result;
  }
}
