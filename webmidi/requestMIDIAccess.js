import MIDIAccess from './MIDIAccess';
import * as packagePrivate from './packagePrivate';
import MidiModule from '../MidiModule';
import { DeviceEventEmitter } from 'react-native';

let midiAccessSingleton = null;
export default async function requestMIDIAccess (options) {
  let midiAccess;
  if (!midiAccessSingleton) {
    midiAccessSingleton = MIDIAccess[packagePrivate.requestMIDIAccess](options);
    midiAccess = await midiAccessSingleton;
    DeviceEventEmitter.addListener(MidiModule.EVENT_MIDIACCESS_ONSTATECHANGE, data => {
      midiAccess[packagePrivate.receiveStateChange](data);
    });
  } else {
    midiAccess = await midiAccessSingleton;
  }
  MidiModule.MIDIAccess_setOnStateChange(true);

  return midiAccess;
}
