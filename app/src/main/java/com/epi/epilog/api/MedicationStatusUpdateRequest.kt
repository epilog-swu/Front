package com.epi.epilog.api

import com.epi.epilog.api.State

data class MedicationStatusUpdateRequest(
    val time: String,
    val status: State
)
