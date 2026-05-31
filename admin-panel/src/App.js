import { useState, useEffect } from "react";
import Login from "./Login";
import RequestList from "./components/RequestList";

function App() {
  const [user, setUser] = useState(null);
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem("lbo-admin-theme") || "dark";
  });
  const [toasts, setToasts] = useState([]);

  // Apply theme to document
  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
    localStorage.setItem("lbo-admin-theme", theme);
  }, [theme]);

  // Toast handler
  const showToast = (message, type = "info") => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => {
      setToasts((prev) => prev.filter((t) => t.id !== id));
    }, 4000);
  };

  const toggleTheme = () => {
    setTheme((t) => (t === "light" ? "dark" : "light"));
    showToast(`Switched to ${theme === "light" ? "Dark" : "Light"} Mode`, "info");
  };

  const handleLogout = () => {
    setUser(null);
    showToast("Logged out successfully", "info");
  };

  // Render Toasts
  const renderToasts = () => (
    <div className="lbo-toast-container">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`lbo-toast lbo-toast-${toast.type} animate-slide-up`}
        >
          {toast.type === "success" && (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: "var(--color-success)" }}>
              <polyline points="20 6 9 17 4 12"></polyline>
            </svg>
          )}
          {toast.type === "error" && (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: "var(--color-error)" }}>
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
          )}
          {toast.type === "info" && (
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" style={{ color: "var(--color-info)" }}>
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
          )}
          <span>{toast.message}</span>
        </div>
      ))}
    </div>
  );

  // 🔒 If NOT logged in → show login screen
  if (!user) {
    return (
      <>
        <Login onLogin={(u) => {
          setUser(u);
          showToast(`Welcome back, ${u.email}`, "success");
        }} theme={theme} toggleTheme={toggleTheme} showToast={showToast} />
        {renderToasts()}
      </>
    );
  }

  // Admin user initials for branding avatar
  const adminInitial = user.email ? user.email.charAt(0).toUpperCase() : "A";

  // ✅ If admin logged in → show dashboard
  return (
    <div style={{ minHeight: "100vh", display: "flex", flexDirection: "column" }}>
      {/* ====================================================================
         PREMIUM LBO NAV HEADER
         ==================================================================== */}
      <header className="glass-panel" style={{
        position: "sticky",
        top: 0,
        zIndex: 100,
        padding: "14px 24px",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        borderBottom: "1px solid var(--border-color)",
        transition: "all var(--transition-normal)"
      }}>
        {/* Brand Logo */}
        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          <img 
            src="/logo.png" 
            alt="LBO Logo" 
            style={{ 
              height: "38px", 
              width: "38px",
              borderRadius: "50%",
              objectFit: "cover",
              border: "1.5px solid var(--border-color)",
              backgroundColor: "white",
              padding: "1px"
            }} 
          />
          <div style={{ display: "flex", flexDirection: "column" }}>
            <span style={{
              fontWeight: 800,
              fontSize: "1.05rem",
              letterSpacing: "-0.03em",
              lineHeight: 1.1,
              color: "var(--color-primary)"
            }}>
              LBO MARKETPLACE
            </span>
            <span className="lbo-badge lbo-badge-pending" style={{
              fontSize: "0.6rem",
              padding: "1px 6px",
              marginTop: "2px",
              alignSelf: "flex-start",
              fontWeight: 800
            }}>
              Console Admin
            </span>
          </div>
        </div>

        {/* Header Actions */}
        <div style={{ display: "flex", alignItems: "center", gap: "16px" }}>
          {/* Theme Toggler */}
          <button
            onClick={toggleTheme}
            className="lbo-btn lbo-btn-secondary"
            title={`Switch to ${theme === "light" ? "Dark" : "Light"} Mode`}
            style={{
              width: "40px",
              height: "40px",
              padding: 0,
              borderRadius: "50%",
              justifyContent: "center"
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

          {/* Admin Account Tag */}
          <div style={{
            display: "flex",
            alignItems: "center",
            gap: "8px",
            padding: "6px 12px",
            borderRadius: "var(--radius-full)",
            backgroundColor: "var(--bg-input)",
            border: "1px solid var(--border-color)",
            fontSize: "0.85rem",
            fontWeight: "600"
          }}>
            <div className="flex-center" style={{
              width: "24px",
              height: "24px",
              borderRadius: "50%",
              backgroundColor: "var(--color-primary)",
              color: "var(--bg-card)",
              fontSize: "0.75rem",
              fontWeight: "700"
            }}>
              {adminInitial}
            </div>
            <span style={{ color: "var(--color-text-secondary)", maxWidth: "150px", overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
              {user.email}
            </span>
          </div>

          {/* Logout button */}
          <button
            onClick={handleLogout}
            className="lbo-btn lbo-btn-primary"
            style={{
              padding: "8px 16px",
              fontSize: "0.85rem",
              height: "40px"
            }}
          >
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
              <polyline points="16 17 21 12 16 7"></polyline>
              <line x1="21" y1="12" x2="9" y2="12"></line>
            </svg>
            <span>Sign Out</span>
          </button>
        </div>
      </header>

      {/* Main Dashboard Space */}
      <main style={{ flex: 1, padding: "40px 24px" }} className="animate-fade-in">
        <RequestList showToast={showToast} theme={theme} />
      </main>

      {/* Footer */}
      <footer style={{
        textAlign: "center",
        padding: "20px",
        fontSize: "0.8rem",
        color: "var(--color-text-muted)",
        borderTop: "1px solid var(--border-color)",
        transition: "all var(--transition-normal)"
      }}>
        © {new Date().getFullYear()} LBO Marketplace. All rights reserved. Admin Console Platform v1.1.0
      </footer>

      {renderToasts()}
    </div>
  );
}

export default App;