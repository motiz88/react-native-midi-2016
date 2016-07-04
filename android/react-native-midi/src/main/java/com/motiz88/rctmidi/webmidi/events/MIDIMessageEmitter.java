package com.motiz88.rctmidi.webmidi.events;
import com.motiz88.rctmidi.webmidi.errors.*;

public interface MIDIMessageEmitter {
  void setOnMIDIMessage(MIDIMessageListener listener) throws InvalidAccessError, InvalidStateError;
}
