/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.epi.epilog.presentation

import android.app.ProgressDialog.show
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.epi.epilog.R
import com.epi.epilog.presentation.theme.EpilogTheme

//class MainActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()
//
//        super.onCreate(savedInstanceState)
//
//        setTheme(android.R.style.Theme_DeviceDefault)
//
//        setContent {
//            WearApp("Android")
//        }
//    }
//}
//
//@Composable
//fun WearApp(greetingName: String) {
//    EpilogTheme {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(MaterialTheme.colors.background),
//            contentAlignment = Alignment.Center
//        ) {
//            TimeText()
//            Greeting(greetingName = greetingName)
//        }
//    }
//}
//
//@Composable
//fun Greeting(greetingName: String) {
//    Text(
//        modifier = Modifier.fillMaxWidth(),
//        textAlign = TextAlign.Center,
//        color = MaterialTheme.colors.primary,
//        text = stringResource(R.string.hello_world, greetingName)
//    )
//}
//
//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearApp("Preview Android")
//}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_sugar)

        val radioGroup : RadioGroup = findViewById(R.id.blood_sugar_radio_group)
        val timePicker : TimePicker = findViewById(R.id.blood_sugar_timepicker)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.blood_sugar_btn8) {
                timePicker.visibility = View.VISIBLE
            } else {
                timePicker.visibility = View.GONE
            }
        }
    }
}