const CACHE_NAME = "attendance-salary-v3";
const APP_SHELL = ["./", "./index.html", "./manifest.webmanifest"];

// Always activate the newest service worker immediately.
self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => cache.addAll(APP_SHELL))
  );
  self.skipWaiting();
});

// Remove old app caches and take control of open PWA windows.
self.addEventListener("activate", event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys
          .filter(key => key !== CACHE_NAME)
          .map(key => caches.delete(key))
      )
    ).then(() => self.clients.claim())
  );
});

// Network-first: when online, users get the latest GitHub Pages files.
// If offline, the last cached copy is used so the app still works.
self.addEventListener("fetch", event => {
  if (event.request.method !== "GET") return;

  event.respondWith(
    fetch(event.request, { cache: "no-cache" })
      .then(response => {
        const copy = response.clone();
        caches.open(CACHE_NAME).then(cache => cache.put(event.request, copy));
        return response;
      })
      .catch(() =>
        caches.match(event.request).then(
          cached => cached || caches.match("./index.html")
        )
      )
  );
});
