package com.numan.wcmp.feature.evidence.capture.location

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.numan.wcmp.core.evidence.LocationCaptureSession
import com.numan.wcmp.ui.theme.WCMPTheme

class LocationCaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        
        setContent {
            WCMPTheme {
                LocationCaptureScreen(
                    onFinished = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocationCaptureSession.complete(null)
    }
}
