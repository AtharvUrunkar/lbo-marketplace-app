import { useState } from "react";
import { getAuth, signInWithEmailAndPassword } from "firebase/auth";
import { getFirestore, doc, getDoc } from "firebase/firestore";

function Login({ onLogin, theme, toggleTheme, showToast }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const login = async (e) => {
    if (e) e.preventDefault();
    if (!email || !password) {
      showToast("Please enter both email and password.", "error");
      return;
    }

    setLoading(true);
    try {
      const auth = getAuth();
      const db = getFirestore();

      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const uid = userCredential.user.uid;

      // 🔥 Check if admin
      const adminDoc = await getDoc(doc(db, "admins", uid));

      if (!adminDoc.exists()) {
        showToast("Access denied: You are not authorized as an administrator.", "error");
        setLoading(false);
        return;
      }

      // ✅ Admin verified
      onLogin(userCredential.user);
    } catch (e) {
      console.error(e);
      let errorMsg = e.message;
      if (e.code === "auth/invalid-credential") {
        errorMsg = "Invalid email or password. Please try again.";
      } else if (e.code === "auth/user-not-found") {
        errorMsg = "No administrator account found with this email.";
      }
      showToast(errorMsg, "error");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex-center animate-fade-in" style={{
      minHeight: "100vh",
      padding: "24px",
      position: "relative",
      background: "radial-gradient(circle at 50% 50%, var(--bg-card) 0%, var(--bg-app) 100%)",
      transition: "background var(--transition-normal)"
    }}>
      {/* Top Right Floating Theme Toggler for immediate UX delight */}
      <button
        onClick={toggleTheme}
        className="lbo-btn lbo-btn-secondary"
        title={`Switch to ${theme === "light" ? "Dark" : "Light"} Mode`}
        style={{
          position: "absolute",
          top: "24px",
          right: "24px",
          width: "44px",
          height: "44px",
          padding: 0,
          borderRadius: "50%",
          justifyContent: "center",
          boxShadow: "var(--shadow-sm)"
        }}
      >
        {theme === "light" ? (
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
          </svg>
        ) : (
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <circle cx="12" cy="12" r="5"></circle>
            <line x1="12" y1="1" x2="12" y2="3"></line>
            <line x1="12" y1="21" x2="12" y2="23"></line>
            <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
            <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
            <line x1="1" y1="12" x2="3" y2="12"></line>
            <line x1="21" y1="12" x2="23" y2="12"></line>
            <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
            <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
          </svg>
        )}
      </button>

      {/* Main Glass Login Card */}
      <div className="glass-panel animate-scale-in" style={{
        width: "100%",
        maxWidth: "420px",
        borderRadius: "var(--radius-lg)",
        padding: "40px",
        boxShadow: "var(--shadow-lg)",
        transition: "all var(--transition-normal)"
      }}>
        {/* Monogram Brand Header */}
        <div style={{ textAlign: "center", marginBottom: "32px" }}>
          <img 
            src="/logo.png" 
            alt="LBO Logo" 
            style={{ 
              height: "56px", 
              width: "56px",
              borderRadius: "50%",
              objectFit: "cover",
              border: "1.5px solid var(--border-color)",
              backgroundColor: "white",
              padding: "2px",
              margin: "0 auto 16px auto",
              boxShadow: "var(--shadow-sm)",
              display: "block"
            }} 
          />
          <h2 style={{ fontSize: "1.75rem", marginBottom: "8px", fontWeight: "800" }}>LBO Console</h2>
          <p style={{ color: "var(--color-text-secondary)", fontSize: "0.9rem", fontWeight: "500" }}>
            Sign in to manage provider registrations.
          </p>
        </div>

        {/* Credentials Form */}
        <form onSubmit={login} style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
          {/* Email input field */}
          <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
            <label style={{ fontSize: "0.8rem", fontWeight: "700", color: "var(--color-text-secondary)", textTransform: "uppercase", letterSpacing: "0.05em" }}>
              Administrator Email
            </label>
            <div style={{ position: "relative" }}>
              <input
                type="email"
                placeholder="admin@lbo.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="lbo-input"
                style={{ paddingLeft: "44px" }}
                disabled={loading}
              />
              <span style={{ position: "absolute", left: "14px", top: "50%", transform: "translateY(-50%)", color: "var(--color-text-muted)", display: "flex" }}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                  <polyline points="22,6 12,13 2,6"></polyline>
                </svg>
              </span>
            </div>
          </div>

          {/* Password input field */}
          <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
            <label style={{ fontSize: "0.8rem", fontWeight: "700", color: "var(--color-text-secondary)", textTransform: "uppercase", letterSpacing: "0.05em" }}>
              Console Password
            </label>
            <div style={{ position: "relative" }}>
              <input
                placeholder="••••••••"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="lbo-input"
                style={{ paddingLeft: "44px" }}
                disabled={loading}
              />
              <span style={{ position: "absolute", left: "14px", top: "50%", transform: "translateY(-50%)", color: "var(--color-text-muted)", display: "flex" }}>
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                  <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                </svg>
              </span>
            </div>
          </div>

          {/* Action button */}
          <button
            type="submit"
            className="lbo-btn lbo-btn-primary"
            style={{ width: "100%", padding: "14px", marginTop: "10px" }}
            disabled={loading}
          >
            {loading ? (
              <div style={{
                width: "20px",
                height: "20px",
                border: "3px solid var(--border-color)",
                borderTop: "3px solid var(--color-primary)",
                borderRadius: "50%",
                animation: "App-logo-spin 1s linear infinite"
              }} />
            ) : (
              <>
                <span>Secure Sign In</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                  <polyline points="12 5 19 12 12 19"></polyline>
                </svg>
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
}

export default Login;