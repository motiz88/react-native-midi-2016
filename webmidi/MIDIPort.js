const priv = {
  type: Symbol('type'),
  state: Symbol('state'),
  connection: Symbol('connection'),
  id: Symbol('id'),
  manufacturer: Symbol('manufacturer'),
  name: Symbol('name'),
  version: Symbol('version'),
  onstatechange: Symbol('onstatechange'),
  midiAccess: Symbol('midiAccess')
};

import MidiModule from '../MidiModule.android';
import { DeviceEventEmitter } from 'react-native';
import MIDIConnectionEvent from './MIDIConnectionEvent';
import * as packagePrivate from './packagePrivate';
export default class MIDIPort {
  constructor (midiAccess, data) {
    this[priv.type] = null;
    this[priv.state] = 'disconnected';
    this[priv.connection] = 'closed';
    this[priv.name] = null;
    this[priv.version] = null;
    this[priv.onstatechange] = null;
    this[priv.id] = null;
    this[priv.manufacturer] = null;

    this[packagePrivate.mergeData](data);

    DeviceEventEmitter.addListener(MidiModule.EVENT_MIDIPORT_ONSTATECHANGE + this.id, eventData => {
      this[packagePrivate.mergeData](eventData);
      if (typeof this.onstatechange === 'function') {
        this.onstatechange(new MIDIConnectionEvent(midiAccess, this));
      }
    });
    MidiModule.MIDIPort_setOnStateChange(this.id, true);
  }

  get type () {
    return this[priv.type];
  }

  get state () {
    return this[priv.state];
  }

  get connection () {
    return this[priv.connection];
  }

  get id () {
    return this[priv.id];
  }

  get manufacturer () {
    return this[priv.manufacturer];
  }

  get name () {
    return this[priv.name];
  }

  get version () {
    return this[priv.version];
  }

  async close () {
    const data = await MidiModule.MIDIPort_close(this.id);
    this[packagePrivate.mergeData](data);
    return this;
  }

  async open () {
    const data = await MidiModule.MIDIPort_open(this.id);
    this[packagePrivate.mergeData](data);
    return this;
  }

  set onstatechange (handler) {
    this[priv.onstatechange] = handler;
  }

  get onstatechange () {
    return this[priv.onstatechange];
  }

  [packagePrivate.mergeData] (data) {
    if (data) {
      const {type, state, connection, name, version, id, manufacturer} = data;
      this[priv.type] = type;
      this[priv.state] = state;
      this[priv.connection] = connection;
      this[priv.name] = name;
      this[priv.version] = version;
      this[priv.id] = id;
      this[priv.manufacturer] = manufacturer;
    }
  }
}
