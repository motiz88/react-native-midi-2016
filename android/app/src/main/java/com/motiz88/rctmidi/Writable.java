package com.motiz88.rctmidi;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public interface Writable { 
  WritableMap toWritableMap(WritableMap result);
}
