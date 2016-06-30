package com.motiz88.rctmidi.webmidi;
import com.motiz88.rctmidi.webmidi.errors.*;
import android.support.annotation.*;

public interface MIDIOutput extends MIDIPort {
  void send(@NonNull byte[] data, double timestamp) throws InvalidAccessError, InvalidStateError;
  void clear();
}