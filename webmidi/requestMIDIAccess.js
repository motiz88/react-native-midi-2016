import MIDIAccess from './MIDIAccess';
import * as packagePrivate from './packagePrivate';
import MidiModule from '../MidiModule.android';
import { DeviceEventEmitter } from 'react-native';

let midiAccessSingleton = null;
export default async function requestMIDIAccess (options) {
  if (!midiAccessSingleton) {
    midiAccessSingleton = MIDIAccess[packagePrivate.requestMIDIAccess](options);
  }
  const midiAccess = await midiAccessSingleton;

  DeviceEventEmitter.addListener(MidiModule.EVENT_MIDIACCESS_ONSTATECHANGE, data => {
    midiAccess[packagePrivate.receiveStateChange](data);
  });
  MidiModule.MIDIAccess_setOnStateChange(true);

  return midiAccess;
}
