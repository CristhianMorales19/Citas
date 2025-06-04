const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  // Configuración para rutas /api (mantiene el prefijo /api)
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
      // NO eliminamos el prefijo /api para estos endpoints
      logLevel: 'debug',
      onProxyReq: (proxyReq, req, res) => {
        console.log(`API Proxy request: ${req.method} ${req.url}`);
      },
      onProxyRes: (proxyRes, req, res) => {
        console.log(`API Proxy response: ${proxyRes.statusCode} for ${req.method} ${req.url}`);
      },
      onError: (err, req, res) => {
        console.error('API Proxy error:', err);
      }
    })
  );

  // Configuración para rutas sin /api (auth, admin, public, etc.)
  app.use(
    ['/auth', '/admin', '/public', '/pacientes', '/diagnostic', '/dev'],
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
      logLevel: 'debug',
      onProxyReq: (proxyReq, req, res) => {
        console.log(`Direct Proxy request: ${req.method} ${req.url}`);
      },
      onProxyRes: (proxyRes, req, res) => {
        console.log(`Direct Proxy response: ${proxyRes.statusCode} for ${req.method} ${req.url}`);
      },
      onError: (err, req, res) => {
        console.error('Direct Proxy error:', err);
      }
    })
  );
};
