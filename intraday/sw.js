const CACHE="intraday-scanner-v2";
self.addEventListener("install",e=>e.waitUntil(caches.open(CACHE).then(c=>c.addAll([
  "/Attendance-Salary-App/intraday/",
  "/Attendance-Salary-App/intraday/index.html",
  "/Attendance-Salary-App/intraday/manifest.webmanifest",
  "/Attendance-Salary-App/intraday/icon.svg"
]))));
self.addEventListener("activate",e=>e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k.startsWith("intraday-scanner-")&&k!==CACHE).map(k=>caches.delete(k))))));
self.addEventListener("fetch",e=>{
  if(e.request.url.includes("query1.finance.yahoo.com")||e.request.url.includes("corsproxy.io"))return;
  e.respondWith(caches.match(e.request).then(r=>r||fetch(e.request)));
});