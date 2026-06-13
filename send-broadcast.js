const https = require('https');

// Extract title and message from command line arguments
const args = process.argv.slice(2);
const title = args[0] || "LBO Announcement";
const message = args[1] || "Hello everyone, check out our new update!";

const appId = "ea563567-0403-4d31-af1a-235130fbbd6b";
const restApiKey = "os_v2_app_5jldkzyeangtdly2enitb655npnnjeoeuekupbfvlcx2gdxqkoakr2tewk6e667qtlgencloxim5gfb5w7634qkoa7fhg2qyygjtcji";

const data = JSON.stringify({
  app_id: appId,
  included_segments: ["Subscribed Users"],
  headings: { en: title },
  contents: { en: message }
});

const options = {
  hostname: 'api.onesignal.com',
  port: 443,
  path: '/api/v1/notifications',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json; charset=utf-8',
    'Authorization': `Key ${restApiKey}`
  }
};

console.log(`\n==================================================`);
console.log(`📣 Sending OneSignal Broadcast Notification...`);
console.log(`==================================================`);
console.log(`Title   : "${title}"`);
console.log(`Message : "${message}"`);
console.log(`--------------------------------------------------`);

const req = https.request(options, (res) => {
  let responseBody = '';

  res.on('data', (chunk) => {
    responseBody += chunk;
  });

  res.on('end', () => {
    console.log(`Status Code : ${res.statusCode}`);
    if (res.statusCode === 200) {
      console.log('Result      : Success! Broadcast sent to all users.');
    } else {
      console.error('Result      : Failed to send broadcast.');
    }
    console.log('Response    :', responseBody);
    console.log(`==================================================\n`);
  });
});

req.on('error', (e) => {
  console.error(`Request Error: ${e.message}`);
});

req.write(data);
req.end();
