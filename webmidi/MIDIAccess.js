const priv = {
  inputs: Symbol('inputs'),
  outputs: Symbol('outputs'),
  inputsView: Symbol('inputsView'),
  outputsView: Symbol('outputsView'),
  sysexEnabled: Symbol('sysexEnabled'),
  onstatechange: Symbol('onstatechange')
};

import ReadOnlyMapView from './ReadOnlyMapView';
import MidiModule from '../MidiModule';
import MIDIInput from './MIDIInput';
import MIDIOutput from './MIDIOutput';
import MIDIConnectionEvent from './MIDIConnectionEvent';
import * as packagePrivate from './packagePrivate';

export default class MIDIAccess {
  constructor () {
    this[priv.inputs] = new Map();
    this[priv.inputsView] = new ReadOnlyMapView(this[priv.inputs]);
    this[priv.outputs] = new Map();
    this[priv.outputsView] = new ReadOnlyMapView(this[priv.outputs]);
    this[priv.sysexEnabled] = null;
    this[priv.onstatechange] = null;
  }

  get inputs () {
    return this[priv.inputsView];
  }

  get outputs () {
    return this[priv.outputsView];
  }

  get sysexEnabled () {
    return this[priv.sysexEnabled];
  }

  set onstatechange (value) {
    this[priv.onstatechange] = value;
  }

  get onstatechange () {
    return this[priv.onstatechange];
  }

  [packagePrivate.mergeData] (data) {
    if (data) {
      const {inputs, outputs, sysexEnabled} = data;

      for (const [id, input] of Object.entries(inputs)) {
        if (this[priv.inputs].has(id)) {
          this[priv.inputs].get(id)[packagePrivate.mergeData](input);
        } else {
          this[priv.inputs].set(id, new MIDIInput(this, input));
        }
      }
      for (const [id, output] of Object.entries(outputs)) {
        if (this[priv.outputs].has(id)) {
          this[priv.outputs].get(id)[packagePrivate.mergeData](output);
        } else {
          this[priv.outputs].set(id, new MIDIOutput(this, output));
        }
      }
      for (const id of [...this[priv.inputs].keys()]) {
        if (!inputs[id]) this[priv.inputs].delete(id);
      }
      for (const id of [...this[priv.outputs].keys()]) {
        if (!outputs[id]) this[priv.outputs].delete(id);
      }
      this[priv.sysexEnabled] = sysexEnabled;
    }
  }

  static async [packagePrivate.requestMIDIAccess] (options) {
    const self = new MIDIAccess();
    self[packagePrivate.mergeData](await MidiModule.requestMIDIAccess(options));
    return self;
  }

  [packagePrivate.receiveStateChange] (portData) {
    console.log('MIDIAccess received statechange', portData);
    let port = portData;
    if (portData.type === 'input' && this.inputs.has(portData.id)) {
      port = this.inputs.get(portData.id);
    } else if (portData.type === 'output' && this.outputs.has(portData.id)) {
      port = this.outputs.get(portData.id);
    }
    if (typeof port[packagePrivate.mergeData] === 'function') {
      port[packagePrivate.mergeData](portData);
    }
    MidiModule.MIDIAccess_get_data()
      .then(data => {
        this[packagePrivate.mergeData](data);
        if (typeof this.onstatechange === 'function') {
          this.onstatechange(new MIDIConnectionEvent(this, port));
        }
      });
  }
}
