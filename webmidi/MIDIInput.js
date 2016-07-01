import MIDIPort from './MIDIPort';
import MIDIMessageEvent from './MIDIMessageEvent';
import MidiModule from '../MidiModule';
import { DeviceEventEmitter } from 'react-native';

const priv = {
  onmidimessage: Symbol('onmidimessage')
};

export default class MIDIInput extends MIDIPort {
  constructor (...args) {
    super(...args);
    this[priv.onmidimessage] = null;
    DeviceEventEmitter.addListener(MidiModule.EVENT_MIDIINPUT_ONMIDIMESSAGE + this.id, ({data, timeStamp}) => {
      if (typeof this.onmidimessage === 'function') {
        if (!(data instanceof Uint8Array)) {
          data = new Uint8Array(data);
        }
        this.onmidimessage(new MIDIMessageEvent(this, data, timeStamp));
      }
    });
  }

  get onmidimessage () {
    return this[priv.onmidimessage];
  }

  set onmidimessage (handler) {
    this[priv.onmidimessage] = handler;
    const attach = typeof handler === 'function';
    MidiModule.MIDIInput_setOnMidiMessage(this.id, attach);
  }
}
