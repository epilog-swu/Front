package com.epi.epilog
import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("message") val message: String,
    @SerializedName("success") val success: Boolean
)
