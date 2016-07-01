package com.motiz88.rctmidi.webmidi;

import com.motiz88.rctmidi.Writable;
import com.motiz88.rctmidi.webmidi.events.*;
import com.motiz88.rctmidi.webmidi.errors.*;

public interface MIDIPort extends Writable, StateChangeEmitter {
  enum Type {
    INPUT, OUTPUT 
  }
  enum State {
    CONNECTED, DISCONNECTED 
  }
  enum ConnectionState {
    OPEN, PENDING, CLOSED 
  }

  String getId();
  String getManufacturer();
  String getName();
  String getVersion();
  MIDIPort.Type getType();
  MIDIPort.State getState();
  ConnectionState getConnection();

  MIDIPort close();

  MIDIPort open() throws InvalidAccessError, InvalidStateError;

}
