data class ProviderRequest(
    val userId: String = "",
    val email: String = "",
    val name: String = "",
    val serviceType: String = "",
    val description: String = "",
    val experience: String = "",
    val latitude: Double = 0.0,   // 🔥 NEW
    val longitude: Double = 0.0,  // 🔥 NEW
    val status: String = "PENDING",
    val createdAt: Long = System.currentTimeMillis()
)