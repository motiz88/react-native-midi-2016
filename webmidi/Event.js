if (typeof global !== 'undefined' && typeof global.Event !== 'undefined') {
  module.exports = global.Event;
} else {
  // An incomplete DOM Event polyfill
  class Event {
    constructor (type, eventInit = {}) {
      eventInit = {bubbles: false, cancelable: false, ...eventInit, type};
      for (const [key, value] of Object.entries(eventInit)) {
        Object.defineProperty(this, key, {value, enumerable: true});
      }
    }
  }
  module.exports = Event;
}
