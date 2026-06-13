const https = require('https');

module.exports = async (req, res) => {
  // CORS Headers configuration
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  // Handle CORS preflight OPTIONS request
  if (req.method === 'OPTIONS') {
    res.status(204).end();
    return;
  }

  // Handle sending the push notification
  if (req.method === 'POST') {
    try {
      // Vercel Serverless automatically parses JSON request bodies
      const payload = req.body || {};

      const oneSignalData = JSON.stringify({
        app_id: payload.app_id || 'ea563567-0403-4d31-af1a-235130fbbd6b',
        included_segments: payload.included_segments || ['Subscribed Users'],
        headings: payload.headings,
        contents: payload.contents
      });

      // Authorization API Key header
      const authHeader = req.headers['authorization'] || 'Key os_v2_app_5jldkzyeangtdly2enitb655npnnjeoeuekupbfvlcx2gdxqkoakr2tewk6e667qtlgencloxim5gfb5w7634qkoa7fhg2qyygjtcji';

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
        let responseBody = '';
        oneSignalRes.on('data', chunk => {
          responseBody += chunk;
        });
        oneSignalRes.on('end', () => {
          res.status(oneSignalRes.statusCode).send(responseBody);
        });
      });

      oneSignalReq.on('error', err => {
        res.status(500).json({ error: err.message });
      });

      oneSignalReq.write(oneSignalData);
      oneSignalReq.end();

    } catch (err) {
      res.status(400).json({ error: 'Invalid payload execution' });
    }
  } else {
    res.status(404).json({ error: 'Not Found' });
  }
};
