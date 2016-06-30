const priv = {
  map: Symbol('map')
};

export default class ReadOnlyMapView {
  constructor (map) {
    this[priv.map] = map;
  }

  get size () {
    return this[priv.map].size;
  }

  entries () {
    return this[priv.map].entries();
  }

  keys () {
    return this[priv.map].keys();
  }

  values () {
    return this[priv.map].values();
  }

  [Symbol.iterator] () {
    return this[priv.map][Symbol.iterator]();
  }

  forEach (...args) {
    return this[priv.map].forEach(...args);
  }

  get (...args) {
    return this[priv.map].get(...args);
  }

  has (...args) {
    return this[priv.map].has(...args);
  }
}
