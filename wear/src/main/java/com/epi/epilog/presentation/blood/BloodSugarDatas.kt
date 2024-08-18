package com.epi.epilog.presentation.blood

import com.google.gson.annotations.SerializedName

data class BloodSugarDatas(
    @SerializedName("total") val total: Int,
    @SerializedName("date") val date: String,
    @SerializedName("diabetes") val diabetes: List<diabetes>
)

data class diabetes(
    @SerializedName("occurrenceType")val occurrenceType: String,
    @SerializedName("bloodSugar")val bloodSugar: Int
)