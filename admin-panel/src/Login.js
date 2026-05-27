import { useState } from "react";
import { getAuth, signInWithEmailAndPassword } from "firebase/auth";
import { getFirestore, doc, getDoc } from "firebase/firestore";

function Login({ onLogin }) {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const login = async () => {
    try {
      const auth = getAuth();
      const db = getFirestore();

      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const uid = userCredential.user.uid;

      // 🔥 Check if admin
      const adminDoc = await getDoc(doc(db, "admins", uid));

      if (!adminDoc.exists()) {
        alert("Access denied: Not an admin");
        return;
      }

      // ✅ Admin verified
      onLogin(userCredential.user);

    } catch (e) {
      alert(e.message);
    }
  };

  return (
    <div style={{ padding: "20px" }}>
      <h2>Admin Login</h2>

      <input
        placeholder="Email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
      />

      <br /><br />

      <input
        placeholder="Password"
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
      />

      <br /><br />

      <button onClick={login}>
        Login
      </button>
    </div>
  );
}

export default Login;