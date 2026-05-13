package com.you.chuang.new_mvi.store
import com.you.chuang.new_mvi.bean.Dynamic
import com.you.chuang.new_mvi.bean.UserProfile
import com.you.chuang.new_mvi.mvibase.FastMviStore
import kotlinx.coroutines.delay

class ProfileStore : FastMviStore<ProfileStore.Intent, ProfileStore.State, ProfileStore.Effect>(
    initialState = State()
) {

    sealed class Intent {
        object LoadData : Intent()          // 加载用户信息 + 动态列表
        object Refresh : Intent()           // 刷新（重新加载）
    }

    data class State(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,   // 下拉刷新状态
        val error: String? = null,
        val userProfile: UserProfile? = null,
        val dynamics: List<Dynamic> = emptyList()
    )

    sealed class Effect {
        data class ShowToast(val message: String) : Effect()
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.LoadData -> loadData(isRefresh = false)
            Intent.Refresh -> loadData(isRefresh = true)
        }
    }

    private suspend fun loadData(isRefresh: Boolean) {
        // 1. 更新加载状态
        if (isRefresh) {
            updateState { copy(isRefreshing = true, error = null) }
        } else {
            updateState { copy(isLoading = true, error = null) }
        }

        // 2. 模拟网络请求（并行请求用户信息和动态列表）
        val result = runCatching {
            // 模拟耗时 1.5 秒
            delay(1500)
            // 模拟成功数据
            val profile = UserProfile(
                avatarUrl = "https://example.com/avatar.png",
                nickname = "小明同学",
                bio = "热爱编程与写作"
            )
            val dynamics = listOf(
                Dynamic(1, "今天学习了 MVI 架构，收获很大！", "2分钟前"),
                Dynamic(2, "Kotlin Flow 真好用", "昨天"),
                Dynamic(3, "分享一个开源项目链接", "3天前")
            )
            Pair(profile, dynamics)
        }

        // 3. 处理结果
        result.onSuccess { (profile, dynamics) ->
            updateState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = null,
                    userProfile = profile,
                    dynamics = dynamics
                )
            }
        }.onFailure { e ->
            updateState {
                copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = "加载失败：${e.message}",
                    userProfile = null,
                    dynamics = emptyList()
                )
            }
            emitEffect(Effect.ShowToast("网络异常，请稍后重试"))
        }
    }
}