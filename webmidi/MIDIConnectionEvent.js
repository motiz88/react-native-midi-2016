'use strict';
import Event from './Event';

export default class MIDIConnectionEvent extends Event {
  constructor (midiAccess, port) {
    super('statechange', {
      bubbles: false,
      cancelBubble: false,
      cancelable: false,
      currentTarget: midiAccess,
      defaultPrevented: false,
      eventPhase: 0,
      path: [],
      port: port,
      returnValue: true,
      srcElement: midiAccess,
      target: midiAccess,
      timeStamp: Date.now()
    });
  }
}
