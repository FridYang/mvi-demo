package com.you.chuang.new_mvi.mvibase

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface MviIntent
interface MviState
interface MviSideEffect

abstract class MviStore<Intent : MviIntent, State : MviState, Effect : MviSideEffect>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    fun sendIntent(intent: Intent) {
        Log.d("MviStore", "📨 Intent received: $intent")
        viewModelScope.launch {
            val oldState = _state.value
            Log.d("MviStore", "🏁 Reduce start | Intent: $intent | Old State: $oldState")
            val newState = reduce(intent, oldState)
            Log.d("MviStore", "✅ Reduce result | New State: $newState")

            // 更新状态
            _state.update { newState }
            Log.d("MviStore", "🔄 State updated | current: ${_state.value}")

            // 处理副作用
            handleSideEffect(intent, newState)?.let { effect ->
                Log.d("MviStore", "💥 Side effect produced: $effect")
                _effect.emit(effect)
                Log.d("MviStore", "📡 Side effect emitted")
            } ?: run {
                Log.d("MviStore", "⭕ No side effect for intent: $intent")
            }
        }
    }

    protected abstract fun reduce(intent: Intent, currentState: State): State

    protected open fun handleSideEffect(intent: Intent, newState: State): Effect? = null
}