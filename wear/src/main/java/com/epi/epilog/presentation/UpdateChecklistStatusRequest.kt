package com.epi.epilog.presentation

import com.epi.epilog.presentation.medicine.State

data class UpdateChecklistStatusRequest(
    val time: String,
    val status: State?
)