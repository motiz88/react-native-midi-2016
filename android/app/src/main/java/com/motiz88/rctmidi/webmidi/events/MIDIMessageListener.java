package com.motiz88.rctmidi.webmidi.events;

public interface MIDIMessageListener {
  void onMIDIMessage(byte[] data, double timestampSeconds);
}