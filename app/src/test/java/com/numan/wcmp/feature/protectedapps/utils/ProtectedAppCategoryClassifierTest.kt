package com.numan.wcmp.feature.protectedapps.utils

import com.numan.wcmp.feature.protectedapps.model.ProtectedAppCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class ProtectedAppCategoryClassifierTest {

    @Test
    fun `whatsapp maps to COMMUNICATION`() {
        val category = ProtectedAppCategoryClassifier.classify("com.whatsapp", "WhatsApp")
        assertEquals(ProtectedAppCategory.COMMUNICATION, category)
    }

    @Test
    fun `gallery maps to MEDIA`() {
        val category = ProtectedAppCategoryClassifier.classify("com.android.gallery3d", "Gallery")
        assertEquals(ProtectedAppCategory.MEDIA, category)
    }

    @Test
    fun `unknown app maps to OTHER`() {
        val category = ProtectedAppCategoryClassifier.classify("com.example.calculator", "Calculator")
        assertEquals(ProtectedAppCategory.OTHER, category)
    }

    @Test
    fun `files app maps to FILES`() {
        val category = ProtectedAppCategoryClassifier.classify("com.google.android.apps.nbu.files", "Files by Google")
        assertEquals(ProtectedAppCategory.FILES, category)
    }
}
