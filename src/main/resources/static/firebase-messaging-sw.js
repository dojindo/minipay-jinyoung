importScripts('https://www.gstatic.com/firebasejs/10.13.2/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.13.2/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyDWSjysEudIWFIKQkzdJNtFTbUS5JRFFbo",
  authDomain: "minipay-remainder.firebaseapp.com",
  projectId: "minipay-remainder",
  storageBucket: "minipay-remainder.firebasestorage.app",
  messagingSenderId: "668137417319",
  appId: "1:668137417319:web:bd1098d03c3fcaba1642c2",
  measurementId: "G-V2R2T3R350"
});

const messaging = firebase.messaging();

messaging.onBackgroundMessage((payload) => {
  console.log(
      '[firebase-messaging-sw.js] Received background message ',
      payload
  );
  const notificationTitle = payload.notification.title;
  const notificationOptions = {
    body: payload.notification.body
    // icon: payload.icon
  };
  self.registration.showNotification(notificationTitle, notificationOptions);
});