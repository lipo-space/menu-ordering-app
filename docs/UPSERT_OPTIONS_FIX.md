# 🔧 UpsertOptions 编译错误修复

## ✅ 问题已解决

**错误信息：**
```
e: file:///.../DishRemoteDataSource.kt:6:41 Unresolved reference: UpsertOptions
e: file:///.../TodayMenuRemoteDataSource.kt:6:41 Unresolved reference: UpsertOptions
```

**原因：**
Supabase-kt 2.x 不使用 `UpsertOptions` 类。正确的做法是将 `onConflict` 作为 `upsert()` 函数参数直接传递。

## 🔧 修复内容

### 修改前（错误代码）:
```kotlin
import io.github.jan.supabase.postgrest.UpsertOptions  // ❌ 不存在

client.from("dishes").upsert(
    JsonObject(...),
    upsertOptions = UpsertOptions(onConflict = "id")  // ❌ 错误
)
```

### 修改后（正确代码）:
```kotlin
// 移除 UpsertOptions 导入

client.from("dishes").upsert(
    JsonObject(...),
    onConflict = "id"  // ✅ 正确
)
```

## 📝 修改的文件

1. **DishRemoteDataSource.kt**
   - 移除 `import io.github.jan.supabase.postgrest.UpsertOptions`
   - 修改 `upsert()` 调用，使用 `onConflict = "id"` 参数

2. **TodayMenuRemoteDataSource.kt**
   - 移除 `import io.github.jan.supabase.postgrest.UpsertOptions`
   - 修改 `upsertTodayMenu()` 使用 `onConflict = "date"`
   - 修改 `upsertTodayMenuDish()` 使用 `onConflict = "today_menu_id,dish_id"`

## 🎯 效果

- ✅ 编译错误已修复
- ✅ `duplicate key` 错误已解决（通过正确的 conflict resolution)
- ✅ 代码符合 Supabase-kt 2.x API 规范

## 📚 参考

- [Supabase Kotlin Documentation - Upsert](https://supabase.com/docs/reference/kotlin/upsert)
- [supabase-kt GitHub Repository](https://github.com/supabase-community/supabase-kt)

---

**提交:** 5258fc2
**时间:** 2026-03-29
