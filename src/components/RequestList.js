import { useEffect, useState } from "react";
import { db } from "../firebase";
import {
  collection,
  getDocs,
  updateDoc,
  doc,
  query,
  where
} from "firebase/firestore";

/*
  ============================================================
  COMPONENT: RequestList
  PURPOSE: Fetches and displays pending service provider requests.
  
  CHANGE LOG:
  PAST:   - Dark theme with purple cards.
  PRESENT: - Light gray cards (#D9D9D9).
           - Black buttons (#000000).
           - Circle avatars preserved and styled for high contrast.
  
  DEPENDENCIES:
  - firebase/firestore: collection, getDocs, updateDoc, doc, query, where
  - firebase.js: db instance
  ============================================================
*/

function RequestList() {
  const [requests, setRequests] = useState([]);
  const [processingId, setProcessingId] = useState(null);

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

  const approve = async (userId, requestId) => {
    setProcessingId(requestId);
    try {
      await updateDoc(doc(db, "users", userId), {
        role: "SERVICE_PROVIDER"
      });

      await updateDoc(doc(db, "provider_requests", requestId), {
        status: "APPROVED"
      });

      fetchRequests();
    } catch (error) {
      console.error("Approve Error:", error);
    } finally {
      setProcessingId(null);
    }
  };

  const reject = async (requestId) => {
    setProcessingId(requestId);
    try {
      await updateDoc(doc(db, "provider_requests", requestId), {
        status: "REJECTED"
      });

      fetchRequests();
    } catch (error) {
      console.error("Reject Error:", error);
    } finally {
      setProcessingId(null);
    }
  };

  const getInitials = (name) => {
    if (!name) return "?";
    return name.split(" ").map(n => n[0]).join("").toUpperCase().substring(0, 2);
  };

  const styles = {
    container: {
      width: '100%',
    },
    title: {
      fontSize: 'var(--font-size-2xl)',
      fontWeight: '800',
      marginBottom: 'var(--space-xl)',
      color: '#000',
      textTransform: 'uppercase',
      letterSpacing: '1px',
    },
    emptyState: {
      textAlign: 'center',
      padding: 'var(--space-2xl)',
      background: '#f0f0f0',
      borderRadius: '0px',
      border: '2px dashed #000',
      color: '#000',
      fontWeight: '600',
    },
    card: {
      background: '#D9D9D9',
      borderRadius: '0px',
      padding: 'var(--space-xl)',
      marginBottom: 'var(--space-lg)',
      boxShadow: 'none',
      border: '2px solid #000',
      display: 'flex',
      flexDirection: 'column',
      gap: 'var(--space-md)',
      transition: 'transform var(--transition-fast)',
    },
    cardHeader: {
      display: 'flex',
      alignItems: 'center',
      gap: 'var(--space-md)',
      marginBottom: 'var(--space-sm)',
    },
    avatar: {
      width: '56px',
      height: '56px',
      borderRadius: '50%',
      background: '#000',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontWeight: '800',
      fontSize: 'var(--font-size-lg)',
      border: '2px solid #000',
    },
    nameInfo: {
      flex: 1,
    },
    name: {
      fontSize: 'var(--font-size-lg)',
      fontWeight: '800',
      color: '#000',
      textTransform: 'uppercase',
    },
    badge: {
      display: 'inline-block',
      padding: '4px 12px',
      borderRadius: '0px',
      fontSize: 'var(--font-size-xs)',
      fontWeight: '800',
      textTransform: 'uppercase',
      background: '#000',
      color: '#fff',
      marginTop: '4px',
    },
    detailsGrid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: 'var(--space-md)',
      padding: 'var(--space-md)',
      background: '#ffffff',
      border: '1px solid #000',
      borderRadius: '0px',
    },
    detailItem: {
      display: 'flex',
      flexDirection: 'column',
      gap: '2px',
    },
    detailLabel: {
      fontSize: 'var(--font-size-xs)',
      color: '#666',
      fontWeight: '800',
      textTransform: 'uppercase',
    },
    detailValue: {
      fontSize: 'var(--font-size-sm)',
      color: '#000',
      fontWeight: '600',
    },
    description: {
      fontSize: 'var(--font-size-sm)',
      color: '#000',
      fontStyle: 'italic',
      lineHeight: '1.5',
      borderLeft: '4px solid #000',
      paddingLeft: 'var(--space-md)',
      background: '#ffffff',
      padding: 'var(--space-sm) var(--space-md)',
    },
    actions: {
      display: 'flex',
      gap: 'var(--space-md)',
      marginTop: 'var(--space-sm)',
    },
    approveBtn: {
      flex: 1,
      padding: '14px',
      borderRadius: '0px',
      background: '#000000',
      color: '#fff',
      border: '2px solid #000000',
      fontWeight: '800',
      cursor: processingId ? 'not-allowed' : 'pointer',
      transition: 'all var(--transition-fast)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      textTransform: 'uppercase',
      letterSpacing: '1px',
    },
    rejectBtn: {
      flex: 1,
      padding: '14px',
      borderRadius: '0px',
      background: 'transparent',
      color: '#000',
      border: '2px solid #000',
      fontWeight: '800',
      cursor: processingId ? 'not-allowed' : 'pointer',
      transition: 'all var(--transition-fast)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      textTransform: 'uppercase',
      letterSpacing: '1px',
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Pending Requests</h2>

      {requests.length === 0 ? (
        <div style={styles.emptyState}>
          <p>NO PENDING REQUESTS</p>
        </div>
      ) : (
        requests.map(req => (
          <div
            key={req.id}
            style={styles.card}
            onMouseOver={(e) => { e.currentTarget.style.transform = 'translateY(-2px)'; }}
            onMouseOut={(e) => { e.currentTarget.style.transform = 'translateY(0)'; }}
          >
            <div style={styles.cardHeader}>
              <div style={styles.avatar}>{getInitials(req.name)}</div>
              <div style={styles.nameInfo}>
                <div style={styles.name}>{req.name || "Unknown Provider"}</div>
                <div style={styles.badge}>{req.serviceType || "General"}</div>
              </div>
            </div>

            <div style={styles.detailsGrid}>
              <div style={styles.detailItem}>
                <span style={styles.detailLabel}>Email Address</span>
                <span style={styles.detailValue}>{req.email}</span>
              </div>
              <div style={styles.detailItem}>
                <span style={styles.detailLabel}>Years of Experience</span>
                <span style={styles.detailValue}>{req.experience} Years</span>
              </div>
            </div>

            {req.description && (
              <div style={styles.description}>
                "{req.description}"
              </div>
            )}

            <div style={styles.actions}>
              <button
                disabled={processingId !== null}
                onClick={() => approve(req.userId, req.id)}
                style={styles.approveBtn}
                onMouseOver={(e) => { if(!processingId) { e.target.style.background = '#333'; e.target.style.borderColor = '#333'; } }}
                onMouseOut={(e) => { if(!processingId) { e.target.style.background = '#000'; e.target.style.borderColor = '#000'; } }}
              >
                {processingId === req.id ? "Processing..." : "Approve"}
              </button>

              <button
                disabled={processingId !== null}
                onClick={() => reject(req.id)}
                style={styles.rejectBtn}
                onMouseOver={(e) => { if(!processingId) { e.target.style.background = '#000'; e.target.style.color = '#fff'; } }}
                onMouseOut={(e) => { if(!processingId) { e.target.style.background = 'transparent'; e.target.style.color = '#000'; } }}
              >
                {processingId === req.id ? "Processing..." : "Reject"}
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default RequestList;