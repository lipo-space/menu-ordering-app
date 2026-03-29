package com.lipo.menu

import android.app.Application
import com.lipo.menu.data.repository.DishRepositoryImpl
import com.lipo.menu.data.repository.TodayMenuRepositoryImpl
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MenuApplication : Application() {

    @Inject
    lateinit var dishRepository: DishRepositoryImpl

    @Inject
    lateinit var todayMenuRepository: TodayMenuRepositoryImpl

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // 应用启动时从云端同步数据
        applicationScope.launch {
            try {
                dishRepository.syncFromCloud()
                todayMenuRepository.syncFromCloud()
            } catch (e: Exception) {
                android.util.Log.e("MenuApplication", "Failed to sync from cloud: ${e.message}")
            }
        }
    }
}
