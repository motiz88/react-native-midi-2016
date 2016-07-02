import requestMIDIAccess from '../../../webmidi/requestMIDIAccess';
import { expect } from 'chai';
import MidiModule from '../../mocks/MidiModule';
import { spy } from 'sinon';

const portInit = {
  in1: {
    type: 'input',
    id: 'in1',
    manufacturer: 'motiz88',
    state: 'connected',
    connection: 'closed',
    name: 'Input 1',
    version: '1.2.3'
  },
  out1: {
    type: 'output',
    id: 'out1',
    manufacturer: 'motiz88',
    state: 'connected',
    connection: 'closed',
    name: 'Output 1',
    version: '1.2.3'
  }
};

describe('requestMIDIAccess', () => {
  it('should exist', () => {
    expect(requestMIDIAccess).to.be.a('function');
  });
  describe('MIDIAccess', () => {
    let midiAccess;
    beforeEach(async () => {
      MidiModule.mock.reset();
      MidiModule.mock.data.inputs.in1 = portInit.in1;
      MidiModule.mock.data.outputs.out1 = portInit.out1;
      midiAccess = await requestMIDIAccess();
    });
    describe('#inputs', () => {
      it('should exist', () => {
        midiAccess.should.have.property('inputs')
          .which.is.an('object');
      });
      it('should be a readonly maplike object', () => {
        const { inputs } = midiAccess;
        assertReadonlyMaplike(inputs);
      });
      it('cannot be reassigned', () => {
        const value = midiAccess.inputs;
        expect(() => { midiAccess.inputs = new Map(); }).to.throw;
        midiAccess.should.have.property('inputs', value);
      });
      describe('mocked input', () => {
        let in1;
        before(() => {
          in1 = midiAccess.inputs.get('in1');
        });
        it('should exist', () => {
          in1.should.be.an('object')
            .with.deep.match({
              type: 'input',
              id: 'in1',
              manufacturer: 'motiz88',
              state: 'connected',
              connection: 'closed',
              name: 'Input 1',
              version: '1.2.3'
            });
        });
        testMIDIPortCommon(() => in1);
        describe('#onmidimessage', () => {
          let handler;
          beforeEach(() => {
            handler = spy();
          });
          it('should cause implicit open when assigned', async () => {
            in1.connection.should.equal('closed');
            in1.onmidimessage = handler;
            await turn();
            in1.connection.should.equal('open');
          });
          it('should fire when a message is received', async () => {
            in1.onmidimessage = handler;
            await turn();

            MidiModule.mock.sendToInput('in1', [0x90, 0x00], 10);
            handler.should.have.been.calledOnce;
            const event = handler.firstCall.args[0];
            assertEventObject(event);
            event.should.deep.match({data: new Uint8Array([0x90, 0x00]), timeStamp: 10});
          });
        });
      });
    });
    describe('#outputs', () => {
      it('should exist', () => {
        midiAccess.should.have.property('outputs')
          .which.is.an('object');
      });
      it('should be a readonly maplike object', () => {
        const { outputs } = midiAccess;
        assertReadonlyMaplike(outputs);
      });
      it('cannot be reassigned', () => {
        const value = midiAccess.outputs;
        expect(() => { midiAccess.outputs = new Map(); }).to.throw;
        midiAccess.should.have.property('outputs', value);
      });
      describe('mocked output', () => {
        let out1;
        before(() => {
          out1 = midiAccess.outputs.get('out1');
        });
        it('should exist', () => {
          out1.should.be.an('object')
            .with.deep.match({
              type: 'output',
              id: 'out1',
              manufacturer: 'motiz88',
              state: 'connected',
              connection: 'closed',
              name: 'Output 1',
              version: '1.2.3'
            });
        });
        testMIDIPortCommon(() => out1);
        describe('#send', () => {
          beforeEach(() => {
            spy(MidiModule, 'MIDIOutput_send');
          });
          afterEach(() => {
            MidiModule.MIDIOutput_send.restore();
          });
          it('should send message to native module with implicit open', async () => {
            out1.connection.should.equal('closed');
            out1.send([0x90, 0x00], 10);
            await turn();
            MidiModule.MIDIOutput_send.should.have.been.calledOnce
              .and.calledWithExactly('out1', [0x90, 0x00], 10);
            out1.connection.should.equal('open');
          });
        });
        describe('#clear', () => {
          beforeEach(() => {
            spy(MidiModule, 'MIDIOutput_clear');
          });
          afterEach(() => {
            MidiModule.MIDIOutput_clear.restore();
          });
          it('should delegate to native module', async () => {
            out1.clear();
            await turn();
            MidiModule.MIDIOutput_clear.should.have.been.calledOnce
              .and.calledWithExactly('out1');
          });
        });
      });
    });
    describe('#sysexEnabled', () => {
      it('should exist', () => midiAccess.should.have.property('sysexEnabled').which.is.a('boolean'));
      it('cannot be reassigned', () => {
        const value = midiAccess.sysexEnabled;
        expect(() => { midiAccess.sysexEnabled = !midiAccess.sysexEnabled; }).to.throw;
        midiAccess.should.have.property('sysexEnabled', value);
      });
    });
    describe('#onstatechange', () => {
      let handler, in1Handler, out1Handler;
      beforeEach(async () => {
        handler = spy();
        in1Handler = spy();
        out1Handler = spy();
        midiAccess.inputs.get('in1').onstatechange = in1Handler;
        midiAccess.outputs.get('out1').onstatechange = out1Handler;
        midiAccess.should.have.property('onstatechange'); // even before we set it
        midiAccess.onstatechange = handler;
        await turn();
      });
      it('should exist and can be changed', () => {
        midiAccess.should.have.property('onstatechange', handler);
      });
      describe('when an input changes state', () => {
        beforeEach(async () => {
          MidiModule.mock.mergePort('in1', { state: 'disconnected' });
          await turn();
        });
        it('should be fired', () => {
          handler.should.have.been.calledOnce;
          const event = handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.equal(midiAccess.inputs.get('in1'));
        });
        it('should be fired on input port', () => {
          in1Handler.should.have.been.calledOnce;
          out1Handler.should.not.have.been.called;
          const event = in1Handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.equal(midiAccess.inputs.get('in1'));
        });
      });
      describe('when an input is removed', () => {
        beforeEach(async () => {
          MidiModule.mock.mergePort('in1', undefined);
          await turn();
        });
        afterEach(async () => {
          MidiModule.mock.mergeOrAddPort('in1', portInit.in1);
          await turn();
        });
        it('should be fired', async () => {
          handler.should.have.been.calledOnce;
          const event = handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.deep.match({id: 'in1'});
        });
        it('should be fired on input port', async () => {
          in1Handler.should.have.been.calledOnce;
          const event = in1Handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.deep.match({id: 'in1'});
        });
        it('the port should disappear from .inputs', () => {
          midiAccess.inputs.has('in1').should.be.false;
        });
      });
      describe('when an output changes state', () => {
        beforeEach(async () => {
          MidiModule.mock.mergePort('out1', { state: 'disconnected' });
          await turn();
        });
        it('should be fired', async () => {
          handler.should.have.been.calledOnce;
          const event = handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.equal(midiAccess.outputs.get('out1'));
        });
        it('should be fired on output port', () => {
          out1Handler.should.have.been.calledOnce;
          in1Handler.should.not.have.been.called;
          const event = out1Handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.equal(midiAccess.outputs.get('out1'));
        });
      });
      describe('when an output is removed', () => {
        beforeEach(async () => {
          MidiModule.mock.mergePort('out1', undefined);
          await turn();
        });
        afterEach(async () => {
          MidiModule.mock.mergeOrAddPort('out1', portInit.out1);
          await turn();
        });
        it('should be fired', async () => {
          handler.should.have.been.calledOnce;
          const event = handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.deep.match({id: 'out1'});
        });
        it('should be fired on output port', async () => {
          out1Handler.should.have.been.calledOnce;
          const event = out1Handler.firstCall.args[0];
          assertEventObject(event);
          event.type.should.equal('statechange');
          event.port.should.deep.match({id: 'out1'});
        });
        it('the port should disappear from .outputs', () => {
          midiAccess.outputs.has('out1').should.be.false;
        });
      });
    });
  });
});

