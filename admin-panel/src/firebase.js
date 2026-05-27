import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";
import { getAuth } from "firebase/auth";

// 🔥 YOUR CONFIG (KEEP SAME)
const firebaseConfig = {
  apiKey: "AIzaSyBTyzvcdB1Ads88Dhtq074CHwxPZndyNaU",
  authDomain: "celebration-wall-d5f34.firebaseapp.com",
  databaseURL: "https://celebration-wall-d5f34-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId: "celebration-wall-d5f34",
  storageBucket: "celebration-wall-d5f34.firebasestorage.app",
  messagingSenderId: "882406731041",
  appId: "1:882406731041:web:e391ef5e4acb07d702a260",
  measurementId: "G-LSR9TP95G1"
};

// 🔥 INIT APP
const app = initializeApp(firebaseConfig);

// 🔥 EXPORT THESE (CRITICAL)
export const db = getFirestore(app);
export const auth = getAuth(app);