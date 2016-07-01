package com.motiz88.rctmidi.webmidi.impl;
import jp.kshoji.javax.sound.midi.*;

class Devices {
  private static MidiDevice getMidiDevice(MidiDevice.Info info) {
    try {
      return MidiSystem.getMidiDevice(info);
    }
    catch (MidiUnavailableException e) {
      return null;
    }
  }
  public static boolean isInput(MidiDevice.Info info) {
    return isInput(getMidiDevice(info));
  }

  public static boolean isOutput(MidiDevice.Info info) {
    return isOutput(getMidiDevice(info));
  }

  public static boolean isInput(MidiDevice device) {
    return device != null && device.getMaxTransmitters() != 0;
  }

  public static boolean isOutput(MidiDevice device) {
    return device != null && device.getMaxReceivers() != 0;
  }

  public static String idAsInput(MidiDevice.Info info) {
    return idAsInput(getMidiDevice(info));
  }

  public static String idAsOutput(MidiDevice.Info info) {
    return idAsOutput(getMidiDevice(info));
  }

  public static String idAsInput(MidiDevice device) {
    return isInput(device) ? ("I" + device.getDeviceInfo().hashCode()) : null;
  }

  public static String idAsOutput(MidiDevice device) {
    return isOutput(device) ? ("O" + device.getDeviceInfo().hashCode()) : null;
  }
}
