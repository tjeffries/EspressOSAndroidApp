package com.example.espressosapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.espressosapp.ui.theme.EspressOSAppTheme
import kotlin.math.pow
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {

    private val devId: String = "GURU@TEST1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Instantiate the RequestQueue for all HTTP functions
            val queue = Volley.newRequestQueue(this)
            val state = ConfigState()

            getConfigValues(queue, devId, state)

            EspressOSAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.background) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "EspressOS Configuration v0.11\n")


                        Text(text = "Target Pressure: ${state.pumpTarget.value.roundDecimal(1)}")
                        Slider(
                            value = state.pumpTarget.value,
                            onValueChange = { state.pumpTarget.value = it },
                            valueRange = 0f..30f)

                        Text(text = "P: ${state.p.value.roundDecimal(1)}")
                        Slider(
                            value = state.p.value,
                            onValueChange = { state.p.value = it },
                            valueRange = 0f..10f)

                        Text(text = "I: ${state.i.value.roundDecimal(1)}")
                        Slider(
                            value = state.i.value,
                            onValueChange = { state.i.value = it },
                            valueRange = 0f..10f)

                        Text(text = "D: ${state.d.value.roundDecimal(1)}")
                        Slider(
                            value = state.d.value,
                            onValueChange = { state.d.value = it },
                            valueRange = 0f..10f)

                        Button(onClick = { updateClick(queue, devId, state) }) {
                            Text(text = "Upload settings")
                        }
                        Text(text = state.resp.value)
                    }
                }
            }
        }
    }
    private fun getConfigValues(queue: RequestQueue, devId: String, state: ConfigState) {
        val url = "http://35.225.145.147/set?device_id=$devId"
        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val respAry: List<String> = response.split(" | ")
                state.update(
                    p = respAry[2].toFloat(),
                    i = respAry[3].toFloat(),
                    d = respAry[4].toFloat(),
                    pumpTarget = respAry[5].toFloat(),
                    resp = response
                )
                Toast.makeText(this, "Settings loaded from server", Toast.LENGTH_SHORT).show()
            },
            {
                /*
                state.value.resp = it.toString()
                 */
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun updateClick(queue: RequestQueue, devId: String, state: ConfigState) {
        val url = "http://35.225.145.147/set?device_id=$devId&p_p=${state.p.value.roundDecimal(1)}&p_i=${state.i.value.roundDecimal(1)}&p_d=${state.d.value.roundDecimal(1)}&p_target=${state.pumpTarget.value.roundDecimal(1)}"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                Toast.makeText(this, "Uploaded", Toast.LENGTH_SHORT).show()
                state.update(resp = response)
            },
            {
                Toast.makeText(this, "Error!", Toast.LENGTH_LONG).show()
                state.update(resp = it.toString())
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    class ConfigState(
        var pumpTarget: MutableState<Float> = mutableStateOf(1f),
        var p: MutableState<Float> = mutableStateOf(1f),
        var i: MutableState<Float> = mutableStateOf(1f),
        var d: MutableState<Float> = mutableStateOf(1f),
        var resp: MutableState<String> = mutableStateOf("null")
    ){
        fun update(pumpTarget: Float? = null, p: Float? = null, i: Float? = null, d: Float? = null, resp: String? = null){
            if(pumpTarget != null) this.pumpTarget.value = pumpTarget
            if(p != null) this.p.value = p
            if(i != null) this.i.value = i
            if(d != null) this.d.value = d
            if(resp != null) this.resp.value = resp
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EspressOSAppTheme {
        Greeting("Android")
    }
}
fun Float.roundDecimal(decimals: Int): Float {
    val multiplier =  10f.pow(decimals)
    return (this * multiplier).roundToInt() / multiplier
}