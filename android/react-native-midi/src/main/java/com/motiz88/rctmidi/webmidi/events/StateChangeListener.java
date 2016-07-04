package com.motiz88.rctmidi.webmidi.events;

import com.motiz88.rctmidi.webmidi.MIDIPort;

public interface StateChangeListener {
  void onStateChange(MIDIPort port);
}