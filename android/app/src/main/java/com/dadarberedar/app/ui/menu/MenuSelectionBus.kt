package com.dadarberedar.app.ui.menu

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuSelectionBus @Inject constructor() {
    private val _categoryId = MutableStateFlow<Long?>(null)
    val categoryId: StateFlow<Long?> = _categoryId.asStateFlow()

    fun select(categoryId: Long?) {
        _categoryId.value = categoryId
    }
}
