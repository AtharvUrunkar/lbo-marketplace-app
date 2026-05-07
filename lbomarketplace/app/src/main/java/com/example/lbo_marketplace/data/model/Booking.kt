data class Booking(
    val id: String = "",
    val userId: String = "",
    val providerId: String = "",
    val problem: String = "",
    val address: String = "",
    val status: String = "PENDING",
    val price: Int = 0,
    val providerPhone: String = ""
)