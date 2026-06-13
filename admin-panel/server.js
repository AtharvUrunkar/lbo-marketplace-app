const http = require('http');
const https = require('https');

const PORT = 3001;

const server = http.createServer((req, res) => {
  // Set CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  // Log all incoming requests
  console.log(`\n[${new Date().toISOString()}] 📥 Incoming request: ${req.method} ${req.url}`);

  // Handle preflight OPTIONS request
  if (req.method === 'OPTIONS') {
    console.log(`[CORS Preflight] Responding 204 to OPTIONS request.`);
    res.writeHead(204);
    res.end();
    return;
  }

  // Handle POST request to send notification
  if (req.method === 'POST' && req.url === '/send-notification') {
    let body = '';
    req.on('data', chunk => {
      body += chunk.toString();
    });
    req.on('end', () => {
      console.log(`[PROXY] Request Body received: ${body}`);
      try {
        const payload = JSON.parse(body);
        
        // Build OneSignal Request Data
        const oneSignalData = JSON.stringify({
          app_id: payload.app_id || 'ea563567-0403-4d31-af1a-235130fbbd6b',
          included_segments: payload.included_segments || ['Subscribed Users'],
          headings: payload.headings,
          contents: payload.contents
        });

        // Resolve Authorization header (fallback if frontend didn't pass it)
        const authHeader = req.headers['authorization'] || 'Key os_v2_app_5jldkzyeangtdly2enitb655npnnjeoeuekupbfvlcx2gdxqkoakr2tewk6e667qtlgencloxim5gfb5w7634qkoa7fhg2qyygjtcji';

        console.log(`[PROXY] Forwarding to OneSignal...`);
        console.log(`  - Target URL : https://api.onesignal.com/api/v1/notifications`);
        console.log(`  - App ID     : ${payload.app_id || 'ea563567-0403-4d31-af1a-235130fbbd6b'}`);
        console.log(`  - Auth Header: ${authHeader.substring(0, 15)}... [hidden for security]`);

        const options = {
          hostname: 'api.onesignal.com',
          port: 443,
          path: '/api/v1/notifications',
          method: 'POST',
          headers: {
            'Content-Type': 'application/json; charset=utf-8',
            'Authorization': authHeader
          }
        };

        const oneSignalReq = https.request(options, (oneSignalRes) => {
          console.log(`[ONESIGNAL RESPONSE] Status Code: ${oneSignalRes.statusCode}`);
          let responseBody = '';
          oneSignalRes.on('data', chunk => {
            responseBody += chunk;
          });
          oneSignalRes.on('end', () => {
            console.log(`[ONESIGNAL RESPONSE] Body: ${responseBody}`);
            res.writeHead(oneSignalRes.statusCode, { 'Content-Type': 'application/json' });
            res.end(responseBody);
          });
        });

        oneSignalReq.on('error', err => {
          console.error(`[ONESIGNAL ERROR] Request failed: ${err.message}`);
          res.writeHead(500, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: err.message }));
        });

        oneSignalReq.write(oneSignalData);
        oneSignalReq.end();

      } catch (err) {
        console.error(`[JSON ERROR] Failed to parse request body: ${err.message}`);
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON payload' }));
      }
    });
  } else {
    console.log(`[404] Route not found: ${req.url}`);
    res.writeHead(404, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Not Found' }));
  }
});

server.listen(PORT, () => {
  console.log(`==================================================`);
  console.log(`🚀 CORS Proxy Server running on http://localhost:${PORT}`);
  console.log(`==================================================`);
});
