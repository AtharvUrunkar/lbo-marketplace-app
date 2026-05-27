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

  // =========================================================
  // 🔥 STATE
  // =========================================================

  const [requests, setRequests] = useState([]);

  // =========================================================
  // 🔥 FETCH REQUESTS
  // =========================================================

  useEffect(() => {
    fetchRequests();
  }, []);

  const fetchRequests = async () => {

    try {

      const q = query(
        collection(db, "provider_requests"),
        where("status", "==", "PENDING")
      );

      const querySnapshot =
        await getDocs(q);

      const data =
        querySnapshot.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        }));

      setRequests(data);

    } catch (error) {

      console.error(
        "Fetch Requests Error:",
        error
      );
    }
  };

  // =========================================================
  // 🔥 DEBUG
  // =========================================================

  console.log(
    "ADMIN USER:",
    auth.currentUser
  );

  // =========================================================
  // 🔥 APPROVE REQUEST
  // =========================================================

  const approve = async (
    userId,
    requestId
  ) => {

    try {

      console.log(
        "STEP 1: updating user"
      );

      // =====================================
      // 🔥 UPDATE USER ROLE
      // =====================================

      await updateDoc(
        doc(db, "users", userId),
        {

          role: "SERVICE_PROVIDER",

          providerApproved: true
        }
      );

      console.log(
        "STEP 2: updating request"
      );

      // =====================================
      // 🔥 UPDATE REQUEST STATUS
      // =====================================

      await updateDoc(
        doc(
          db,
          "provider_requests",
          requestId
        ),
        {
          status: "APPROVED"
        }
      );

      // =====================================
      // 🔥 REFRESH LIST
      // =====================================

      fetchRequests();

    } catch (error) {

      console.error(
        "Approve Error:",
        error
      );
    }
  };

  // =========================================================
  // 🔥 REJECT REQUEST
  // =========================================================

  const reject = async (
    requestId
  ) => {

    try {

      await updateDoc(
        doc(
          db,
          "provider_requests",
          requestId
        ),
        {
          status: "REJECTED"
        }
      );

      fetchRequests();

    } catch (error) {

      console.error(
        "Reject Error:",
        error
      );
    }
  };

  // =========================================================
  // 🔥 UI
  // =========================================================

  return (

    <div
      style={{
        maxWidth: "900px",
        margin: "auto",
        padding: "20px"
      }}
    >

      {/* ========================================= */}
      {/* 🔥 TITLE */}
      {/* ========================================= */}

      <h2
        style={{
          textAlign: "center",
          marginBottom: "30px"
        }}
      >
        Pending Provider Requests
      </h2>

      {/* ========================================= */}
      {/* 🔥 EMPTY STATE */}
      {/* ========================================= */}

      {requests.length === 0 && (

        <p
          style={{
            textAlign: "center"
          }}
        >
          No pending requests
        </p>
      )}

      {/* ========================================= */}
      {/* 🔥 REQUEST LIST */}
      {/* ========================================= */}

      {requests.map(req => (

        <div
          key={req.id}
          style={{

            background: "#fff",

            borderRadius: "12px",

            padding: "20px",

            marginBottom: "20px",

            boxShadow:
              "0 4px 10px rgba(0,0,0,0.1)"
          }}
        >

          {/* ===================================== */}
          {/* 🔥 PROVIDER INFO */}
          {/* ===================================== */}

          <h3
            style={{
              marginBottom: "15px"
            }}
          >
            {req.name || "No Name"}
          </h3>

          <p>
            <b>Email:</b>{" "}
            {req.email}
          </p>

          <p>
            <b>Service:</b>{" "}
            {req.serviceType}
          </p>

          <p>
            <b>Experience:</b>{" "}
            {req.experience}
          </p>

          <p>
            <b>Description:</b>{" "}
            {req.description}
          </p>

          {/* ===================================== */}
          {/* 🔥 ACTION BUTTONS */}
          {/* ===================================== */}

          <div
            style={{
              marginTop: "20px",
              display: "flex",
              gap: "10px",
              flexWrap: "wrap"
            }}
          >

            {/* ================================= */}
            {/* 🔥 VIEW DOCUMENT */}
            {/* ================================= */}

<a
  href={req.verificationDocUrl}
  target="_blank"
  rel="noopener noreferrer"
  style={{
    padding: "10px 16px",
    backgroundColor: "#1976d2",
    color: "white",
    textDecoration: "none",
    borderRadius: "6px",
    fontWeight: "bold"
  }}
>
  Open Document
</a>

            {/* ================================= */}
            {/* 🔥 APPROVE */}
            {/* ================================= */}

            <button
              onClick={() =>
                approve(
                  req.userId,
                  req.id
                )
              }

              style={{

                padding: "10px 16px",

                backgroundColor: "green",

                color: "white",

                border: "none",

                borderRadius: "6px",

                cursor: "pointer",

                fontWeight: "bold"
              }}
            >
              Approve
            </button>

            {/* ================================= */}
            {/* 🔥 REJECT */}
            {/* ================================= */}

            <button
              onClick={() =>
                reject(req.id)
              }

              style={{

                padding: "10px 16px",

                backgroundColor: "red",

                color: "white",

                border: "none",

                borderRadius: "6px",

                cursor: "pointer",

                fontWeight: "bold"
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