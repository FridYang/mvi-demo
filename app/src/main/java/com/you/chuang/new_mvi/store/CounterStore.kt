package com.you.chuang.new_mvi.store

import android.util.Log
import com.you.chuang.new_mvi.mvibase.MviStore

class CounterStore : MviStore<CounterIntent, CounterState, CounterEffect>(CounterState()) {

    override fun reduce(intent: CounterIntent, currentState: CounterState): CounterState {
        Log.d("CounterStore", "⚙️ Reduce: $intent, current count = ${currentState.count}")
        return when (intent) {
            CounterIntent.Increment -> currentState.copy(count = currentState.count + 1)
            CounterIntent.Decrement -> currentState.copy(count = currentState.count - 1)
            CounterIntent.Reset -> currentState.copy(count = 0)
        }
    }

    override fun handleSideEffect(intent: CounterIntent, newState: CounterState): CounterEffect? {
        return when (intent) {
            CounterIntent.Reset -> {
                Log.d("CounterStore", "🎯 Handle side effect for Reset, new state: $newState")
                CounterEffect.ShowToast("计数器已重置")
            }
            else -> null
        }
    }
}