import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-app.js";
import { getAuth, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";

const firebaseConfig = {
    apiKey: "AIzaSyC8gCyhoxqr8Ziwn_Bp_GBgdxexQwsvJM8",
    authDomain: "share-eaac7.firebaseapp.com",
    projectId: "share-eaac7",
    storageBucket: "share-eaac7.firebasestorage.app",
    messagingSenderId: "104312055465",
    appId: "1:104312055465:web:1225b2cc22744124222bb8",
    measurementId: "G-DL5ZBSMEXL"
};

const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

export async function getCurrentUserFirebaseIdToken() {
    return new Promise((resolve, reject) => {
        const unsubscribe = onAuthStateChanged(auth, (user) => {
            unsubscribe();
            if (user) {
                user.getIdToken(true)
                    .then((idToken) => {
                        resolve(idToken);
                    })
                    .catch((error) => {
                        console.error("Firebase ID 토큰 가져오기 실패 (firebase-auth.js):", error);
                        resolve(null); // 또는 reject(error)
                    });
            } else {
                console.log("Firebase 사용자가 로그인되어 있지 않습니다. (firebase-auth.js)");
                resolve(null);
            }
        }, (error) => {
            console.error("onAuthStateChanged 오류 (firebase-auth.js):", error);
            resolve(null); // 또는 reject(error)
        });
    });
}

export { auth };