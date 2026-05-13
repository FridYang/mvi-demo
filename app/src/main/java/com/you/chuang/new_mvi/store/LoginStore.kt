package com.you.chuang.new_mvi.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginStore : ViewModel() {

    // ========== MVI 契约（内部类） ==========
    sealed class Intent {
        data class UpdatePhone(val phone: String) : Intent()
        data class UpdateCode(val code: String) : Intent()
        object RequestCode : Intent()
        object Login : Intent()
    }

    data class State(
        val phone: String = "",
        val code: String = "",
        val isRequestCodeEnabled: Boolean = true,
        val countdown: Int = 0,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
        object NavigateToHome : Effect()
    }

    // ========== Store 实现 ==========
    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>()
    val effect: SharedFlow<Effect> = _effect.asSharedFlow()

    fun sendIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdatePhone -> updatePhone(intent.phone)
            is Intent.UpdateCode -> updateCode(intent.code)
            Intent.RequestCode -> requestCode()
            Intent.Login -> login()
        }
    }

    private fun updatePhone(phone: String) {
        _state.update { it.copy(phone = phone, errorMessage = null) }
    }

    private fun updateCode(code: String) {
        _state.update { it.copy(code = code, errorMessage = null) }
    }

    private fun requestCode() {
        val currentState = _state.value
        if (!isValidPhone(currentState.phone)) {
            emitEffect(Effect.ShowToast("请输入正确的11位手机号"))
            return
        }
        _state.update { it.copy(isRequestCodeEnabled = false, countdown = 60) }

        viewModelScope.launch {
            // 模拟发送验证码 API
            delay(1000)
            emitEffect(Effect.ShowToast("验证码已发送（演示：123456）"))
            startCountdown()
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            for (i in 60 downTo 1) {
                _state.update { it.copy(countdown = i) }
                delay(1000)
            }
            _state.update { it.copy(isRequestCodeEnabled = true, countdown = 0) }
        }
    }

    private fun login() {
        val currentState = _state.value
        if (!isValidPhone(currentState.phone)) {
            emitEffect(Effect.ShowToast("手机号不正确"))
            return
        }
        if (currentState.code.length != 6) {
            emitEffect(Effect.ShowToast("验证码必须为6位"))
            return
        }

        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            delay(2000) // 模拟登录 API
            val success = currentState.code == "123456"
            if (success) {
                _state.update { it.copy(isLoading = false) }
                emitEffect(Effect.NavigateToHome)
            } else {
                _state.update {
                    it.copy(isLoading = false, errorMessage = "验证码错误")
                }
                emitEffect(Effect.ShowToast("验证码错误"))
            }
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 11 && phone.all { it.isDigit() }
    }

    private fun emitEffect(effect: Effect) {
        viewModelScope.launch { _effect.emit(effect) }
    }
}