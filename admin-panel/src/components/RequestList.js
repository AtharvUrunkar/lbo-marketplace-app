import { useEffect, useState } from "react";
import { db, auth } from "../firebase";
import {
  collection,
  getDocs,
  getDoc,
  updateDoc,
  doc,
  query,
  where
} from "firebase/firestore";

// =========================================================
// 🌐 CONSTANTS
// =========================================================
const STANDARD_CATEGORIES = [
  "AC Repair",
  "Appliance Repair",
  "Carpenter",
  "Civil Lawyer",
  "Cleaning Services",
  "Doctor",
  "Education & Tuition",
  "Electrician",
  "Home Services",
  "Hotel",
  "Lawyer",
  "Painter",
  "Pest Control",
  "Plumber",
  "Police"
].sort();

const STANDARD_CLUSTERS = [
  "Sangli",
  "Kolhapur",
  "Belgaum",
  "ichalkaranji"
];

function RequestList({ showToast }) {
  // =========================================================
  // 🔥 STATE
  // =========================================================
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activePreviewRequest, setActivePreviewRequest] = useState(null);
  const [docLoading, setDocLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");

  // Filtering & Selection Assignments
  const [selectedFilterCluster, setSelectedFilterCluster] = useState("All");
  const [categoryAssignments, setCategoryAssignments] = useState({});
  const [clusterAssignments, setClusterAssignments] = useState({});

  // Analytics State
  const [analyticsModalOpen, setAnalyticsModalOpen] = useState(false);
  const [analyticsLoading, setAnalyticsLoading] = useState(false);
  const [analyticsData, setAnalyticsData] = useState({
    totalUsers: 0,
    providersCount: 0,
    clientsCount: 0,
    pendingCount: 0,
    approvedCount: 0,
    rejectedCount: 0,
    categoryCounts: {},
    clusterCounts: {},
    auditLogs: []
  });

  // =========================================================
  // 🔥 FETCH PENDING APPLICATIONS & SMART RETRIEVAL
  // =========================================================
  useEffect(() => {
    fetchRequests();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchRequests = async () => {
    setLoading(true);
    try {
      const q = query(
        collection(db, "provider_requests"),
        where("status", "==", "PENDING")
      );

      const querySnapshot = await getDocs(q);
      const requestData = [];

      for (const document of querySnapshot.docs) {
        const req = { id: document.id, ...document.data() };

        // Smart retrieval: fetch user profile for contact & address fallbacks
        try {
          const userDoc = await getDoc(doc(db, "users", req.userId));
          if (userDoc.exists()) {
            req.userProfile = userDoc.data();
          }
        } catch (err) {
          console.warn(`Could not retrieve user profile for ${req.userId}:`, err);
        }

        requestData.push(req);
      }

      setRequests(requestData);

      // Pre-populate assignments using smart auto-detection
      const initialCategories = {};
      const initialClusters = {};

      requestData.forEach(req => {
        initialCategories[req.id] = getSuggestedCategory(req);
        initialClusters[req.id] = getSuggestedCluster(req) || "Sangli";
      });

      setCategoryAssignments(initialCategories);
      setClusterAssignments(initialClusters);

    } catch (error) {
      console.error("Fetch Requests Error:", error);
      showToast("Failed to fetch pending requests: " + error.message, "error");
    } finally {
      setLoading(false);
    }
  };

  // =========================================================
  // 🔥 SMART DETECTION ALGORITHM
  // =========================================================
  const getSuggestedCluster = (req) => {
    const addressText = [
      req.fullAddress,
      req.city,
      req.area,
      req.address,
      req.location,
      req.userProfile?.fullAddress,
      req.userProfile?.city,
      req.userProfile?.area,
      req.userProfile?.address,
      req.description
    ].filter(Boolean).join(" ").toLowerCase();

    if (addressText.includes("sangli")) return "Sangli";
    if (addressText.includes("kolhapur")) return "Kolhapur";
    if (addressText.includes("belgaum") || addressText.includes("belgav") || addressText.includes("belgavi")) return "Belgaum";
    if (addressText.includes("ichalkaranji") || addressText.includes("ichalkaranji") || addressText.includes("ichal")) return "ichalkaranji";

    return ""; // No suggestion found
  };

  const getSuggestedCategory = (req) => {
    const serviceType = req.serviceType || req.userProfile?.serviceType || req.userProfile?.category || "";
    const matched = STANDARD_CATEGORIES.find(
      cat => cat.toLowerCase() === serviceType.toLowerCase() || serviceType.toLowerCase().includes(cat.toLowerCase())
    );
    return matched || serviceType || STANDARD_CATEGORIES[0];
  };

  const getAppCityName = (cluster) => {
    if (cluster === "Belgaum") return "Belgav";
    if (cluster === "ichalkaranji") return "Ichalkaranji";
    return cluster; // Sangli and Kolhapur match perfectly
  };

  // =========================================================
  // 🔥 SYSTEM ANALYTICS FETCH ENGINE
  // =========================================================
  const loadAnalytics = async () => {
    setAnalyticsLoading(true);
    setAnalyticsModalOpen(true);
    try {
      // 1. Fetch Users counts
      const usersSnapshot = await getDocs(collection(db, "users"));
      const usersList = usersSnapshot.docs.map(d => d.data());
      const totalUsers = usersList.length;
      const providers = usersList.filter(u => u.role === "SERVICE_PROVIDER" || u.providerApproved === true);
      const clients = usersList.filter(u => u.role === "USER" || (!u.role && !u.providerApproved));

      // 2. Fetch Provider Requests logs
      const reqsSnapshot = await getDocs(collection(db, "provider_requests"));
      const reqsList = reqsSnapshot.docs.map(d => ({ id: d.id, ...d.data() }));
      const pendingReqs = reqsList.filter(r => r.status === "PENDING").length;
      const approvedReqs = reqsList.filter(r => r.status === "APPROVED");
      const rejectedReqs = reqsList.filter(r => r.status === "REJECTED").length;

      // 3. Aggregate Categories & Clusters distributions
      const catCounts = {};
      const cluCounts = {};

      providers.forEach(p => {
        const cat = p.serviceType || p.category || "General Services";
        catCounts[cat] = (catCounts[cat] || 0) + 1;

        const clu = p.cluster || "Sangli";
        cluCounts[clu] = (cluCounts[clu] || 0) + 1;
      });

      // 4. Construct Audit logs
      const auditLogs = approvedReqs
        .filter(r => r.approvedAt)
        .sort((a, b) => new Date(b.approvedAt) - new Date(a.approvedAt));

      setAnalyticsData({
        totalUsers,
        providersCount: providers.length,
        clientsCount: clients.length,
        pendingCount: pendingReqs,
        approvedCount: approvedReqs.length,
        rejectedCount: rejectedReqs,
        categoryCounts: catCounts,
        clusterCounts: cluCounts,
        auditLogs
      });

    } catch (err) {
      console.error("Error loading analytics:", err);
      showToast("Failed to fetch analytics metrics: " + err.message, "error");
    } finally {
      setAnalyticsLoading(false);
    }
  };

  // =========================================================
  // 🔥 APPROVE OPERATION (WITH DYNAMIC AUDITING)
  // =========================================================
  const approve = async (userId, requestId, providerName) => {
    try {
      const assignedCat = categoryAssignments[requestId] || STANDARD_CATEGORIES[0];
      const assignedClus = clusterAssignments[requestId] || "Sangli";

      const adminUser = auth.currentUser;
      const adminEmail = adminUser ? adminUser.email : "system.admin@lbo.com";
      const adminUid = adminUser ? adminUser.uid : "system";

      showToast(`Verifying & Approving ${providerName}...`, "info");

      // Update User Document
      await updateDoc(
        doc(db, "users", userId),
        {
          role: "SERVICE_PROVIDER",
          providerApproved: true,
          serviceType: assignedCat,
          category: assignedCat,
          cluster: assignedClus,
          city: getAppCityName(assignedClus),
          approvedByEmail: adminEmail,
          approvedByUid: adminUid,
          approvedAt: new Date().toISOString()
        }
      );

      // Update Verification Request Document
      await updateDoc(
        doc(db, "provider_requests", requestId),
        {
          status: "APPROVED",
          serviceType: assignedCat,
          cluster: assignedClus,
          city: getAppCityName(assignedClus),
          approvedByEmail: adminEmail,
          approvedByUid: adminUid,
          approvedAt: new Date().toISOString()
        }
      );

      showToast(`Successfully verified ${providerName}!`, "success");

      if (activePreviewRequest?.id === requestId) {
        setActivePreviewRequest(null);
      }

      fetchRequests();
    } catch (error) {
      console.error("Approve Error:", error);
      showToast("Verification failed: " + error.message, "error");
    }
  };

  // =========================================================
  // 🔥 REJECT OPERATION
  // =========================================================
  const reject = async (requestId, providerName) => {
    try {
      showToast(`Rejecting registration...`, "info");

      await updateDoc(
        doc(db, "provider_requests", requestId),
        {
          status: "REJECTED",
          rejectedAt: new Date().toISOString()
        }
      );

      showToast(`Successfully rejected ${providerName || "Provider"}'s application.`, "success");

      if (activePreviewRequest?.id === requestId) {
        setActivePreviewRequest(null);
      }

      fetchRequests();
    } catch (error) {
      console.error("Reject Error:", error);
      showToast("Rejection failed: " + error.message, "error");
    }
  };

  // =========================================================
  // 🔥 UTILITIES
  // =========================================================
  const isImageUrl = (url) => {
    if (!url) return false;
    const cleanUrl = url.toLowerCase().split('?')[0];
    return (
      cleanUrl.endsWith('.jpg') ||
      cleanUrl.endsWith('.jpeg') ||
      cleanUrl.endsWith('.png') ||
      cleanUrl.endsWith('.webp') ||
      cleanUrl.endsWith('.gif') ||
      cleanUrl.includes('/image/upload/')
    );
  };

  const getInitials = (name) => {
    if (!name) return "P";
    return name
      .split(" ")
      .map(n => n[0])
      .join("")
      .toUpperCase()
      .substring(0, 2);
  };

  // =========================================================
  // 🔥 DYNAMIC CLIENT-SIDE FILTERING & SEARCHING
  // =========================================================
  const filteredRequests = requests.filter(req => {
    const term = searchQuery.toLowerCase();
    const matchesSearch = (
      (req.name && req.name.toLowerCase().includes(term)) ||
      (req.email && req.email.toLowerCase().includes(term)) ||
      (req.serviceType && req.serviceType.toLowerCase().includes(term)) ||
      (req.description && req.description.toLowerCase().includes(term))
    );

    if (selectedFilterCluster === "All") return matchesSearch;

    // Filter by suggested or currently assigned cluster before approval
    const suggested = getSuggestedCluster(req) || clusterAssignments[req.id] || "Sangli";
    return matchesSearch && suggested === selectedFilterCluster;
  });

  // =========================================================
  // 🔥 RENDER COMPONENT
  // =========================================================
  return (
    <div style={{ maxWidth: "1200px", margin: "0 auto", display: "flex", flexDirection: "column", gap: "32px" }}>

      {/* ====================================================================
         🔥 METRICS & ANALYTICS HEADER PANELS
         ==================================================================== */}
      <div style={{
        display: "grid",
        gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))",
        gap: "20px"
      }}>
        {/* Stat Panel 1: Pending */}
        <div className="lbo-card animate-slide-up" style={{ display: "flex", alignItems: "center", gap: "20px", padding: "20px 24px" }}>
          <div className="flex-center" style={{
            width: "48px",
            height: "48px",
            borderRadius: "50%",
            backgroundColor: "var(--color-primary)",
            color: "var(--bg-card)"
          }}>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
          </div>
          <div>
            <h4 style={{ fontSize: "1.75rem", fontWeight: "800", lineHeight: 1.1 }}>{requests.length}</h4>
            <span style={{ fontSize: "0.85rem", color: "var(--color-text-secondary)", fontWeight: "600" }}>Pending Requests</span>
          </div>
        </div>

        {/* Stat Panel 2: Categories */}
        <div className="lbo-card animate-slide-up" style={{ display: "flex", alignItems: "center", gap: "20px", padding: "20px 24px", animationDelay: "0.1s" }}>
          <div className="flex-center" style={{
            width: "48px",
            height: "48px",
            borderRadius: "50%",
            backgroundColor: "var(--bg-input)",
            color: "var(--color-primary)",
            border: "1px solid var(--border-color)"
          }}>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <rect x="2" y="7" width="20" height="14" rx="2" ry="2"></rect>
              <path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"></path>
            </svg>
          </div>
          <div>
            <h4 style={{ fontSize: "1.75rem", fontWeight: "800", lineHeight: 1.1 }}>{STANDARD_CATEGORIES.length}</h4>
            <span style={{ fontSize: "0.85rem", color: "var(--color-text-secondary)", fontWeight: "600" }}>Available Services</span>
          </div>
        </div>

        {/* Stat Panel 3: Interactive System Analytics (Replacing 'Ready' card) */}
        <button
          onClick={loadAnalytics}
          className="lbo-card animate-slide-up"
          style={{
            display: "flex",
            alignItems: "center",
            gap: "20px",
            padding: "20px 24px",
            animationDelay: "0.2s",
            cursor: "pointer",
            border: "1px solid var(--border-color)",
            textAlign: "left",
            width: "100%",
            background: "var(--bg-card)",
            color: "inherit",
            fontFamily: "inherit",
            outline: "none"
          }}
        >
          <div className="flex-center" style={{
            width: "48px",
            height: "48px",
            borderRadius: "50%",
            backgroundColor: "var(--color-success-bg)",
            color: "var(--color-success)"
          }}>
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <line x1="18" y1="20" x2="18" y2="10"></line>
              <line x1="12" y1="20" x2="12" y2="4"></line>
              <line x1="6" y1="20" x2="6" y2="14"></line>
            </svg>
          </div>
          <div>
            <h4 style={{ fontSize: "1.75rem", fontWeight: "800", lineHeight: 1.1 }}>Analytics</h4>
            <span style={{ fontSize: "0.85rem", color: "var(--color-text-secondary)", fontWeight: "600", display: "flex", alignItems: "center", gap: "4px" }}>
              <span>System Status Log</span>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <line x1="5" y1="12" x2="19" y2="12"></line>
                <polyline points="12 5 19 12 12 19"></polyline>
              </svg>
            </span>
          </div>
        </button>
      </div>

      {/* ====================================================================
         🔥 MAIN CONTROL TOOLBAR (SEARCH & CLUSTER FILTERS)
         ==================================================================== */}
      <div className="glass-panel" style={{
        padding: "16px 24px",
        borderRadius: "var(--radius-md)",
        display: "flex",
        alignItems: "center",
        justifyContent: "space-between",
        gap: "20px",
        flexWrap: "wrap",
        transition: "all var(--transition-normal)"
      }}>
        {/* Dynamic Search Box */}
        <div style={{ position: "relative", flex: 1, minWidth: "260px" }}>
          <input
            type="text"
            placeholder="Search registrations by name, email, description..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="lbo-input"
            style={{ paddingLeft: "44px" }}
          />
          <span style={{ position: "absolute", left: "14px", top: "50%", transform: "translateY(-50%)", color: "var(--color-text-muted)", display: "flex" }}>
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <circle cx="11" cy="11" r="8"></circle>
              <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
            </svg>
          </span>
        </div>

        {/* Location Cluster Selector Filter Button */}
        <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
          <span style={{ fontSize: "0.85rem", fontWeight: "700", color: "var(--color-text-secondary)" }}>
            Cluster Filter:
          </span>
          <select
            value={selectedFilterCluster}
            onChange={(e) => setSelectedFilterCluster(e.target.value)}
            className="lbo-input"
            style={{
              padding: "8px 16px",
              height: "48px",
              minWidth: "160px",
              border: "1px solid var(--border-color)",
              backgroundColor: "var(--bg-input)"
            }}
          >
            <option value="All">All Clusters (Sangli/KHP...)</option>
            <option value="Sangli">Sangli Cluster</option>
            <option value="Kolhapur">Kolhapur Cluster</option>
            <option value="Belgaum">Belgaum Cluster</option>
            <option value="ichalkaranji">ichalkaranji Cluster</option>
          </select>
        </div>

        {/* Refresh Button */}
        <button
          onClick={fetchRequests}
          className="lbo-btn lbo-btn-secondary"
          style={{ height: "48px" }}
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
            <polyline points="23 4 23 10 17 10"></polyline>
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path>
          </svg>
          <span>Refresh</span>
        </button>
      </div>

      {/* ====================================================================
         🔥 APPLICATIONS FEED CONTAINER
         ==================================================================== */}
      <div>
        <h3 style={{ fontSize: "1.25rem", marginBottom: "20px", display: "flex", alignItems: "center", gap: "8px" }}>
          <span>Pending Provider Approvals</span>
          <span className="lbo-badge lbo-badge-pending" style={{ fontSize: "0.75rem", padding: "2px 8px" }}>
            {filteredRequests.length} applications
          </span>
        </h3>

        {/* Loading Spinner */}
        {loading && (
          <div className="flex-center" style={{ minHeight: "240px", flexDirection: "column", gap: "16px" }}>
            <div style={{
              width: "40px",
              height: "40px",
              border: "3.5px solid var(--border-color)",
              borderTop: "3.5px solid var(--color-primary)",
              borderRadius: "50%",
              animation: "App-logo-spin 1s linear infinite"
            }} />
            <span style={{ color: "var(--color-text-secondary)", fontWeight: "600", fontSize: "0.9rem" }}>Fetching applications & retrieving profiles...</span>
          </div>
        )}

        {/* Empty State */}
        {!loading && filteredRequests.length === 0 && (
          <div className="lbo-card flex-center animate-scale-in" style={{
            minHeight: "260px",
            flexDirection: "column",
            gap: "16px",
            textAlign: "center",
            padding: "40px",
            borderStyle: "dashed",
            borderWidth: "2px"
          }}>
            <div className="flex-center" style={{
              width: "64px",
              height: "64px",
              borderRadius: "50%",
              backgroundColor: "var(--bg-input)",
              color: "var(--color-text-muted)"
            }}>
              <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path>
                <polyline points="22 4 12 14.01 9 11.01"></polyline>
              </svg>
            </div>
            <div>
              <h4 style={{ fontSize: "1.2rem", fontWeight: "700", marginBottom: "4px" }}>No Registrations Found</h4>
              <p style={{ color: "var(--color-text-secondary)", fontSize: "0.9rem" }}>
                {searchQuery || selectedFilterCluster !== "All"
                  ? "No pending requests matched your active filter or search keyword."
                  : "All registration forms have been successfully verified and approved."}
              </p>
            </div>
            {(searchQuery || selectedFilterCluster !== "All") && (
              <button onClick={() => { setSearchQuery(""); setSelectedFilterCluster("All"); }} className="lbo-btn lbo-btn-secondary" style={{ padding: "8px 16px" }}>
                Reset All Filters
              </button>
            )}
          </div>
        )}

        {/* Application List Cards */}
        {!loading && filteredRequests.length > 0 && (
          <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
            {filteredRequests.map((req, index) => {
              const phoneVal = req.phone || req.phoneNumber || req.userProfile?.phone || req.userProfile?.phoneNumber || "";
              const addressVal = req.fullAddress || req.address || req.city || req.userProfile?.fullAddress || req.userProfile?.address || req.userProfile?.city || "";
              const suggestedClus = getSuggestedCluster(req);

              return (
                <div
                  key={req.id}
                  className="lbo-card animate-slide-up"
                  style={{
                    display: "flex",
                    gap: "24px",
                    alignItems: "flex-start",
                    animationDelay: `${index * 0.05}s`
                  }}
                >
                  {/* Provider Profile Picture or Monogram Avatar */}
                  <div className="flex-center" style={{
                    width: "54px",
                    height: "54px",
                    borderRadius: "50%",
                    backgroundColor: "var(--color-primary)",
                    color: "var(--bg-card)",
                    fontWeight: "700",
                    fontSize: "1.2rem",
                    flexShrink: 0,
                    boxShadow: "var(--shadow-sm)",
                    overflow: "hidden"
                  }}>
                    {req.profileImageUrl || req.profileImage || req.userProfile?.profileImageUrl || req.userProfile?.profileImage ? (
                      <img
                        src={req.profileImageUrl || req.profileImage || req.userProfile?.profileImageUrl || req.userProfile?.profileImage}
                        alt={req.name}
                        style={{ width: "100%", height: "100%", objectFit: "cover" }}
                      />
                    ) : (
                      getInitials(req.name)
                    )}
                  </div>

                  {/* Body Content */}
                  <div style={{ flex: 1, display: "flex", flexDirection: "column", gap: "12px" }}>
                    <div>
                      {/* Name & Basic Badges */}
                      <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap", marginBottom: "6px" }}>
                        <h3 style={{ fontSize: "1.2rem", fontWeight: "800" }}>{req.name || "Anonymous Applicant"}</h3>
                        <span className="lbo-badge lbo-badge-info" style={{ fontWeight: 800 }}>
                          Requested: {req.serviceType || "General"}
                        </span>
                        {req.experience && (
                          <span className="lbo-badge" style={{ backgroundColor: "var(--bg-input)", color: "var(--color-text-primary)", fontWeight: 700 }}>
                            {req.experience} Experience
                          </span>
                        )}
                      </div>

                      {/* Contact metadata */}
                      <div style={{ display: "flex", gap: "20px", flexWrap: "wrap", marginTop: "8px" }}>
                        <a
                          href={`mailto:${req.email}`}
                          style={{
                            fontSize: "0.85rem",
                            color: "var(--color-text-secondary)",
                            fontWeight: "600",
                            display: "inline-flex",
                            alignItems: "center",
                            gap: "6px"
                          }}
                        >
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                            <polyline points="22,6 12,13 2,6"></polyline>
                          </svg>
                          {req.email}
                        </a>

                        {phoneVal && (
                          <a
                            href={`tel:${phoneVal}`}
                            style={{
                              fontSize: "0.85rem",
                              color: "var(--color-text-secondary)",
                              fontWeight: "600",
                              display: "inline-flex",
                              alignItems: "center",
                              gap: "6px"
                            }}
                          >
                            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                              <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
                            </svg>
                            {phoneVal}
                          </a>
                        )}
                      </div>

                      {/* Address Meta Block */}
                      {addressVal && (
                        <div style={{
                          fontSize: "0.85rem",
                          color: "var(--color-text-secondary)",
                          fontWeight: "500",
                          display: "inline-flex",
                          alignItems: "center",
                          gap: "6px",
                          marginTop: "8px"
                        }}>
                          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                            <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                            <circle cx="12" cy="10" r="3"></circle>
                          </svg>
                          <span style={{ fontStyle: "italic" }}>{addressVal}</span>
                        </div>
                      )}
                    </div>

                    {/* Applicant Description */}
                    {req.description && (
                      <div style={{
                        backgroundColor: "var(--bg-input)",
                        padding: "12px 16px",
                        borderRadius: "var(--radius-sm)",
                        borderLeft: "3.5px solid var(--color-primary)",
                        fontSize: "0.9rem",
                        lineHeight: "1.5",
                        color: "var(--color-text-secondary)"
                      }}>
                        {req.description}
                      </div>
                    )}

                    {/* ========================================================
                       🔥 DYNAMIC ASSIGNMENT DROPDOWNS (BEFORE APPROVAL)
                       ======================================================== */}
                    <div style={{
                      display: "flex",
                      gap: "16px",
                      flexWrap: "wrap",
                      padding: "16px",
                      backgroundColor: "var(--bg-input)",
                      borderRadius: "var(--radius-md)",
                      border: "1px solid var(--border-color)",
                      marginTop: "4px"
                    }}>
                      {/* Dropdown 1: Category Selector */}
                      <div style={{ display: "flex", flexDirection: "column", gap: "6px", flex: 1, minWidth: "200px" }}>
                        <label style={{ fontSize: "0.72rem", fontWeight: "800", color: "var(--color-text-secondary)", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                          Assign Service Category
                        </label>
                        <select
                          value={categoryAssignments[req.id] || ""}
                          onChange={(e) => setCategoryAssignments(prev => ({ ...prev, [req.id]: e.target.value }))}
                          className="lbo-input"
                          style={{
                            padding: "8px 12px",
                            height: "40px",
                            fontSize: "0.85rem",
                            border: "1px solid var(--border-color)",
                            backgroundColor: "var(--bg-card)"
                          }}
                        >
                          {STANDARD_CATEGORIES.map(cat => (
                            <option key={cat} value={cat}>{cat}</option>
                          ))}
                        </select>
                      </div>

                      {/* Dropdown 2: Location Cluster Selector */}
                      <div style={{ display: "flex", flexDirection: "column", gap: "6px", flex: 1, minWidth: "200px" }}>
                        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                          <label style={{ fontSize: "0.72rem", fontWeight: "800", color: "var(--color-text-secondary)", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                            Assign Location Cluster
                          </label>
                          {suggestedClus && (
                            <span className="lbo-badge lbo-badge-success" style={{ fontSize: "0.6rem", padding: "1px 6px" }}>
                              Suggested: {suggestedClus}
                            </span>
                          )}
                        </div>
                        <select
                          value={clusterAssignments[req.id] || ""}
                          onChange={(e) => setClusterAssignments(prev => ({ ...prev, [req.id]: e.target.value }))}
                          className="lbo-input"
                          style={{
                            padding: "8px 12px",
                            height: "40px",
                            fontSize: "0.85rem",
                            border: "1px solid var(--border-color)",
                            backgroundColor: "var(--bg-card)"
                          }}
                        >
                          {STANDARD_CLUSTERS.map(clus => (
                            <option key={clus} value={clus}>{clus} Cluster</option>
                          ))}
                        </select>
                      </div>
                    </div>

                    {/* Actions Bar */}
                    <div style={{
                      display: "flex",
                      gap: "12px",
                      flexWrap: "wrap",
                      alignItems: "center",
                      marginTop: "8px"
                    }}>
                      <button
                        onClick={() => {
                          setActivePreviewRequest(req);
                          setDocLoading(true);
                        }}
                        className="lbo-btn lbo-btn-primary"
                        style={{
                          padding: "10px 18px",
                          fontSize: "0.85rem"
                        }}
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                          <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                          <circle cx="12" cy="12" r="3"></circle>
                        </svg>
                        <span>Review Credentials</span>
                      </button>

                      <button
                        onClick={() => approve(req.userId, req.id, req.name)}
                        className="lbo-btn lbo-btn-secondary"
                        style={{
                          padding: "10px 18px",
                          fontSize: "0.85rem",
                          borderColor: "var(--color-success)",
                          color: "var(--color-success)"
                        }}
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                          <polyline points="20 6 9 17 4 12"></polyline>
                        </svg>
                        <span>Approve Provider</span>
                      </button>

                      <button
                        onClick={() => reject(req.id, req.name)}
                        className="lbo-btn lbo-btn-secondary"
                        style={{
                          padding: "10px 18px",
                          fontSize: "0.85rem",
                          borderColor: "var(--color-error)",
                          color: "var(--color-error)"
                        }}
                      >
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                          <line x1="18" y1="6" x2="6" y2="18"></line>
                          <line x1="6" y1="6" x2="18" y2="18"></line>
                        </svg>
                        <span>Reject</span>
                      </button>
                    </div>

                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* ====================================================================
         🔥 INLINE SPLIT-PANE DOCUMENT VIEW MODAL (IN PRESENT TAB)
         ==================================================================== */}
      {activePreviewRequest && (
        <div className="modal-overlay flex-center" onClick={() => setActivePreviewRequest(null)}>
          <div
            className="modal-content-container animate-scale-in"
            onClick={(e) => e.stopPropagation()}
            style={{
              display: "flex",
              flexDirection: "column"
            }}
          >
            {/* Modal Header */}
            <div style={{
              padding: "20px 24px",
              borderBottom: "1px solid var(--border-color)",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              background: "var(--bg-card)",
              zIndex: 10
            }}>
              <div style={{ display: "flex", flexDirection: "column" }}>
                <span style={{ fontSize: "0.75rem", fontWeight: "800", textTransform: "uppercase", letterSpacing: "0.05em", color: "var(--color-text-muted)" }}>
                  Credential Verification
                </span>
                <h3 style={{ fontSize: "1.25rem", fontWeight: "800" }}>
                  {activePreviewRequest.name}'s Document
                </h3>
              </div>

              <button
                onClick={() => setActivePreviewRequest(null)}
                className="lbo-btn lbo-btn-secondary"
                style={{
                  width: "36px",
                  height: "36px",
                  padding: 0,
                  borderRadius: "50%",
                  justifyContent: "center"
                }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>

            {/* Split Pane Modal Body */}
            <div style={{
              flex: 1,
              display: "flex",
              overflow: "hidden",
              flexDirection: window.innerWidth < 800 ? "column" : "row"
            }}>

              {/* Left Column: Visual Document Viewer (60% width) */}
              <div style={{
                flex: 3,
                background: "#0c0c0e",
                position: "relative",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                padding: "20px",
                overflow: "auto",
                borderRight: window.innerWidth < 800 ? "none" : "1px solid var(--border-color)",
                borderBottom: window.innerWidth < 800 ? "1px solid var(--border-color)" : "none"
              }}>
                {docLoading && (
                  <div className="flex-center" style={{
                    position: "absolute",
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    zIndex: 2,
                    backgroundColor: "rgba(12, 12, 14, 0.95)",
                    flexDirection: "column",
                    gap: "12px"
                  }}>
                    <div style={{
                      width: "30px",
                      height: "30px",
                      border: "3px solid #333",
                      borderTop: "3px solid #fff",
                      borderRadius: "50%",
                      animation: "App-logo-spin 0.8s linear infinite"
                    }} />
                    <span style={{ fontSize: "0.8rem", color: "#aaa", fontWeight: "600" }}>Loading Verification File...</span>
                  </div>
                )}

                {activePreviewRequest.verificationDocUrl ? (
                  isImageUrl(activePreviewRequest.verificationDocUrl) ? (
                    <img
                      src={activePreviewRequest.verificationDocUrl}
                      alt="Verification Credentials"
                      onLoad={() => setDocLoading(false)}
                      onError={() => setDocLoading(false)}
                      style={{
                        maxWidth: "100%",
                        maxHeight: "100%",
                        objectFit: "contain",
                        borderRadius: "var(--radius-sm)",
                        boxShadow: "0 8px 32px rgba(0,0,0,0.5)"
                      }}
                    />
                  ) : (
                    <iframe
                      src={activePreviewRequest.verificationDocUrl}
                      title="Verification Document"
                      onLoad={() => setDocLoading(false)}
                      style={{
                        width: "100%",
                        height: "100%",
                        border: "none",
                        backgroundColor: "#fff",
                        borderRadius: "var(--radius-sm)"
                      }}
                    />
                  )
                ) : (
                  <div style={{ color: "#aaa", textAlign: "center" }}>
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" style={{ marginBottom: "12px" }}>
                      <path d="M13 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9z"></path>
                      <polyline points="13 2 13 9 20 9"></polyline>
                    </svg>
                    <p style={{ fontSize: "0.9rem" }}>No document URL uploaded for this provider</p>
                  </div>
                )}
              </div>

              {/* Right Column: Applicant Details & Direct Operations (40% width) */}
              <div style={{
                flex: 2,
                padding: "24px",
                display: "flex",
                flexDirection: "column",
                gap: "20px",
                overflowY: "auto",
                background: "var(--bg-card)"
              }}>
                <div>
                  <div style={{ display: "flex", gap: "16px", alignItems: "center", marginBottom: "16px" }}>
                    <div className="flex-center" style={{
                      width: "60px",
                      height: "60px",
                      borderRadius: "50%",
                      backgroundColor: "var(--bg-input)",
                      border: "1px solid var(--border-color)",
                      overflow: "hidden",
                      flexShrink: 0
                    }}>
                      {activePreviewRequest.profileImageUrl || activePreviewRequest.profileImage || activePreviewRequest.userProfile?.profileImageUrl || activePreviewRequest.userProfile?.profileImage ? (
                        <img
                          src={activePreviewRequest.profileImageUrl || activePreviewRequest.profileImage || activePreviewRequest.userProfile?.profileImageUrl || activePreviewRequest.userProfile?.profileImage}
                          alt={activePreviewRequest.name}
                          style={{ width: "100%", height: "100%", objectFit: "cover" }}
                        />
                      ) : (
                        <span style={{ fontSize: "1.2rem", fontWeight: "700", color: "var(--color-text-secondary)" }}>
                          {getInitials(activePreviewRequest.name)}
                        </span>
                      )}
                    </div>
                    <div>
                      <h4 style={{ fontSize: "1.1rem", fontWeight: "800", margin: 0 }}>Applicant Details</h4>
                      <span style={{ fontSize: "0.75rem", color: "var(--color-text-muted)" }}>ID: {activePreviewRequest.userId}</span>
                    </div>
                  </div>

                  <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                    <div style={{ display: "flex", justifyContent: "space-between", borderBottom: "1px solid var(--border-color)", paddingBottom: "6px" }}>
                      <span style={{ fontSize: "0.85rem", color: "var(--color-text-muted)", fontWeight: "600" }}>Full Name</span>
                      <span style={{ fontSize: "0.85rem", fontWeight: "700" }}>{activePreviewRequest.name}</span>
                    </div>

                    <div style={{ display: "flex", justifyContent: "space-between", borderBottom: "1px solid var(--border-color)", paddingBottom: "6px" }}>
                      <span style={{ fontSize: "0.85rem", color: "var(--color-text-muted)", fontWeight: "600" }}>Email Address</span>
                      <span style={{ fontSize: "0.85rem", fontWeight: "700", wordBreak: "break-all" }}>{activePreviewRequest.email}</span>
                    </div>

                    {activePreviewRequest.phone || activePreviewRequest.userProfile?.phone ? (
                      <div style={{ display: "flex", justifyContent: "space-between", borderBottom: "1px solid var(--border-color)", paddingBottom: "6px" }}>
                        <span style={{ fontSize: "0.85rem", color: "var(--color-text-muted)", fontWeight: "600" }}>Phone Number</span>
                        <span style={{ fontSize: "0.85rem", fontWeight: "700" }}>{activePreviewRequest.phone || activePreviewRequest.userProfile?.phone}</span>
                      </div>
                    ) : null}

                    <div style={{ display: "flex", justifyContent: "space-between", borderBottom: "1px solid var(--border-color)", paddingBottom: "6px" }}>
                      <span style={{ fontSize: "0.85rem", color: "var(--color-text-muted)", fontWeight: "600" }}>Experience</span>
                      <span style={{ fontSize: "0.85rem", fontWeight: "700" }}>{activePreviewRequest.experience || "Not listed"}</span>
                    </div>
                  </div>
                </div>

                {/* Description Statement */}
                {activePreviewRequest.description && (
                  <div>
                    <h5 style={{ fontSize: "0.8rem", fontWeight: "700", color: "var(--color-text-secondary)", textTransform: "uppercase", letterSpacing: "0.05em", marginBottom: "6px" }}>
                      Professional Statement
                    </h5>
                    <div style={{
                      backgroundColor: "var(--bg-input)",
                      padding: "10px 14px",
                      borderRadius: "var(--radius-sm)",
                      fontSize: "0.82rem",
                      lineHeight: "1.4",
                      color: "var(--color-text-secondary)",
                      fontStyle: "italic"
                    }}>
                      "{activePreviewRequest.description}"
                    </div>
                  </div>
                )}

                {/* ========================================================
                   🔥 MODAL ASSIGNMENT CONTROLS
                   ======================================================== */}
                <div style={{
                  padding: "14px",
                  backgroundColor: "var(--bg-input)",
                  borderRadius: "var(--radius-md)",
                  border: "1px solid var(--border-color)",
                  display: "flex",
                  flexDirection: "column",
                  gap: "14px"
                }}>
                  {/* Category Assignment Select */}
                  <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                    <label style={{ fontSize: "0.7rem", fontWeight: "800", color: "var(--color-text-secondary)", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                      Assign Service Category
                    </label>
                    <select
                      value={categoryAssignments[activePreviewRequest.id] || ""}
                      onChange={(e) => setCategoryAssignments(prev => ({ ...prev, [activePreviewRequest.id]: e.target.value }))}
                      className="lbo-input"
                      style={{ padding: "8px 12px", height: "38px", fontSize: "0.85rem", backgroundColor: "var(--bg-card)" }}
                    >
                      {STANDARD_CATEGORIES.map(cat => (
                        <option key={cat} value={cat}>{cat}</option>
                      ))}
                    </select>
                  </div>

                  {/* Cluster Assignment Select */}
                  <div style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <label style={{ fontSize: "0.7rem", fontWeight: "800", color: "var(--color-text-secondary)", textTransform: "uppercase", letterSpacing: "0.05em" }}>
                        Assign Location Cluster
                      </label>
                      {getSuggestedCluster(activePreviewRequest) && (
                        <span style={{ color: "var(--color-success)", fontSize: "0.6rem", fontWeight: "800" }}>
                          ★ Auto: {getSuggestedCluster(activePreviewRequest)}
                        </span>
                      )}
                    </div>
                    <select
                      value={clusterAssignments[activePreviewRequest.id] || ""}
                      onChange={(e) => setClusterAssignments(prev => ({ ...prev, [activePreviewRequest.id]: e.target.value }))}
                      className="lbo-input"
                      style={{ padding: "8px 12px", height: "38px", fontSize: "0.85rem", backgroundColor: "var(--bg-card)" }}
                    >
                      {STANDARD_CLUSTERS.map(clus => (
                        <option key={clus} value={clus}>{clus} Cluster</option>
                      ))}
                    </select>
                  </div>
                </div>

                {/* Verification Controls inside Modal */}
                <div style={{ marginTop: "auto", display: "flex", flexDirection: "column", gap: "10px" }}>
                  <div style={{ display: "flex", gap: "10px" }}>
                    <button
                      onClick={() => approve(activePreviewRequest.userId, activePreviewRequest.id, activePreviewRequest.name)}
                      className="lbo-btn lbo-btn-success"
                      style={{ flex: 1, padding: "12px", fontSize: "0.9rem" }}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <polyline points="20 6 9 17 4 12"></polyline>
                      </svg>
                      <span>Approve</span>
                    </button>

                    <button
                      onClick={() => reject(activePreviewRequest.id, activePreviewRequest.name)}
                      className="lbo-btn lbo-btn-danger"
                      style={{ flex: 1, padding: "12px", fontSize: "0.9rem" }}
                    >
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                        <line x1="18" y1="6" x2="6" y2="18"></line>
                        <line x1="6" y1="6" x2="18" y2="18"></line>
                      </svg>
                      <span>Reject</span>
                    </button>
                  </div>

                  {activePreviewRequest.verificationDocUrl && (
                    <a
                      href={activePreviewRequest.verificationDocUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="lbo-btn lbo-btn-secondary"
                      style={{ width: "100%", justifyContent: "center", textDecoration: "none", fontSize: "0.8rem", padding: "8px" }}
                    >
                      <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path>
                        <polyline points="15 3 21 3 21 9"></polyline>
                        <line x1="10" y1="14" x2="21" y2="3"></line>
                      </svg>
                      <span>Open in External Tab</span>
                    </a>
                  )}
                </div>

              </div>
            </div>

          </div>
        </div>
      )}

      {/* ====================================================================
         🔥 DYNAMIC SYSTEM ANALYTICS MODAL OVERLAY
         ==================================================================== */}
      {analyticsModalOpen && (
        <div className="modal-overlay flex-center" onClick={() => setAnalyticsModalOpen(false)}>
          <div
            className="modal-content-container animate-scale-in"
            onClick={(e) => e.stopPropagation()}
            style={{
              maxWidth: "800px",
              height: "80vh",
              display: "flex",
              flexDirection: "column"
            }}
          >
            {/* Modal Header */}
            <div style={{
              padding: "20px 24px",
              borderBottom: "1px solid var(--border-color)",
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              background: "var(--bg-card)"
            }}>
              <div>
                <span style={{ fontSize: "0.75rem", fontWeight: "800", textTransform: "uppercase", letterSpacing: "0.05em", color: "var(--color-text-muted)" }}>
                  LBO Marketplace Metrics
                </span>
                <h3 style={{ fontSize: "1.25rem", fontWeight: "800" }}>
                  Core System Analytics
                </h3>
              </div>

              <button
                onClick={() => setAnalyticsModalOpen(false)}
                className="lbo-btn lbo-btn-secondary"
                style={{
                  width: "36px",
                  height: "36px",
                  padding: 0,
                  borderRadius: "50%",
                  justifyContent: "center"
                }}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>

            {/* Modal Body */}
            <div style={{ flex: 1, overflowY: "auto", padding: "24px", display: "flex", flexDirection: "column", gap: "24px" }}>
              {analyticsLoading ? (
                <div className="flex-center" style={{ minHeight: "300px", flexDirection: "column", gap: "16px" }}>
                  <div style={{
                    width: "40px",
                    height: "40px",
                    border: "3.5px solid var(--border-color)",
                    borderTop: "3.5px solid var(--color-primary)",
                    borderRadius: "50%",
                    animation: "App-logo-spin 1s linear infinite"
                  }} />
                  <span style={{ color: "var(--color-text-secondary)", fontWeight: "600", fontSize: "0.9rem" }}>Fetching Firestore database metrics...</span>
                </div>
              ) : (
                <>
                  {/* Summary Counters Grid */}
                  <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(160px, 1fr))", gap: "16px" }}>
                    {/* User Card */}
                    <div style={{ padding: "16px", backgroundColor: "var(--bg-input)", borderRadius: "var(--radius-md)", border: "1px solid var(--border-color)" }}>
                      <span style={{ fontSize: "0.75rem", fontWeight: "800", color: "var(--color-text-muted)", textTransform: "uppercase" }}>Total Users</span>
                      <h4 style={{ fontSize: "1.75rem", fontWeight: "800", marginTop: "4px" }}>{analyticsData.totalUsers}</h4>
                    </div>

                    {/* Providers Card */}
                    <div style={{ padding: "16px", backgroundColor: "var(--bg-input)", borderRadius: "var(--radius-md)", border: "1px solid var(--border-color)" }}>
                      <span style={{ fontSize: "0.75rem", fontWeight: "800", color: "var(--color-success)", textTransform: "uppercase" }}>Providers</span>
                      <h4 style={{ fontSize: "1.75rem", fontWeight: "800", marginTop: "4px", color: "var(--color-success)" }}>{analyticsData.providersCount}</h4>
                    </div>

                    {/* Customers Card */}
                    <div style={{ padding: "16px", backgroundColor: "var(--bg-input)", borderRadius: "var(--radius-md)", border: "1px solid var(--border-color)" }}>
                      <span style={{ fontSize: "0.75rem", fontWeight: "800", color: "var(--color-info)", textTransform: "uppercase" }}>Customers</span>
                      <h4 style={{ fontSize: "1.75rem", fontWeight: "800", marginTop: "4px", color: "var(--color-info)" }}>{analyticsData.clientsCount}</h4>
                    </div>

                    {/* Applications Count */}
                    <div style={{ padding: "16px", backgroundColor: "var(--bg-input)", borderRadius: "var(--radius-md)", border: "1px solid var(--border-color)" }}>
                      <span style={{ fontSize: "0.75rem", fontWeight: "800", color: "var(--color-warning)", textTransform: "uppercase" }}>Pending Verifs</span>
                      <h4 style={{ fontSize: "1.75rem", fontWeight: "800", marginTop: "4px", color: "var(--color-warning)" }}>{analyticsData.pendingCount}</h4>
                    </div>
                  </div>

                  {/* Distribution Columns */}
                  <div style={{ display: "grid", gridTemplateColumns: window.innerWidth < 700 ? "1fr" : "1fr 1fr", gap: "24px" }}>

                    {/* Categories Bar Distribution Chart */}
                    <div style={{ padding: "16px", backgroundColor: "var(--bg-input)", borderRadius: "var(--radius-md)", border: "1px solid var(--border-color)", display: "flex", flexDirection: "column", gap: "12px" }}>
                      <h4 style={{ fontSize: "0.95rem", fontWeight: "800", textTransform: "uppercase", letterSpacing: "0.03em" }}>Service Categories</h4>
                      {Object.keys(analyticsData.categoryCounts).length === 0 ? (
                        <span style={{ fontSize: "0.85rem", color: "var(--color-text-muted)" }}>No approved providers cataloged yet</span>
                      ) : (
                        <div style={{ display: "flex", flexDirection: "column", gap: "10px", maxHeight: "200px", overflowY: "auto", paddingRight: "4px" }}>
                          {Object.entries(analyticsData.categoryCounts)
                            .sort((a, b) => b[1] - a[1])
                            .map(([cat, val]) => {
                              const pct = Math.min(100, Math.round((val / analyticsData.providersCount) * 100));
                              return (
                                <div key={cat} style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                                  <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.75rem", fontWeight: "700" }}>
                                    <span>{cat}</span>
                                    <span>{val} ({pct}%)</span>
                                  </div>
                                  <div style={{ height: "6px", width: "100%", backgroundColor: "var(--border-color)", borderRadius: "3px", overflow: "hidden" }}>
                                    <div style={{ height: "100%", width: `${pct}%`, backgroundColor: "var(--color-primary)" }} />
                                  </div>
                                </div>
                              );
                            })}
                        </div>
                      )}
                    </div>

                    {/* Clusters Bar Distribution Chart */}
                    <div style={{ padding: "16px", backgroundColor: "var(--bg-input)", borderRadius: "var(--radius-md)", border: "1px solid var(--border-color)", display: "flex", flexDirection: "column", gap: "12px" }}>
                      <h4 style={{ fontSize: "0.95rem", fontWeight: "800", textTransform: "uppercase", letterSpacing: "0.03em" }}>Location Clusters</h4>
                      <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                        {STANDARD_CLUSTERS.map(clus => {
                          const val = analyticsData.clusterCounts[clus] || 0;
                          const pct = analyticsData.providersCount > 0 ? Math.min(100, Math.round((val / analyticsData.providersCount) * 100)) : 0;
                          return (
                            <div key={clus} style={{ display: "flex", flexDirection: "column", gap: "4px" }}>
                              <div style={{ display: "flex", justifyContent: "space-between", fontSize: "0.75rem", fontWeight: "700" }}>
                                <span>{clus} Cluster</span>
                                <span>{val} ({pct}%)</span>
                              </div>
                              <div style={{ height: "6px", width: "100%", backgroundColor: "var(--border-color)", borderRadius: "3px", overflow: "hidden" }}>
                                <div style={{ height: "100%", width: `${pct}%`, backgroundColor: "var(--color-success)" }} />
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>

                  </div>

                  {/* ========================================================
                     🔥 ADMIN APPROVAL AUDIT LOGS (AUDITING LOG)
                     ======================================================== */}
                  <div style={{ padding: "16px", backgroundColor: "var(--bg-input)", borderRadius: "var(--radius-md)", border: "1px solid var(--border-color)" }}>
                    <h4 style={{ fontSize: "0.95rem", fontWeight: "800", textTransform: "uppercase", letterSpacing: "0.03em", marginBottom: "12px" }}>
                      Admin Verification Audit Logs
                    </h4>

                    {analyticsData.auditLogs.length === 0 ? (
                      <p style={{ fontSize: "0.85rem", color: "var(--color-text-muted)", padding: "12px", textAlign: "center" }}>
                        No audit logs captured. Verifications will show up here once approved.
                      </p>
                    ) : (
                      <div style={{ display: "flex", flexDirection: "column", gap: "1px", backgroundColor: "var(--border-color)", maxHeight: "250px", overflowY: "auto", borderRadius: "var(--radius-sm)" }}>
                        {analyticsData.auditLogs.map((log) => (
                          <div
                            key={log.id}
                            style={{
                              display: "flex",
                              justifyContent: "space-between",
                              alignItems: "center",
                              padding: "12px 16px",
                              backgroundColor: "var(--bg-card)",
                              gap: "12px"
                            }}
                          >
                            <div style={{ display: "flex", flexDirection: "column" }}>
                              <span style={{ fontSize: "0.85rem", fontWeight: "700", color: "var(--color-text-primary)" }}>{log.name}</span>
                              <span style={{ fontSize: "0.75rem", color: "var(--color-text-muted)" }}>
                                Category: <b>{log.serviceType}</b> | Cluster: <b>{log.cluster || "Sangli"}</b>
                              </span>
                            </div>
                            <div style={{ textAlign: "right", display: "flex", flexDirection: "column", alignItems: "flex-end" }}>
                              <span style={{ fontSize: "0.8rem", fontWeight: "600", color: "var(--color-text-primary)" }}>
                                Approved by: <span style={{ color: "var(--color-info)", fontWeight: "700" }}>{log.approvedByEmail || "System Admin"}</span>
                              </span>
                              <span style={{ fontSize: "0.7rem", color: "var(--color-text-muted)" }}>
                                {log.approvedAt ? new Date(log.approvedAt).toLocaleString() : ""}
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                </>
              )}
            </div>

            {/* Modal Footer */}
            <div style={{
              padding: "16px 24px",
              borderTop: "1px solid var(--border-color)",
              display: "flex",
              justifyContent: "flex-end",
              background: "var(--bg-card)"
            }}>
              <button
                onClick={() => setAnalyticsModalOpen(false)}
                className="lbo-btn lbo-btn-primary"
                style={{ padding: "10px 24px" }}
              >
                Close Metrics Board
              </button>
            </div>

          </div>
        </div>
      )}

    </div>
  );
}

export default RequestList;