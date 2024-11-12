package com.epi.epilog.diary

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import api.DiaryFragment
import com.epi.epilog.R
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class DiaryFragmentWeight : Fragment(), DiaryFragment {

    private var weightEditText: EditText? = null
    private var bodyFatEditText: EditText? = null
    private var uploadImageView: ImageView? = null
    private val PICK_IMAGE_REQUEST = 1
    private val IMAGE_SIZE = 140 // 크롭할 이미지의 너비와

    override fun getData(): JSONObject {
        val data = JSONObject()
        try {
            data.put("weight", weightEditText?.text.toString())
            data.put("bodyFatPercentage", bodyFatEditText?.text.toString())
            // bodyPhoto 필드를 null로 설정
            data.put("bodyPhoto", JSONObject.NULL)

        } catch (e: JSONException) {
            Log.e("final_weight_error", "JSONException in getData: ${e.message}")
        }

        Log.d("DiaryFragmentWeight", "getData: $data") // 추가된 로그

        return data
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_weight, container, false)

        weightEditText = view.findViewById(R.id.weightEditText)
        bodyFatEditText = view.findViewById(R.id.bodyFatEditText)
        uploadImageView = view.findViewById(R.id.uploadImageView)

        // 모든 EditText에 정수만 입력받도록 설정
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            if (source.isEmpty()) {
                return@InputFilter null // Allow deletion
            }
            for (i in start until end) {
                if (!Character.isDigit(source[i])) {
                    Toast.makeText(context, "정수만 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                    return@InputFilter "" // Reject non-digits and show toast
                }
            }
            null // Accept the input
        }

        weightEditText?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(inputFilter)
        }

        bodyFatEditText?.apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(inputFilter)
        }

        //TODO: 이미지 업로드 작업
        uploadImageView?.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            try {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, selectedImageUri)
                val croppedBitmap = cropBitmapToSquare(bitmap)
                val resizedBitmap = resizeBitmap(croppedBitmap, IMAGE_SIZE, IMAGE_SIZE)
                uploadImageView?.setImageBitmap(resizedBitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun cropBitmapToSquare(bitmap: Bitmap): Bitmap {
        val dimension = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - dimension) / 2
        val y = (bitmap.height - dimension) / 2
        return Bitmap.createBitmap(bitmap, x, y, dimension, dimension)
    }

    private fun resizeBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }


    override fun isFilledOut(): Boolean {
        val weight = weightEditText?.text?.toString() ?: ""
        val bodyFat = bodyFatEditText?.text?.toString() ?: ""
        return weight.isNotEmpty()  || bodyFat.isNotEmpty()
    }

}
