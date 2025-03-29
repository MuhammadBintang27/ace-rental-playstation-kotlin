import kotlinx.serialization.Serializable

@Serializable
data class PlayStation(
    val id: Int,
    val nomorUnit: String,
    val status: String,
    val tipeMain: String,
    val waktuMulai: String?,
    val waktuSelesai: String?
)
