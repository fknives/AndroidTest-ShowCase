package org.fnives.test.showcase.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.google.accompanist.insets.ProvideWindowInsets
import org.fnives.test.showcase.compose.screen.AppNavigation

class ComposeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestShowCaseApp()
        }
    }
}

@Composable
fun TestShowCaseApp() {
    ProvideWindowInsets {
        MaterialTheme {
            AppNavigation()
        }
    }
}
