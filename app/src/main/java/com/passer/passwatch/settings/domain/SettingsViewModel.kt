package com.passer.passwatch.settings.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.passer.passwatch.core.repo.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState())
    private val _hubMacAddress = userPreferencesRepository.hubMacAddress

    val state = combine(_state, _hubMacAddress) { state, hubMacAddress ->
        state.copy(
            hubMacAddress = hubMacAddress
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun onEvent(event: SettingsEvent){
        when(event){
            is SettingsEvent.SaveMacAddress -> {
                val newMacAddress = event.newMacAddress

                viewModelScope.launch {
                    userPreferencesRepository.saveHubMacAddress(newMacAddress)
                }

                _state.update {
                    it.copy(newHubMacAddress = "")
                }
            }

            is SettingsEvent.SetMacAddress -> {
                _state.update {
                    it.copy(newHubMacAddress = event.newMacAddress)
                }
            }
        }
    }
}