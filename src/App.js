import { useState } from "react";
import Login from "./Login";
import RequestList from "./components/RequestList";

/*
  ============================================================
  COMPONENT: App
  PURPOSE: Root application component and Dashboard Layout.
  
  CHANGE LOG:
  PAST:   - Dark theme with purple accents.
  PRESENT: - White background, black borders, black buttons.
           - Logo centered in the dashboard.
           - Circle avatars preserved.
  
  DEPENDENCIES:
  - Login: Auth gate component.
  - RequestList: Main feature component.
  - index.css: Design system variables.
  ============================================================
*/

function App() {
  const [user, setUser] = useState(null);

  if (!user) {
    return <Login onLogin={setUser} />;
  }

  const styles = {
    layout: {
      display: 'flex',
      minHeight: '100vh',
      background: 'var(--color-bg-primary)',
    },
    sidebar: {
      width: 'var(--sidebar-width)',
      background: 'var(--color-sidebar)',
      borderRight: '2px solid var(--color-border)',
      display: 'flex',
      flexDirection: 'column',
      padding: 'var(--space-xl) var(--space-md)',
      position: 'fixed',
      height: '100vh',
      left: 0,
      top: 0,
      zIndex: 100,
    },
    brand: {
      marginBottom: 'var(--space-2xl)',
      padding: '0 var(--space-md)',
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: 'var(--space-sm)',
    },
    logoPlaceholder: {
      width: '60px',
      height: '60px',
      background: '#000',
      borderRadius: '50%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontSize: '10px',
      fontWeight: 'bold',
      marginBottom: 'var(--space-sm)',
    },
    brandName: {
      fontSize: 'var(--font-size-lg)',
      fontWeight: '800',
      color: '#000',
      letterSpacing: '1px',
      textTransform: 'uppercase',
    },
    nav: {
      flex: 1,
    },
    navItem: {
      display: 'flex',
      alignItems: 'center',
      padding: 'var(--space-md)',
      color: '#000',
      background: 'var(--color-sidebar-active)',
      border: '2px solid #000',
      borderRadius: '0px',
      fontWeight: '700',
      marginBottom: 'var(--space-sm)',
      cursor: 'default',
      textTransform: 'uppercase',
      fontSize: 'var(--font-size-sm)',
    },
    logoutBtn: {
      display: 'flex',
      alignItems: 'center',
      padding: 'var(--space-md)',
      color: '#000',
      background: 'transparent',
      border: '2px solid #000',
      borderRadius: '0px',
      fontWeight: '700',
      cursor: 'pointer',
      transition: 'all var(--transition-fast)',
      justifyContent: 'center',
      textTransform: 'uppercase',
      letterSpacing: '1px',
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
      borderBottom: '2px solid #000',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 var(--space-2xl)',
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
    headerLogoBox: {
        width: '40px',
        height: '40px',
        background: '#000',
        borderRadius: '50%',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        color: '#fff',
        fontSize: '8px',
        fontWeight: 'bold',
    },
    headerTitle: {
      fontSize: 'var(--font-size-md)',
      fontWeight: '800',
      textTransform: 'uppercase',
      letterSpacing: '1px',
    },
    userBadge: {
      display: 'flex',
      alignItems: 'center',
      gap: 'var(--space-sm)',
      fontSize: 'var(--font-size-sm)',
      color: '#000',
      fontWeight: '600',
    },
    avatar: {
      width: '32px',
      height: '32px',
      borderRadius: '50%',
      background: '#000',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontWeight: '700',
      fontSize: '12px',
      border: '1px solid #000',
    },
    content: {
      padding: 'var(--space-2xl)',
      maxWidth: '1200px',
      margin: '0 auto',
      width: '100%',
    }
  };

  return (
    <div style={styles.layout}>
      {/* Sidebar Navigation */}
      <aside style={styles.sidebar}>
        <div style={styles.brand}>
          <div style={styles.logoPlaceholder}>LOGO</div>
          <div style={styles.brandName}>LBO ADMIN</div>
        </div>
        
        <div style={styles.nav}>
          <div style={styles.navItem}>
            <span>Dashboard</span>
          </div>
        </div>

        <button
          onClick={() => setUser(null)}
          style={styles.logoutBtn}
          onMouseOver={(e) => { e.target.style.background = '#000'; e.target.style.color = '#fff'; }}
          onMouseOut={(e) => { e.target.style.background = 'transparent'; e.target.style.color = '#000'; }}
        >
          Logout
        </button>
      </aside>

      {/* Main Content Area */}
      <main style={styles.main}>
        <header style={styles.header}>
          <div style={styles.headerTitle}>Provider Management</div>
          
          <div style={styles.headerLogo}>
             <div style={styles.headerLogoBox}>LOGO</div>
             <span style={{fontWeight: '800', fontSize: '14px'}}>LBO</span>
          </div>

          <div style={styles.userBadge}>
            <span>{user.email}</span>
            <div style={styles.avatar}>AD</div>
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