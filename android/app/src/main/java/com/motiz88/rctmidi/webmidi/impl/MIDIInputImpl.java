package com.motiz88.rctmidi.webmidi.impl;

import com.motiz88.rctmidi.webmidi.events.MIDIMessageListener; 
import com.motiz88.rctmidi.webmidi.MIDIInput;
import com.motiz88.rctmidi.webmidi.errors.*;
import com.motiz88.rctmidi.webmidi.MIDIPort;
import com.motiz88.rctmidi.webmidi.impl.MIDIPortImpl;
import jp.kshoji.javax.sound.midi.*;

class MIDIInputImpl extends MIDIPortImpl implements MIDIInput {
  @Override
  public final MIDIPort.Type getType() {
    return Type.INPUT;
  }

  MIDIMessageListener midiMessageListener;
  Receiver receiver = new Receiver() {
    @Override
    public void close() {}

    @Override
    public void send(MidiMessage message, long timestampMicros) {
      android.util.Log.e("MIDIInputImpl", "Receiver.send()");
      if (midiMessageListener == null)
        return;
      midiMessageListener.onMIDIMessage(message.getMessage(), timestampMicros / 1000000.0);
    }
  };

  @Override
  public void setOnMIDIMessage(MIDIMessageListener listener) throws InvalidAccessError, InvalidStateError {
    android.util.Log.d("MIDIInputImpl", "setOnMIDIMessage");
    android.util.Log.d("MIDIInputImpl", (listener != null) ? "got listener" : "listener == null");
    Receiver targetReceiver = (midiMessageListener != null) ? receiver : null; 
    if ((midiMessageListener == listener) && (transmitter != null) && (transmitter.getReceiver() == targetReceiver)) {
      android.util.Log.d("MIDIInputImpl", "nothing to do for setOnMIDIMessage");
      return;
    }
    midiMessageListener = listener;
    if (midiMessageListener != null) {
      open();
      transmitter.setReceiver(receiver);
    }
    else if (transmitter != null)
      transmitter.setReceiver(null);
  }

  MIDIInputImpl(MidiDevice.Info deviceInfo, MIDIAccessImpl access) throws MidiUnavailableException {
    super(deviceInfo, access);
    setId(Devices.idAsInput(this.getDevice()));
  }

  Transmitter transmitter = null;

  @Override
  protected void openDirectionalPort() throws MidiUnavailableException {
    if (transmitter == null) {
      android.util.Log.d("MIDIInputImpl", "openDirectionalPort(): getting transmitter");
      transmitter = getDevice().getTransmitter();
    }
    else
      android.util.Log.d("MIDIInputImpl", "openDirectionalPort(): already open");
  }

  @Override
  protected void closeDirectionalPort() {
    if (transmitter != null) {
      android.util.Log.d("MIDIInputImpl", "closeDirectionalPort(): closing transmitter");
      transmitter.close();
    }
    else
      android.util.Log.d("MIDIInputImpl", "closeDirectionalPort(): already closed");
    transmitter = null;
  }
 
}