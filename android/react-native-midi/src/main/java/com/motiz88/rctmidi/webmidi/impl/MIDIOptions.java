package com.motiz88.rctmidi.webmidi.impl;

import com.facebook.react.bridge.ReadableMap;

public class MIDIOptions {
  private boolean software = false;
  private boolean sysex = false;

  public boolean getSoftware() {
    return software;
  }

  public boolean getSysex() {
    return sysex;
  }

  public MIDIOptions(ReadableMap options) {
    if (options == null)
      return;
    if (options.hasKey("software"))
      software = options.getBoolean("software");
    if (options.hasKey("sysex"))
      sysex = options.getBoolean("sysex");
  }
}