package com.motiz88.rctmidi;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import java.util.Map;
import com.motiz88.rctmidi.webmidi.*;

public final class MapWriter { 
  public static <T extends Writable> WritableMap toWritableMap(Map<String, T> source, WritableMap result) {
    for (Map.Entry<String, T> entry : source.entrySet()) {
      result.putMap(entry.getKey(), MapWriter.toWritableMap(entry.getValue()));
    }
    return result;
  }

  public static <T extends Writable> WritableMap toWritableMap(Map<String, T> source) {
    return toWritableMap(source, Arguments.createMap());
  }
  
  public static WritableMap toWritableMap(Writable source, WritableMap result) {
    return source.toWritableMap(result);
  }

  public static WritableMap toWritableMap(Writable source) {
    return toWritableMap(source, Arguments.createMap());
  }
}
