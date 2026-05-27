data class ProviderRequest(

    val userId: String = "",

    val email: String = "",

    val name: String = "",

    val serviceType: String = "",

    val description: String = "",

    val experience: String = "",

    // 🔥 LOCATION
    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    // 🔥 CLOUDINARY URLS
    val profileImageUrl: String = "",

    val verificationDocUrl: String = "",

    // 🔥 STATUS
    val status: String = "PENDING",

    // 🔥 TIMESTAMP
    val createdAt: Long = System.currentTimeMillis()
)