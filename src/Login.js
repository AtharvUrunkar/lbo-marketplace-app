import { useState } from "react";
import { getAuth, signInWithEmailAndPassword } from "firebase/auth";
import { getFirestore, doc, getDoc } from "firebase/firestore";

/*
  ============================================================
  COMPONENT: Login
  PURPOSE: Admin authentication gate.
  
  CHANGE LOG:
  PAST:   - Simple <div> with padding.
          - Raw <input> and <button> elements with default browser styling.
          - No visual hierarchy or branding.
          - Hardcoded <br /> tags for spacing.
  
  PRESENT: - White background, black buttons, black borders.
           - Logo placeholder included.
           - Structured with modern black & white aesthetics.
  
  DEPENDENCIES:
  - firebase/auth: signInWithEmailAndPassword (Existing logic, UNCHANGED)
  - firebase/firestore: doc, getDoc (Existing logic, UNCHANGED)
  - index.css: Uses CSS variables like --color-bg-primary, etc.
  
  REFERENCED BY: src/App.js (Main auth gate)
  ============================================================
*/

function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const login = async () => {
    setIsLoading(true);
    setError("");
    try {
      const auth = getAuth();
      const db = getFirestore();

      const userCredential = await signInWithEmailAndPassword(auth, email, password);
      const uid = userCredential.user.uid;

      const adminDoc = await getDoc(doc(db, "admins", uid));

      if (!adminDoc.exists()) {
        setError("Access denied: Not an admin");
        setIsLoading(false);
        return;
      }

      onLogin(userCredential.user);

    } catch (e) {
      setError(e.message);
      setIsLoading(false);
    }
  };

  const styles = {
    container: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '100vh',
      width: '100vw',
      background: 'var(--color-bg-primary)',
      padding: 'var(--space-md)',
    },
    card: {
      background: '#ffffff',
      padding: 'var(--space-2xl)',
      borderRadius: 'var(--radius-lg)',
      border: '2px solid var(--color-border)',
      boxShadow: 'var(--shadow-lg)',
      width: '100%',
      maxWidth: '400px',
      textAlign: 'center',
    },
    logoContainer: {
      marginBottom: 'var(--space-xl)',
      display: 'flex',
      justifyContent: 'center',
    },
    logo: {
      width: '80px',
      height: '80px',
      background: '#000',
      borderRadius: '50%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontSize: '12px',
      fontWeight: 'bold',
    },
    header: {
      marginBottom: 'var(--space-xl)',
    },
    title: {
      fontSize: 'var(--font-size-2xl)',
      fontWeight: '800',
      color: '#000',
      marginBottom: 'var(--space-xs)',
      textTransform: 'uppercase',
      letterSpacing: '1px',
    },
    subtitle: {
      color: 'var(--color-text-secondary)',
      fontSize: 'var(--font-size-sm)',
    },
    inputGroup: {
      textAlign: 'left',
      marginBottom: 'var(--space-md)',
    },
    label: {
      display: 'block',
      fontSize: 'var(--font-size-xs)',
      color: '#000',
      marginBottom: 'var(--space-xs)',
      textTransform: 'uppercase',
      letterSpacing: '0.05em',
      fontWeight: '700',
    },
    input: {
      width: '100%',
      padding: 'var(--space-md)',
      background: '#ffffff',
      border: '2px solid #000000',
      borderRadius: '0px',
      color: '#000',
      fontSize: 'var(--font-size-base)',
      outline: 'none',
      transition: 'all var(--transition-fast)',
    },
    button: {
      width: '100%',
      padding: 'var(--space-md)',
      background: '#000000',
      color: '#ffffff',
      border: '2px solid #000000',
      borderRadius: '0px',
      fontSize: 'var(--font-size-base)',
      fontWeight: '700',
      cursor: isLoading ? 'not-allowed' : 'pointer',
      marginTop: 'var(--space-md)',
      transition: 'all var(--transition-fast)',
      textTransform: 'uppercase',
      letterSpacing: '1px',
      opacity: isLoading ? 0.7 : 1,
    },
    error: {
      background: '#fff0f0',
      color: '#ff0000',
      padding: 'var(--space-sm) var(--space-md)',
      borderRadius: 'var(--radius-sm)',
      fontSize: 'var(--font-size-sm)',
      marginBottom: 'var(--space-md)',
      border: '1px solid #ff0000',
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.logoContainer}>
          {/* PASTE LOGO HERE */}
          <div style={styles.logo}>LOGO</div>
        </div>

        <div style={styles.header}>
          <h2 style={styles.title}>LBO Admin</h2>
          <p style={styles.subtitle}>Sign in to your account</p>
        </div>

        {error && <div style={styles.error}>{error}</div>}

        <div style={styles.inputGroup}>
          <label style={styles.label}>Email Address</label>
          <input
            style={styles.input}
            placeholder="admin@lbo.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div style={styles.inputGroup}>
          <label style={styles.label}>Password</label>
          <input
            style={styles.input}
            placeholder="••••••••"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button 
          style={styles.button} 
          onClick={login}
          disabled={isLoading}
          onMouseOver={(e) => { if(!isLoading) { e.target.style.background = '#333333'; e.target.style.borderColor = '#333333'; } }}
          onMouseOut={(e) => { if(!isLoading) { e.target.style.background = '#000000'; e.target.style.borderColor = '#000000'; } }}
        >
          {isLoading ? "Authenticating..." : "Sign In"}
        </button>
      </div>
    </div>
  );
}

export default Login;