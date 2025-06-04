const path = require('path');

module.exports = {
  // ... otras configuraciones existentes
  resolve: {
    fallback: {
      http: require.resolve('stream-http'),
      https: require.resolve('https-browserify'),
      stream: require.resolve('stream-browserify'),
      crypto: require.resolve('crypto-browserify'),
      assert: require.resolve('assert'),
      url: require.resolve('url'),
      util: require.resolve('util'),
    },
  },
  // ... otras configuraciones existentes
};
