package com.motiz88.rctmidi.webmidi;

import java.util.*;
import com.motiz88.rctmidi.Writable;
import com.motiz88.rctmidi.webmidi.events.*;

public interface MIDIAccess extends Writable, StateChangeEmitter {
  public Map<String, MIDIInput> getInputs();
  public Map<String, MIDIOutput> getOutputs();
  public boolean getSysexEnabled();
}
