import 'core-js/fn/object/entries';

import chai from 'chai';
import chaiDeepMatch from 'chai-deep-match';
import sinonChai from 'sinon-chai';

import mockery from 'mockery';
chai.should();
chai.use(chaiDeepMatch);
chai.use(sinonChai);

import reactNativeMock from './mocks/react-native';
import midiModuleMock from './mocks/MidiModule';
mockery.enable({
  warnOnUnregistered: false
});
mockery.registerMock('react-native', reactNativeMock);
mockery.registerMock('../MidiModule', midiModuleMock);
mockery.registerMock('../MidiIos', midiModuleMock);

