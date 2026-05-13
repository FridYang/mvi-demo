package com.you.chuang.new_mvi.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputEditText
import com.you.chuang.new_mvi.R
import com.you.chuang.new_mvi.store.LoginStore
import kotlinx.coroutines.launch
import kotlin.getValue

class LoginActivity : AppCompatActivity() {
    private val store: LoginStore by viewModels()
    private lateinit var etPhone: TextInputEditText
    private lateinit var etCode: TextInputEditText
    private lateinit var btnRequestCode: Button
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private var isLoggedIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isLoggedIn) {
            setContentView(R.layout.activity_home)
            return
        }
        setContentView(R.layout.activity_login)
        initViews()
        observeStateAndEffect()
        setupListeners()
    }

    private fun initViews() {
        etPhone = findViewById(R.id.etPhone)
        etCode = findViewById(R.id.etCode)
        btnRequestCode = findViewById(R.id.btnRequestCode)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
    }

    private fun observeStateAndEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.state.collect { state ->
                    updateUI(state)
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.effect.collect { effect ->
                    when (effect) {
                        is LoginStore.Effect.ShowToast -> {
                            Toast.makeText(this@LoginActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }
                        LoginStore.Effect.NavigateToHome -> navigateToHome()
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        etPhone.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                store.sendIntent(LoginStore.Intent.UpdatePhone(s.toString()))
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        etCode.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                store.sendIntent(LoginStore.Intent.UpdateCode(s.toString()))
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        btnRequestCode.setOnClickListener {
            store.sendIntent(LoginStore.Intent.RequestCode)
        }
        btnLogin.setOnClickListener {
            store.sendIntent(LoginStore.Intent.Login)
        }
    }

    private fun updateUI(state: LoginStore.State) {
        if (etPhone.text.toString() != state.phone) {
            etPhone.setText(state.phone)
        }
        if (etCode.text.toString() != state.code) {
            etCode.setText(state.code)
        }
        btnRequestCode.isEnabled = state.isRequestCodeEnabled
        btnRequestCode.text = if (state.countdown > 0) "${state.countdown}秒" else "获取验证码"
        btnLogin.isEnabled = !state.isLoading
        btnLogin.text = if (state.isLoading) "登录中..." else "登录"
        if (state.errorMessage != null) {
            tvError.text = state.errorMessage
            tvError.visibility = android.view.View.VISIBLE
        } else {
            tvError.visibility = android.view.View.GONE
        }
    }

    private fun navigateToHome() {
        isLoggedIn = true
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}