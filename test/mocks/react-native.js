import { EventEmitter } from 'events';

const emitter = new EventEmitter();

const DeviceEventEmitter = {
  addListener (type, onReceived) {
    emitter.addListener(type, onReceived);
    return {
      remove () {
        emitter.removeListener(type, onReceived);
      }
    };
  },
  mock: {
    emit (...args) {
      return emitter.emit(...args);
    }
  }
};

module.exports = { DeviceEventEmitter };
