package com.lipo.menu.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
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

    companion object {
        private const val SUPABASE_URL = "https://yrredllhwswsfxvzhoka.supabase.co"
        private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlycmVkbGxod3N3c2Z4dnpob2thIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzQ2MjMxODEsImV4cCI6MjA5MDE5OTE4MX0.M21uHSqkyPJ7r2wMumjYTSdQ0225gFVlOjDrPYw_zew"
    }

    private val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Realtime)
    }

    fun getClient(): SupabaseClient = client
}
