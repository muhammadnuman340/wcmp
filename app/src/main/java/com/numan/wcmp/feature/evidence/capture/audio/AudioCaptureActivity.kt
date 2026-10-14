package com.numan.wcmp.feature.evidence.capture.audio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.numan.wcmp.core.evidence.AudioCaptureSession
import com.numan.wcmp.ui.theme.WCMPTheme

class AudioCaptureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )
        
        setContent {
            WCMPTheme {
                AudioCaptureScreen(
                    onFinished = {
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure the session is completed with null if activity is destroyed without successful capture
        AudioCaptureSession.complete(null)
    }
}
