package com.numan.wcmp.core.background

import org.junit.Assert.assertEquals
import org.junit.Test

class WcmpWorkSchedulerTest {

    @Test
    fun `work names are unique and correct`() {
        assertEquals("WCMP_INSTALLED_APPS_SYNC", WcmpWorkManager.SYNC_WORK_NAME)
        assertEquals("WCMP_REPOSITORY_MAINTENANCE", WcmpWorkManager.MAINTENANCE_WORK_NAME)
        assertEquals("WCMP_DIAGNOSTICS_REFRESH", WcmpWorkManager.DIAGNOSTICS_WORK_NAME)
    }
}
