package com.you.chuang.new_mvi.store

import com.you.chuang.new_mvi.mvibase.MviIntent
import com.you.chuang.new_mvi.mvibase.MviSideEffect
import com.you.chuang.new_mvi.mvibase.MviState

sealed class CounterIntent : MviIntent {
    object Increment : CounterIntent()
    object Decrement : CounterIntent()
    object Reset : CounterIntent()
}

data class CounterState(
    val count: Int = 0
) : MviState

sealed class CounterEffect : MviSideEffect {
    data class ShowToast(val message: String) : CounterEffect()
}