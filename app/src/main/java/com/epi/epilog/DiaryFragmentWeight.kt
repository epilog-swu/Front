package com.epi.epilog

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import java.io.IOException

class DiaryFragmentWeight : Fragment() {

    private var weightEditText: EditText? = null
    private var bodyFatEditText: EditText? = null
    private var uploadImageView: EditText? = null
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.diary_fragment_weight, container, false)

        weightEditText = view.findViewById(R.id.weightEditText)
        bodyFatEditText = view.findViewById(R.id.bodyFatEditText)
        uploadImageView = view.findViewById(R.id.uploadImageView)

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
                uploadImageView?.background = BitmapDrawable(resources, bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun isFilledOut(): Boolean {
        val weight = weightEditText?.text?.toString() ?: ""
        val bodyFat = bodyFatEditText?.text?.toString() ?: ""
        return (weight.isNotEmpty() && weight != "80") || (bodyFat.isNotEmpty() && bodyFat != "30")
    }

}
