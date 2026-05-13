package com.you.chuang.new_mvi.ui
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.you.chuang.new_mvi.R
import com.you.chuang.new_mvi.adapter.DynamicAdapter
import com.you.chuang.new_mvi.store.ProfileStore
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private val store: ProfileStore by viewModels()

    private lateinit var btnRefresh: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var layoutUserInfo: View
    private lateinit var tvNickname: TextView
    private lateinit var tvBio: TextView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var rvDynamics: RecyclerView
    private lateinit var adapter: DynamicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        setupRecyclerView()
        observeStateAndEffect()
        setupListeners()

        // 自动加载数据
        store.sendIntent(ProfileStore.Intent.LoadData)
    }

    private fun initViews() {
        btnRefresh = findViewById(R.id.btnRefresh)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        layoutUserInfo = findViewById(R.id.layoutUserInfo)
        tvNickname = findViewById(R.id.tvNickname)
        tvBio = findViewById(R.id.tvBio)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        rvDynamics = findViewById(R.id.rvDynamics)
    }

    private fun setupRecyclerView() {
        adapter = DynamicAdapter()
        rvDynamics.layoutManager = LinearLayoutManager(this)
        rvDynamics.adapter = adapter
    }

    private fun observeStateAndEffect() {
        // 观察状态变化
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.state.collect { state ->
                    updateUI(state)
                }
            }
        }
        // 观察副作用（Toast）
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                store.effect.collect { effect ->
                    when (effect) {
                        is ProfileStore.Effect.ShowToast -> {
                            Toast.makeText(this@ProfileActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(state: ProfileStore.State) {
        // 加载状态
        progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        swipeRefresh.isRefreshing = state.isRefreshing

        // 错误处理
        if (state.error != null) {
            tvError.text = state.error
            tvError.visibility = View.VISIBLE
            layoutUserInfo.visibility = View.GONE
            rvDynamics.visibility = View.GONE
        } else {
            tvError.visibility = View.GONE
            layoutUserInfo.visibility = View.VISIBLE
            rvDynamics.visibility = View.VISIBLE

            // 用户信息
            state.userProfile?.let { profile ->
                tvNickname.text = profile.nickname
                tvBio.text = profile.bio
                // 头像示例：如果用 ImageView 可加载网络图片，这里省略
            }

            // 动态列表
            adapter.submitList(state.dynamics)
        }
    }

    private fun setupListeners() {
        btnRefresh.setOnClickListener {
            store.sendIntent(ProfileStore.Intent.Refresh)
        }
        swipeRefresh.setOnRefreshListener {
            store.sendIntent(ProfileStore.Intent.Refresh)
        }
    }
}