package com.numan.wcmp.domain.evidence.health

import android.content.Context
import java.io.File

interface EvidenceDirectoryProvider {
    fun getEvidenceDirectory(): File
    fun getControlledViewingDirectory(): File
}

class AndroidEvidenceDirectoryProvider(private val context: Context) : EvidenceDirectoryProvider {
    override fun getEvidenceDirectory(): File {
        return File(context.filesDir, "evidence")
    }

    override fun getControlledViewingDirectory(): File {
        return File(context.cacheDir, "controlled_viewing")
    }
}
