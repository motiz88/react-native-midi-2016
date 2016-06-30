import ReadOnlyMapView from './ReadOnlyMapView';

export default class ReadOnlyMap extends ReadOnlyMapView {
  constructor (...args) {
    super(new Map(...args));
  }
}
