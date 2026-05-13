package com.you.chuang.new_mvi

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.you.chuang.new_mvi.databinding.ActivityMainBinding
import com.you.chuang.new_mvi.store.CounterEffect
import com.you.chuang.new_mvi.store.CounterIntent
import com.you.chuang.new_mvi.store.CounterStore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val store: CounterStore by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.state.collect { state ->
                    Log.d("MainActivity", "📺 UI State updated: $state")
                    binding.tvCount.text = "当前计数：${state.count}"
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.effect.collect { effect ->
                    Log.d("MainActivity", "📢 Effect received: $effect")
                    when (effect) {
                        is CounterEffect.ShowToast -> Toast.makeText(this@MainActivity, effect.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnIncrement.setOnClickListener { store.sendIntent(CounterIntent.Increment) }
        binding.btnDecrement.setOnClickListener { store.sendIntent(CounterIntent.Decrement) }
        binding.btnReset.setOnClickListener { store.sendIntent(CounterIntent.Reset) }
    }
}