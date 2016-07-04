if (typeof global !== 'undefined' && typeof global.Event !== 'undefined') {
  module.exports = global.Event;
} else {
  module.exports = require('./Event-compat.js');
}
