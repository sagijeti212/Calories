const CACHE = 'ccache-v1';
const ASSETS = [
  '/', '/index.html',
  '/assets/style.css',
  '/assets/app.js',
  '/assets/i18n/en.json',
  '/assets/i18n/ru.json',
  '/assets/foods.json',
  '/assets/icons/icon-192.png',
  '/assets/icons/icon-512.png'
];
self.addEventListener('install', (e)=>{
  e.waitUntil(caches.open(CACHE).then(c=>c.addAll(ASSETS)));
});
self.addEventListener('activate', (e)=>{
  e.waitUntil(self.clients.claim());
});
self.addEventListener('fetch', (e)=>{
  e.respondWith(caches.match(e.request).then(res=> res || fetch(e.request)));
});
