package com.epi.epilog.presentation

data class UpdateChecklistStatusRequest(
    val time: String,
    val status: State?
)