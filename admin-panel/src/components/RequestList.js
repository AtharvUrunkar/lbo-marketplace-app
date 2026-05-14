import { useEffect, useState } from "react";
import { db, auth } from "../firebase";
import {
  collection,
  getDocs,
  updateDoc,
  doc,
  query,
  where
} from "firebase/firestore";

function RequestList() {
  const [requests, setRequests] = useState([]);

  useEffect(() => {
    fetchRequests();
  }, []);

  const fetchRequests = async () => {
    const q = query(
      collection(db, "provider_requests"),
      where("status", "==", "PENDING")
    );

    const querySnapshot = await getDocs(q);

    const data = querySnapshot.docs.map(doc => ({
      id: doc.id,
      ...doc.data()
    }));

    setRequests(data);
  };

  // 🔥 DEBUG (REMOVE LATER)
  console.log("ADMIN USER:", auth.currentUser);

  const approve = async (userId, requestId) => {
  try {
    console.log("STEP 1: updating user");

    await updateDoc(doc(db, "users", userId), {
      role: "SERVICE_PROVIDER"
    });

    console.log("STEP 2: updating request");

    await updateDoc(doc(db, "provider_requests", requestId), {
      status: "APPROVED"
    });

    fetchRequests();
  } catch (error) {
    console.error("Approve Error:", error);
  }
};

  const reject = async (requestId) => {
    try {
      await updateDoc(doc(db, "provider_requests", requestId), {
        status: "REJECTED"
      });

      fetchRequests();
    } catch (error) {
      console.error("Reject Error:", error);
    }
  };

  return (
    <div style={{ maxWidth: "800px", margin: "auto" }}>
      <h2 style={{ textAlign: "center" }}>Pending Provider Requests</h2>

      {requests.length === 0 && (
        <p style={{ textAlign: "center" }}>No pending requests</p>
      )}

      {requests.map(req => (
        <div
          key={req.id}
          style={{
            background: "#fff",
            borderRadius: "12px",
            padding: "20px",
            margin: "15px 0",
            boxShadow: "0 4px 10px rgba(0,0,0,0.1)"
          }}
        >
          <h3>{req.name || "No Name"}</h3>

          <p><b>Email:</b> {req.email}</p>
          <p><b>Service:</b> {req.serviceType}</p>
          <p><b>Experience:</b> {req.experience}</p>
          <p><b>Description:</b> {req.description}</p>

          <div style={{ marginTop: "15px" }}>
            <button
              onClick={() => approve(req.userId, req.id)}
              style={{
                padding: "10px",
                marginRight: "10px",
                backgroundColor: "green",
                color: "white",
                border: "none"
              }}
            >
              Approve
            </button>

            <button
              onClick={() => reject(req.id)}
              style={{
                padding: "10px",
                backgroundColor: "red",
                color: "white",
                border: "none"
              }}
            >
              Reject
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}

export default RequestList;