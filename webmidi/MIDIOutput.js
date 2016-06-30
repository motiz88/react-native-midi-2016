import MIDIPort from './MIDIPort';
import MidiModule from '../MidiModule';

export default class MIDIOutput extends MIDIPort {
  send (data, timestamp) {
    return MidiModule.MIDIOutput_send(this.id, data, timestamp);
  }

  clear () {
    return MidiModule.MIDIOutput_clear(this.id);
  }
}
