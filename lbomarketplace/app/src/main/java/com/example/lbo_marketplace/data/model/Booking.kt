data class Booking(

    val id: String = "",

    // 🔥 USER INFO
    val userId: String = "",

    val userName: String = "",

    val userPhone: String = "",

    // 🔥 PROVIDER INFO
    val providerId: String = "",

    val providerName: String = "",

    val providerPhone: String = "",

    val providerImageUrl: String = "",

    // 🔥 SERVICE DETAILS
    val serviceType: String = "",

    val problem: String = "",

    val address: String = "",

    // 🔥 BOOKING STATUS
    val status: String = "PENDING",

    // 🔥 TIMESTAMP
    val createdAt: Long = System.currentTimeMillis()
)