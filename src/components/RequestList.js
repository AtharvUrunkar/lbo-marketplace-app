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
  - Reduced dimensions and font sizes for a MINIMALIST look.
  - More compact cards and data grids.
  - Refined empty state and action buttons.
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

  const getAvatarColor = (name) => {
    if (!name) return "#000000";
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
      hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const colors = [
      "#E74C3C", "#3498DB", "#2ECC71", "#F1C40F", 
      "#9B59B6", "#1ABC9C", "#E67E22", "#34495E"
    ];
    return colors[Math.abs(hash) % colors.length];
  };

  const styles = {
    container: {
      width: '100%',
    },
    title: {
      fontSize: '20px', // Reduced from 28px
      fontWeight: '900',
      marginBottom: 'var(--space-xl)',
      color: '#000',
      textTransform: 'uppercase',
      letterSpacing: '0.05em',
    },
    emptyState: {
      textAlign: 'center',
      padding: '40px var(--space-xl)', // Reduced from 80px
      background: '#ffffff',
      borderRadius: 'var(--radius-lg)',
      border: '2px dashed #000000',
      color: '#000',
      maxWidth: '600px', // Added limit to width
    },
    emptyStateTitle: {
      fontSize: '18px', // Reduced from 24px
      fontWeight: '900',
      marginBottom: '4px',
      textTransform: 'uppercase',
    },
    card: {
      background: '#D9D9D9',
      borderRadius: 'var(--radius-md)',
      padding: 'var(--space-xl)', // Reduced from space-2xl
      marginBottom: 'var(--space-lg)',
      border: '1.5px solid #000000',
      display: 'flex',
      flexDirection: 'column',
      gap: 'var(--space-md)',
      boxShadow: '4px 4px 0px #000000', // Reduced shadow from 10px
    },
    cardHeader: {
      display: 'flex',
      alignItems: 'center',
      gap: 'var(--space-md)',
    },
    avatar: (name) => ({
      width: '48px', // Reduced from 72px
      height: '48px',
      borderRadius: '50%',
      background: getAvatarColor(name),
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: '#fff',
      fontWeight: '900',
      fontSize: '16px', // Reduced from 24px
      border: '2px solid #000000',
    }),
    nameInfo: {
      flex: 1,
    },
    name: {
      fontSize: '18px', // Reduced from 24px
      fontWeight: '900',
      color: '#000',
      textTransform: 'uppercase',
    },
    badge: {
      display: 'inline-block',
      padding: '4px 12px', // Reduced from 8px 20px
      borderRadius: 'var(--radius-full)',
      fontSize: '10px', // Reduced from 12px
      fontWeight: '900',
      textTransform: 'uppercase',
      background: '#000000',
      color: '#fff',
      marginTop: '6px',
      letterSpacing: '0.05em',
    },
    detailsGrid: {
      display: 'grid',
      gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
      gap: 'var(--space-md)',
      padding: 'var(--space-md)',
      background: '#ffffff',
      borderRadius: 'var(--radius-sm)',
      border: '1.5px solid #000000',
    },
    detailItem: {
      display: 'flex',
      flexDirection: 'column',
      gap: '2px',
    },
    detailLabel: {
      fontSize: '10px',
      color: '#666',
      fontWeight: '900',
      textTransform: 'uppercase',
    },
    detailValue: {
      fontSize: '14px',
      color: '#000',
      fontWeight: '800',
    },
    description: {
      fontSize: '13px',
      color: '#333',
      fontStyle: 'italic',
      lineHeight: '1.4',
      background: '#ffffff',
      padding: '12px 16px',
      borderRadius: 'var(--radius-sm)',
      border: '1.5px solid #000000',
    },
    actions: {
      display: 'flex',
      gap: 'var(--space-md)',
    },
    approveBtn: {
      flex: 1,
      padding: '12px', // Reduced from 20px
      borderRadius: 'var(--radius-sm)',
      background: '#000000',
      color: '#fff',
      border: '1.5px solid #000000',
      fontWeight: '900',
      cursor: processingId ? 'not-allowed' : 'pointer',
      transition: 'all var(--transition-fast)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      textTransform: 'uppercase',
      fontSize: '12px',
    },
    rejectBtn: {
      flex: 1,
      padding: '12px',
      borderRadius: 'var(--radius-sm)',
      background: '#ffffff',
      color: '#000',
      border: '1.5px solid #000000',
      fontWeight: '900',
      cursor: processingId ? 'not-allowed' : 'pointer',
      transition: 'all var(--transition-fast)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      textTransform: 'uppercase',
      fontSize: '12px',
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Pending Approvals</h2>

      {requests.length === 0 ? (
        <div style={styles.emptyState}>
          <div style={styles.emptyStateTitle}>Queue Empty</div>
          <p style={{fontWeight: '700', fontSize: '12px'}}>All provider requests have been processed.</p>
        </div>
      ) : (
        requests.map(req => (
          <div key={req.id} style={styles.card}>
            <div style={styles.cardHeader}>
              <div style={styles.avatar(req.name)}>{getInitials(req.name)}</div>
              <div style={styles.nameInfo}>
                <div style={styles.name}>{req.name || "New Provider"}</div>
                <div style={styles.badge}>{req.serviceType || "Unclassified"}</div>
              </div>
            </div>

            <div style={styles.detailsGrid}>
              <div style={styles.detailItem}>
                <span style={styles.detailLabel}>Email</span>
                <span style={styles.detailValue}>{req.email}</span>
              </div>
              <div style={styles.detailItem}>
                <span style={styles.detailLabel}>Experience</span>
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
              >
                {processingId === req.id ? "..." : "Approve"}
              </button>

              <button
                disabled={processingId !== null}
                onClick={() => reject(req.id)}
                style={styles.rejectBtn}
              >
                {processingId === req.id ? "..." : "Decline"}
              </button>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

export default RequestList;