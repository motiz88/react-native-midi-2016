import requestMIDIAccess from './requestMIDIAccess';

if (!global.navigator) {
  global.navigator = {};
}

if (!global.navigator.requestMIDIAccess) {
  global.navigator.requestMIDIAccess = requestMIDIAccess;
}
