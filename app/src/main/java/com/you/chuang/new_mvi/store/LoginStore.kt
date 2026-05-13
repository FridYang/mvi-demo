package com.you.chuang.new_mvi.store
import com.you.chuang.new_mvi.mvibase.FastMviStore
import kotlinx.coroutines.delay

class LoginStore : FastMviStore<LoginStore.Intent, LoginStore.State, LoginStore.Effect>(
    initialState = State()
) {
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

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.UpdatePhone -> updatePhone(intent.phone)
            is Intent.UpdateCode -> updateCode(intent.code)
            Intent.RequestCode -> requestCode()
            Intent.Login -> login()
        }
    }

    private suspend fun updatePhone(phone: String) {
        updateState { copy(phone = phone, errorMessage = null) }
    }

    private suspend fun updateCode(code: String) {
        updateState { copy(code = code, errorMessage = null) }
    }

    private suspend fun requestCode() {
        val currentState = state.value
        if (!isValidPhone(currentState.phone)) {
            emitEffect(Effect.ShowToast("请输入正确的11位手机号"))
            return
        }
        updateState { copy(isRequestCodeEnabled = false, countdown = 60) }

        // 模拟发送验证码
        delay(1000)
        emitEffect(Effect.ShowToast("验证码已发送（演示：123456）"))
        startCountdown()
    }

    private suspend fun startCountdown() {
        for (i in 60 downTo 1) {
            updateState { copy(countdown = i) }
            delay(1000)
        }
        updateState { copy(isRequestCodeEnabled = true, countdown = 0) }
    }

    private suspend fun login() {
        val currentState = state.value
        if (!isValidPhone(currentState.phone)) {
            emitEffect(Effect.ShowToast("手机号不正确"))
            return
        }
        if (currentState.code.length != 6) {
            emitEffect(Effect.ShowToast("验证码必须为6位"))
            return
        }

        updateState { copy(isLoading = true, errorMessage = null) }

        delay(2000) // 模拟登录
        val success = currentState.code == "123456"
        if (success) {
            updateState { copy(isLoading = false) }
            emitEffect(Effect.NavigateToHome)
        } else {
            updateState { copy(isLoading = false, errorMessage = "验证码错误") }
            emitEffect(Effect.ShowToast("验证码错误"))
        }
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length == 11 && phone.all { it.isDigit() }
    }
}