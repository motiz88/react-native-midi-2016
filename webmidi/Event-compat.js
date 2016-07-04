// An incomplete DOM Event polyfill
export default class Event {
  constructor (type, eventInit = {}) {
    eventInit = {bubbles: false, cancelable: false, ...eventInit, type};
    for (const [key, value] of Object.entries(eventInit)) {
      Object.defineProperty(this, key, {value, enumerable: true});
    }
  }
}
