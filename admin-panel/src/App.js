import { useState } from "react";
import Login from "./Login";
import RequestList from "./components/RequestList";

function App() {

  const [user, setUser] = useState(null);

  // 🔒 If NOT logged in → show login
  if (!user) {
    return <Login onLogin={setUser} />;
  }

  // ✅ If admin logged in → show dashboard
  return (
    <div style={{ padding: "20px" }}>
      <h1>Admin Panel</h1>

      <button
        onClick={() => setUser(null)}
        style={{ marginBottom: "20px" }}
      >
        Logout
      </button>

      <RequestList />
    </div>
  );
}

export default App;