package com.you.chuang.new_mvi.bean

data class UserProfile(
    val avatarUrl: String = "",   // 头像URL（demo使用文字代替）
    val nickname: String,
    val bio: String
)

data class Dynamic(
    val id: Long,
    val content: String,
    val publishTime: String
)