package com.motiz88.rctmidi.webmidi.impl;

import jp.kshoji.javax.sound.midi.MidiMessage;
import android.support.annotation.*;

class ArbitraryMidiMessage extends MidiMessage {
  public ArbitraryMidiMessage(@NonNull final byte[] data) {
    super(data);
  }

  @Override
  public Object clone() {
    return new ArbitraryMidiMessage(getMessage());
  }   
}