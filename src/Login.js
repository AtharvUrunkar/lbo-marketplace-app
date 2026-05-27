import { useState } from "react";
import { getAuth, signInWithEmailAndPassword } from "firebase/auth";
import { getFirestore, doc, getDoc } from "firebase/firestore";

/*
  ============================================================
  COMPONENT: Login
  PURPOSE: Admin authentication gate.
  
  CHANGE LOG:
  - Reduced dimensions for a more MINIMALIST feel.
  - Smaller card, logo, and refined input spacing.
  ============================================================
*/

function Login({ onLogin }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  const LOGO_VIDEO_URL = "/logo.mp4"; 

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
        setError("Access denied");
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
      background: '#ffffff',
      padding: 'var(--space-md)',
    },
    card: {
      background: '#ffffff',
      padding: 'var(--space-xl)', // Reduced from space-2xl
      borderRadius: 'var(--radius-lg)',
      border: '1.5px solid #000',
      boxShadow: '0 10px 30px rgba(0,0,0,0.05)',
      width: '100%',
      maxWidth: '380px', // Reduced from 440px
      textAlign: 'center',
    },
    logoContainer: {
      marginBottom: 'var(--space-lg)',
      display: 'flex',
      justifyContent: 'center',
    },
    logoVideo: {
      width: '80px', // Reduced from 120px
      height: '80px',
      objectFit: 'cover',
      borderRadius: 'var(--radius-md)',
      backgroundColor: '#000',
    },
    header: {
      marginBottom: 'var(--space-xl)',
    },
    title: {
      fontSize: '20px', // Reduced from 2xl
      fontWeight: '900',
      color: '#000',
      marginBottom: '4px',
      letterSpacing: '-0.02em',
    },
    subtitle: {
      color: 'var(--color-text-secondary)',
      fontSize: '12px',
      fontWeight: '600',
    },
    inputGroup: {
      textAlign: 'left',
      marginBottom: 'var(--space-md)',
    },
    label: {
      display: 'block',
      fontSize: '10px',
      color: '#000',
      marginBottom: '4px',
      textTransform: 'uppercase',
      letterSpacing: '0.05em',
      fontWeight: '800',
    },
    input: {
      width: '100%',
      padding: '12px', // Reduced from 16px
      background: '#ffffff',
      border: '1.5px solid #000000',
      borderRadius: 'var(--radius-md)',
      color: '#000',
      fontSize: '14px',
      outline: 'none',
      transition: 'all var(--transition-fast)',
    },
    button: {
      width: '100%',
      padding: '14px', // Reduced from 18px
      background: '#000000',
      color: '#ffffff',
      border: 'none',
      borderRadius: 'var(--radius-md)',
      fontSize: '14px',
      fontWeight: '800',
      cursor: isLoading ? 'not-allowed' : 'pointer',
      marginTop: 'var(--space-sm)',
      transition: 'all var(--transition-normal)',
      opacity: isLoading ? 0.7 : 1,
    },
    error: {
      background: '#fff0f0',
      color: '#ee5253',
      padding: '10px',
      borderRadius: 'var(--radius-sm)',
      fontSize: '12px',
      marginBottom: 'var(--space-md)',
      border: '1px solid rgba(238,82,83,0.1)',
      fontWeight: '600',
    }
  };

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.logoContainer}>
          <video 
            src={LOGO_VIDEO_URL} 
            style={styles.logoVideo}
            autoPlay 
            loop 
            muted 
            playsInline
          />
        </div>

        <div style={styles.header}>
          <h2 style={styles.title}>Admin Panel</h2>
          <p style={styles.subtitle}>Secure login for LBO Management</p>
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
        >
          {isLoading ? "..." : "Sign In"}
        </button>
      </div>
    </div>
  );
}

export default Login;