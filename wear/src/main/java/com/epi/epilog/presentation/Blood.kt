package com.epi.epilog.presentation

import com.google.gson.annotations.SerializedName

data class Blood(
    @SerializedName("date") val date: String,
    @SerializedName("occurrenceType") val occurrenceType: String,
    @SerializedName("bloodSugar") val bloodSugar: Int
)
