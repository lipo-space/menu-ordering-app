package com.lipo.menu.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.SupabaseClientBuilder
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Supabase 客户端配置
 *
 * 使用说明：
 * 1. 在 Supabase Dashboard 获取 Project URL 和 anon key
 * 2. 将下面的占位符替换为实际值
 * 3. 确保 anon key 保密，不要提交到 Git
 */
@Singleton
class SupabaseConfig @Inject constructor() {

    // TODO: 替换为你的 Supabase 配置
    // 从 .env.local 文件或 Supabase Dashboard 获取
    companion object {
        private const val SUPABASE_URL = "YOUR_SUPABASE_URL"  // 例如: https://xxxxxx.supabase.co
        private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"  // anon public key
    }

    private val client: SupabaseClient = SupabaseClientBuilder(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Realtime)
    }.build()

    fun getClient(): SupabaseClient = client
}
