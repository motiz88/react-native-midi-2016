/* eslint camelcase: 0 */

import deepEqual from 'deep-equal';
import { DeviceEventEmitter } from './react-native';

const EVENT_MIDIACCESS_ONSTATECHANGE = 'com.motiz88.rctmidi.webmidi.MIDIAccess#statechange';
const EVENT_MIDIPORT_ONSTATECHANGE = 'com.motiz88.rctmidi.webmidi.MIDIPort#statechange&id=';
const EVENT_MIDIINPUT_ONMIDIMESSAGE = 'com.motiz88.rctmidi.webmidi.MIDIInput#midimessage&id=';

class MidiModuleMockDriver {
  eventsAttached = {};

  constructor (parent, data = {}) {
    this.parent = parent;
    this._initialData = {...data};
    this.reset();
  }

  reset (resetData) {
    this.data = {...(resetData || this._initialData)};
    this.eventsAttached = {};
  }

  getPortType (id) {
    let type;
    if (this.data.inputs[id]) {
      type = 'input';
    } else if (this.data.outputs[id]) {
      type = 'output';
    }
    if (!type) {
      throw new Error('No such port');
    }
    return type;
  }

  setData (data) {
    // const modified = !deepEqual(this.data, data);
    this.data = data;
  }

  emitIfAttached (event, ...args) {
    if (this.eventsAttached[event]) {
      DeviceEventEmitter.mock.emit(event, ...args);
    }
  }

  mergePort (id, newPort) {
    const collection = this.getPortType(id) + 's';
    const port = this.data[collection][id] || {};
    newPort = {...port, ...newPort};
    const portModified = !deepEqual(port, newPort);
    this.setData({
      ...this.data,
      [collection]: {
        ...this.data[collection],
        [id]: newPort
      }
    });
    if (portModified) {
      this.emitIfAttached(EVENT_MIDIACCESS_ONSTATECHANGE, newPort);
      this.emitIfAttached(EVENT_MIDIPORT_ONSTATECHANGE + id, newPort);
    }
    return this.data[collection][id];
  }

  async sendToInput (id, data, timeStamp = undefined) {
    this.emitIfAttached(EVENT_MIDIINPUT_ONMIDIMESSAGE + id, {data, timeStamp});
  }
}

class MidiModuleMock {
  EVENT_MIDIACCESS_ONSTATECHANGE = EVENT_MIDIACCESS_ONSTATECHANGE;
  EVENT_MIDIPORT_ONSTATECHANGE = EVENT_MIDIPORT_ONSTATECHANGE;
  EVENT_MIDIINPUT_ONMIDIMESSAGE = EVENT_MIDIINPUT_ONMIDIMESSAGE;

  mock = new MidiModuleMockDriver(this, {
    inputs: {},
    outputs: {}
  });

  async requestMIDIAccess (options) {
    return this.mock.data;
  }

  async MIDIAccess_get_data () {
    return this.mock.data;
  }

  async MIDIAccess_setOnStateChange (attach) {
    this.mock.eventsAttached[this.EVENT_MIDIACCESS_ONSTATECHANGE] = !!attach;
  }

  async MIDIPort_open (id) {
    return this.mock.mergePort(id, {connection: 'open'});
  }

  async MIDIPort_close (id) {
    return this.mock.mergePort(id, {connection: 'closed'});
  }

  async MIDIPort_setOnStateChange (id, attach) {
    this.mock.eventsAttached[this.EVENT_MIDIPORT_ONSTATECHANGE + id] = !!attach;
  }

  async MIDIInput_setOnMidiMessage (id, attach) {
    this.mock.eventsAttached[this.EVENT_MIDIINPUT_ONMIDIMESSAGE + id] = !!attach;
    if (attach) await this.MIDIPort_open(id);
  }

  async MIDIOutput_send (id, data, timestamp) {
    await this.MIDIPort_open(id);
  }
}

export default new MidiModuleMock();
