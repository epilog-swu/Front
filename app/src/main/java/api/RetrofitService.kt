package com.epi.epilog.presentation.theme.api


import android.os.Parcel
import android.os.Parcelable
import com.epi.epilog.ApiResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    private const val BASE_URL = "http://epilog-develop-env.eba-imw3vi3g.ap-northeast-2.elasticbeanstalk.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val retrofitService: RetrofitService by lazy {
        retrofit.create(RetrofitService::class.java)
    }
}

interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<String>

    @Headers("Content-Type: application/json")
    @POST("/api/auth/signup")
    fun signUp(@Body request: SignUpRequest): Call<SignUpResponse>

    @Headers("Content-Type: application/json")
    @GET("api/auth/validation")
    fun validateId(@Query("id") userId: String): Call<ApiResponse>

    @Headers("Content-Type: application/json")
    @GET("api/medicines")
    fun getMedicationChecklist(@Query("date") date: String, @Header("Authorization") token: String): Call<MedicationChecklistResponse>

}


data class MedicationChecklistResponse(
    val date: String,
    val medicationId: Int,
    val checklist: List<ChecklistItem>
)

data class ChecklistItem(
    val id: Int,
    val goalTime: String,
    val title: String,
    val time: String,
    val medicationName: String,
    val isComplete: Boolean,
    val state: String
)

data class LoginRequest(
    val loginId: String,
    val password: String
)

data class SignUpResponse(
    val success: Boolean,
    val code: String,
    val token: String
)

data class SignUpRequest(
    val loginId: String,
    val password: String,
    val name: String,
    val stature: Float,
    val weight: Float,
    val gender: String,
    val protectorName: String,
    val protectorPhone: String,
    val medication: List<Medication>
)

data class Medication(
    val name: String,
    val times: MutableList<String>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeStringList(times)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Medication> {
        override fun createFromParcel(parcel: Parcel): Medication {
            return Medication(parcel)
        }

        override fun newArray(size: Int): Array<Medication?> {
            return arrayOfNulls(size)
        }
    }
}