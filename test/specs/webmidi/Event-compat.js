import Event from '../../../webmidi/Event-compat';
import { expect } from 'chai';

describe('Event polyfill', () => {
  it('should exist', () => {
    expect(Event).to.be.a('function');
  });
  it('should construct an Event successfully', () => {
    const e = new Event('myevent');
    e.should.be.an.instanceOf(Event);
  });
  it('should have type property', () => {
    const e = new Event('myevent');
    e.should.have.property('type', 'myevent');
  });
  it('.type should not be overridden by EventInit', () => {
    const e = new Event('myevent', {type: 'somethingelse'});
    e.should.have.property('type', 'myevent');
  });
  it('should copy props from EventInit', () => {
    const e = new Event('myevent', {foo: 'foo!', bar: 'bar!'});
    e.should.have.property('foo', 'foo!');
    e.should.have.property('bar', 'bar!');
  });
});
