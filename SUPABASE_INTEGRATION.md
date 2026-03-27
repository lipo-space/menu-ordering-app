# Supabase 集成配置指南

## ✅ 已完成的集成步骤

1. ✅ 添加 Supabase 依赖到 `gradle/libs.versions.toml`
2. ✅ 启用 Supabase 依赖在 `app/build.gradle.kts`
3. ✅ 创建 `SupabaseConfig.kt` 配置类
4. ✅ 创建 `DishRemoteDataSource.kt` 远程数据源
5. ✅ 添加 ISO8601 时间格式转换到 `DateUtils.kt`
6. ✅ 修改 `DishRepositoryImpl.kt` 实现实时同步
7. ✅ 创建 `SupabaseModule.kt` DI 模块

---

## 🔧 下一步：配置 Supabase 连接

### 第一步：填写 Supabase 配置信息

打开文件：`app/src/main/java/com/lipo/menu/data/remote/SupabaseConfig.kt`

将以下占位符替换为你的实际值：

```kotlin
companion object {
    private const val SUPABASE_URL = "https://你的项目ID.supabase.co"  // 从 Supabase Dashboard 获取
    private const val SUPABASE_ANON_KEY = "你的anon公钥"  // 从 Settings → API 获取
}
```

**如何获取这些值**：
1. 访问 Supabase Dashboard: https://supabase.com/dashboard
2. 选择你的项目
3. 进入 Settings → API
4. 复制：
   - **Project URL** → `SUPABASE_URL`
   - **anon public** key → `SUPABASE_ANON_KEY`

⚠️ **安全提示**：
- `anon key` 可以在客户端使用，是公开的
- **永远不要**在客户端代码中使用 `service_role` key

---

### 第二步：编译项目

在 Android Studio 中：
1. 点击 **Build** → **Clean Project**
2. 点击 **Build** → **Rebuild Project**
3. 等待编译完成（首次可能需要几分钟）

**如果遇到编译错误**：
```bash
# 在项目根目录执行
./gradlew clean build
```

---

### 第三步：测试数据同步

1. **运行应用**：点击 Run 按钮（绿色三角形）
2. **添加测试菜品**：
   - 点击"菜品"标签
   - 点击右下角的 "+" 按钮
   - 输入："测试菜品 - Supabase 同步"
   - 点击"确定"

3. **验证同步成功**：
   - 打开 Supabase Dashboard
   - 进入 Table Editor → dishes
   - 应该看到新添加的菜品

4. **检查日志**：
```bash
# 在 Android Studio Logcat 中过滤
tag:Supabase
```

---

## 🔍 故障排查

### 问题 1：编译失败 - Unresolved reference

**解决方案**：
1. 确保 Gradle Sync 成功
2. 检查网络连接（可能需要下载依赖）
3. 清理并重新编译：
```bash
./gradlew clean
./gradlew build
```

### 问题 2：运行时崩溃 - SupabaseConfig 错误

**可能原因**：
- SUPABASE_URL 或 SUPABASE_ANON_KEY 为空或错误

**解决方案**：
1. 检查 `SupabaseConfig.kt` 中的值
2. 确保 URL 格式正确：`https://xxxxxx.supabase.co`
3. 确保 anon key 是完整的 JWT 格式

### 问题 3：数据没有同步到 Supabase

**检查清单**：
- [ ] 网络连接正常
- [ ] Supabase 项目正在运行（Dashboard 中查看）
- [ ] 数据库表已创建（执行了 `supabase-schema.sql`）
- [ ] RLS 策略正确配置

**查看详细日志**：
```kotlin
// 在 DishRemoteDataSource 中添加日志
try {
    postgrest["dishes"].upsert(...)
    Log.d("Supabase", "Sync success: ${dish.name}")
} catch (e: Exception) {
    Log.e("Supabase", "Sync failed", e)
    throw RemoteDataSourceException("Failed to upsert dish: ${e.message}", e)
}
```

---

## 📊 实时同步策略说明

当前实现的同步策略：**实时同步（Real-time Sync）**

**工作流程**：
```
用户添加菜品
    ↓
1. 立即同步到 Supabase（等待响应）
    ↓
2. 保存到本地 Room 数据库
    ↓
3. 返回成功
```

**优点**：
- ✅ 数据一致性高
- ✅ 云端总是最新的
- ✅ 简单直接

**缺点**：
- ⚠️ 需要网络连接
- ⚠️ 操作稍慢（网络延迟）

---

## 🎯 下一步计划

完成 Supabase 集成后，下一步是：

1. **部署 Vercel API** - 创建定时任务自动生成菜单
2. **配置 GitHub Actions** - 自动化 CI/CD
3. **优化同步逻辑** - 添加离线支持和冲突解决

---

## 📝 代码示例

### 在其他 Repository 中使用 Supabase

参考 `DishRepositoryImpl.kt` 的实现：

```kotlin
class CombinationRepositoryImpl @Inject constructor(
    private val combinationDao: CombinationDao,
    private val combinationRemoteDataSource: CombinationRemoteDataSource // 添加远程数据源
) : CombinationRepository {

    override suspend fun createCombination(...): Result<Combination> {
        return try {
            val combination = Combination(...)

            // 1. 同步到云端
            combinationRemoteDataSource.upsertCombination(combination)

            // 2. 保存到本地
            combinationDao.insertCombination(combination.toEntity())

            Result.success(combination)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 🔗 相关文件

| 文件 | 说明 |
|------|------|
| `SupabaseConfig.kt` | Supabase 客户端配置 |
| `DishRemoteDataSource.kt` | 菜品远程数据源 |
| `DishRepositoryImpl.kt` | 已修改，添加同步逻辑 |
| `SupabaseModule.kt` | Hilt DI 模块 |
| `supabase-schema.sql` | 数据库表结构脚本 |
| `SUPABASE_SETUP.md` | Supabase 配置指南 |

---

## ✅ 完成检查清单

- [ ] 已填写 `SUPABASE_URL`
- [ ] 已填写 `SUPABASE_ANON_KEY`
- [ ] 项目编译成功
- [ ] 应用运行成功
- [ ] 添加菜品后能在 Supabase Dashboard 看到数据
- [ ] Logcat 没有错误日志

完成后，你就可以继续进行 Vercel 部署了！🎉
