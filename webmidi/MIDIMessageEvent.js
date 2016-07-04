'use strict';
import Event from './Event';

export default class MIDIMessageEvent extends Event {
  constructor (port, data, receivedTime) {
    super('midimessage', {
      bubbles: false,
      cancelBubble: false,
      cancelable: false,
      currentTarget: port,
      data: data,
      defaultPrevented: false,
      eventPhase: 0,
      path: [],
      receivedTime: receivedTime,
      returnValue: true,
      srcElement: port,
      target: port,
      timeStamp: receivedTime
    });
  }
}
