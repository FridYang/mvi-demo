package com.you.chuang.new_mvi.mvibase
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class FastMviStore<Intent, State, Effect>(
    initialState: State
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    protected suspend fun updateState(update: State.() -> State) {
        _state.update { it.update() }
    }

    protected suspend fun emitEffect(effect: Effect) {
        _effect.emit(effect)
    }

    abstract suspend fun handleIntent(intent: Intent)

    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            handleIntent(intent)
        }
    }
}