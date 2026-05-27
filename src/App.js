import { useState } from "react";
import Login from "./Login";
import RequestList from "./components/RequestList";

/*
  ============================================================
  COMPONENT: App
  PURPOSE: Root application component and Dashboard Layout.
  
  CHANGE LOG:
  - Reduced dimensions for a more MINIMAL look.
  - Smaller sidebar, header, and logo boxes.
  - Refined padding and compact user badge.
  ============================================================
*/

function App() {
  const [user, setUser] = useState(null);

  // CHANGE LOGO MP4 PATH HERE
  const LOGO_VIDEO_URL = "/logo.mp4";

  const getAvatarColor = (name) => {
    if (!name) return "#000000";
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const c = (hash & 0x00FFFFFF).toString(16).toUpperCase();
    return "#" + "00000".substring(0, 6 - c.length) + c;
  };

  if (!user) {
    return <Login onLogin={setUser} />;
  }

  const styles = {
    layout: {
      display: 'flex',
      minHeight: '100vh',
      background: '#ffffff',
    },
    sidebar: {
      width: 'var(--sidebar-width)',
      background: '#ffffff',
      borderRight: '1.5px solid #000000',
      display: 'flex',
      flexDirection: 'column',
      padding: 'var(--space-lg) var(--space-md)',
      position: 'fixed',
      height: '100vh',
      left: 0,
      top: 0,
      zIndex: 100,
    },
    brand: {
      marginBottom: 'var(--space-xl)',
      padding: 'var(--space-md)',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: 'var(--space-xs)',
      background: '#ffffff',
      borderRadius: 'var(--radius-md)',
      border: '1.5px solid #000000',
    },
    logoVideo: {
      width: '50px', // Reduced from 80px
      height: '50px',
      objectFit: 'cover',
      borderRadius: 'var(--radius-sm)',
      backgroundColor: '#000',
    },
    brandName: {
      fontSize: '14px', // Reduced from 18px
      fontWeight: '900',
      color: '#000',
      letterSpacing: '0.05em',
      textTransform: 'uppercase',
    },
    nav: {
      flex: 1,
    },
    navItem: {
      display: 'flex',
      alignItems: 'center',
      padding: '12px 16px', // Reduced from 18px
      color: '#fff',
      background: '#000000',
      borderRadius: 'var(--radius-md)',
      fontWeight: '800',
      marginBottom: 'var(--space-sm)',
      cursor: 'default',
      textTransform: 'uppercase',
      fontSize: '12px', // Reduced from 14px
      letterSpacing: '0.05em',
      border: '1.5px solid #000000',
    },
    logoutBtn: {
      display: 'flex',
      alignItems: 'center',
      padding: '12px 16px', // Reduced from 18px
      color: '#ffffff',
      background: '#000000',
      border: '1.5px solid #000000',
      borderRadius: 'var(--radius-md)',
      fontWeight: '800',
      cursor: 'pointer',
      transition: 'all var(--transition-fast)',
      justifyContent: 'center',
      textTransform: 'uppercase',
      letterSpacing: '0.05em',
      fontSize: '12px',
    },
    main: {
      flex: 1,
      marginLeft: 'var(--sidebar-width)',
      display: 'flex',
      flexDirection: 'column',
    },
    header: {
      height: 'var(--header-height)',
      background: '#ffffff',
      borderBottom: '1.5px solid #000000',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 var(--space-xl)',
      position: 'sticky',
      top: 0,
      zIndex: 90,
    },
    headerLogo: {
      position: 'absolute',
      left: '50%',
      transform: 'translateX(-50%)',
      display: 'flex',
      alignItems: 'center',
      gap: 'var(--space-sm)',
    },
    headerLogoVideo: {
        width: '40px', // Reduced from 60px
        height: '40px',
        objectFit: 'cover',
        borderRadius: 'var(--radius-sm)',
        backgroundColor: '#000',
        border: '1px solid #000',
    },
    headerTitle: {
      fontSize: '14px', // Reduced from 20px
      fontWeight: '900',
      textTransform: 'uppercase',
      letterSpacing: '0.05em',
    },
    userBadge: {
      display: 'flex',
      alignItems: 'center',
      gap: 'var(--space-sm)',
      fontSize: '12px',
      color: '#000',
      fontWeight: '700',
      padding: '6px 14px',
      background: '#ffffff',
      borderRadius: 'var(--radius-full)',
      border: '1.5px solid #000000',
    },
    avatar: {
      width: '28px', // Reduced from 40px
      height: '28px',
      borderRadius: '50%',
      background: getAvatarColor(user.email),
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontWeight: '900',
      fontSize: '10px',
      border: '1.5px solid #000',
    },
    content: {
      padding: 'var(--space-xl)',
      maxWidth: '1200px',
      margin: '0',
      width: '100%',
    }
  };

  return (
    <div style={styles.layout}>
      <aside style={styles.sidebar}>
        <div style={styles.brand}>
          <video 
            src={LOGO_VIDEO_URL} 
            style={styles.logoVideo}
            autoPlay 
            loop 
            muted 
            playsInline
          />
          <div style={styles.brandName}>LBO ADMIN</div>
        </div>
        
        <div style={styles.nav}>
          <div style={styles.navItem}>
            <span style={{marginRight: '8px'}}>📊</span>
            <span>Dashboard</span>
          </div>
        </div>

        <button
          onClick={() => setUser(null)}
          style={styles.logoutBtn}
          onMouseOver={(e) => { e.target.style.background = '#222'; e.target.style.borderColor = '#222'; }}
          onMouseOut={(e) => { e.target.style.background = '#000'; e.target.style.borderColor = '#000'; }}
        >
          Logout
        </button>
      </aside>

      <main style={styles.main}>
        <header style={styles.header}>
          <div style={styles.headerTitle}>Overview</div>
          
          <div style={styles.headerLogo}>
             <video 
                src={LOGO_VIDEO_URL} 
                style={styles.headerLogoVideo}
                autoPlay 
                loop 
                muted 
                playsInline
             />
             <span style={{fontWeight: '900', fontSize: '18px', letterSpacing: '0.05em'}}>LBO</span>
          </div>

          <div style={styles.userBadge}>
            <span>{user.email}</span>
            <div style={styles.avatar}>
              {user.email.substring(0, 2).toUpperCase()}
            </div>
          </div>
        </header>

        <div style={styles.content}>
          <RequestList />
        </div>
      </main>
    </div>
  );
}

export default App;