function assertReadonlyMaplike (object) {
  object.should.respondTo('entries');
  object.should.respondTo('forEach');
  object.should.respondTo('get');
  object.should.respondTo('has');
  object.should.respondTo('keys');
  object.should.respondTo('values');
  object.should.respondTo(Symbol.iterator);
  object.should.have.property('size')
    .which.is.a('number');
  expect(() => {
    object.set('dummy', {});
  }).to.throw;
  expect(() => {
    object.clear();
  }).to.throw;
  expect(() => {
    object.remove('dummy');
  }).to.throw;

  // Let's just call these functions to make sure they're safe.
  object.entries();
  object.keys();
  object.values();
  for (/*eslint no-unused-vars: 0*/ const a of object) {}
  object.forEach(() => {});
}

function testMIDIPortCommon (getPort) {
  it('#open(), #close()', async () => {
    const port = getPort();
    let returnedPort;
    returnedPort = await port.open();
    returnedPort.should.equal(port);
    returnedPort.should.deep.match({ connection: 'open' });

    returnedPort = await port.close();
    returnedPort.should.equal(port);
    returnedPort.should.deep.match({ connection: 'closed' });
  });
}

function assertEventObject (event) {
  event.should.be.an('object')
    .and.contain.keys(['bubbles', 'cancelable', 'currentTarget', 'defaultPrevented',
      'eventPhase', 'target', 'timeStamp', 'type']);
}

function delay (time = 0) {
  return new Promise(resolve => setTimeout(resolve, time));
}

function turn () {
  return delay(0);
}
