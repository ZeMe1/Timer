package kz.zeme.timer.core.locale.domain

import kz.zeme.timer.core.domain.Response

interface LanguageRepository {

    suspend fun getLanguages(): Response<List<Language>>

    suspend fun saveSelectedLanguage(language: Language): Response<Unit>
}
