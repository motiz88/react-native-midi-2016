package com.motiz88.rctmidi.webmidi.impl;

import com.motiz88.rctmidi.webmidi.errors.*;
import android.support.annotation.*;
import com.motiz88.rctmidi.webmidi.MIDIOutput;
import jp.kshoji.javax.sound.midi.*;
import com.motiz88.rctmidi.webmidi.MIDIPort;

class MIDIOutputImpl extends MIDIPortImpl implements MIDIOutput {
  @Override
  public final MIDIPort.Type getType() {
    return Type.OUTPUT;
  }

  @Override
  public void send(@NonNull byte[] data, double timestampSeconds) throws InvalidAccessError, InvalidStateError {
    // TODO: In principle we should throw InvalidAccessError if data is SysEx whilst SysEx is not allowed 
    if (getState() != State.CONNECTED)
      throw new InvalidStateError();
    if (getConnection() != ConnectionState.OPEN)
      open();
    try {
      receiver.send(new ArbitraryMidiMessage(data), (long) Math.round(1000000.0 * timestampSeconds));
    }
    catch (IllegalStateException e) {
      throw new InvalidStateError(); 
    }
  }

  @Override
  public void clear() {
    // TODO: actually clear
  }

  MIDIOutputImpl(MidiDevice.Info deviceInfo, MIDIAccessImpl access) throws MidiUnavailableException {
    super(deviceInfo, access);
    setId(Devices.idAsOutput(this.getDevice()));
  }

  private Receiver receiver;

  @Override
  protected void openDirectionalPort() throws MidiUnavailableException {
    if (receiver == null) {
      receiver = getDevice().getReceiver();
    }
  }

  @Override
  protected void closeDirectionalPort() {
    if (receiver != null)
      receiver.close();
    receiver = null;
  }
}
