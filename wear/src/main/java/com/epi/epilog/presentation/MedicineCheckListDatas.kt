package com.epi.epilog.presentation

import com.google.gson.annotations.SerializedName

data class MedicineCheckListDatas(
    @SerializedName("date") val date: String,
    @SerializedName("checklist") val checklist: List<checklist>
)

data class checklist(
    @SerializedName("id")val id: Int,
    @SerializedName("goalTime")val goalTime: String,
    @SerializedName("title")val title: String,
    @SerializedName("isComplete") var isComplete: Boolean,
    @SerializedName("state")val state: State?
)


enum class State{
    복용, 미복용, 상태없음
}