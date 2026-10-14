package com.numan.wcmp.domain.evidence

interface EvidenceStorage {
    suspend fun saveEvidenceRecord(record: EvidenceRecord, relatedEventId: String?, title: String, category: String, sessionId: String? = null)
    suspend fun deleteEvidenceRecord(id: String)
}
