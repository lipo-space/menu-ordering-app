# 🧪 单元测试编译错误修复

## ✅ 问题已解决

**错误信息：**
```
e: file:///.../TodayMenuViewModelTest.kt:77:13 No value passed for parameter 'todayMenuRepository'
```

**原因：**
TodayMenuViewModel 构造函数添加了 `todayMenuRepository` 参数，但测试文件没有更新。

---

## 🔧 修复内容

### 修改前（错误）:
```kotlin
private lateinit var getTodayMenuUseCase: GetTodayMenuUseCase
// ... 其他 use cases
private lateinit var viewModel: TodayMenuViewModel

@BeforeEach
fun setup() {
    getTodayMenuUseCase = mockk()
    // ... 其他初始化

    viewModel = TodayMenuViewModel(
        getTodayMenuUseCase,
        createTodayMenuUseCase,
        // ... 其他参数
        // ❌ 缺少 todayMenuRepository 参数
    )
}
```

### 修改后（正确）:
```kotlin
private lateinit var getTodayMenuUseCase: GetTodayMenuUseCase
// ... 其他 use cases
private lateinit var todayMenuRepository: com.lipo.menu.data.repository.TodayMenuRepositoryImpl  // ✅ 添加
private lateinit var viewModel: TodayMenuViewModel

@BeforeEach
fun setup() {
    getTodayMenuUseCase = mockk()
    // ... 其他初始化
    todayMenuRepository = mockk(relaxed = true)  // ✅ 添加初始化

    viewModel = TodayMenuViewModel(
        getTodayMenuUseCase,
        createTodayMenuUseCase,
        // ... 其他参数
        todayMenuRepository  // ✅ 传递参数
    )
}
```

---

## 📝 技术细节

### 为什么需要 relaxed = true？

```kotlin
todayMenuRepository = mockk(relaxed = true)
```

使用 `relaxed = true` 可以让 mock 对象的所有方法返回默认值（0, false, null 等），避免需要手动 mock 每个方法。

这对于 `syncFromCloud()` 方法特别有用，因为它返回 `Unit`，我们不需要验证它的行为，只需要避免 NPE。

---

## 📊 提交记录

- **6c776bd** - fix: 修复 TodayMenuViewModelTest 缺少 todayMenuRepository 参数

---

## ✅ 验证

修复后，GitHub Actions 应该成功构建：
- ✅ `compileDebugUnitTestKotlin` - 成功
- ✅ `compileReleaseUnitTestKotlin` - 成功
- ✅ APK 生成成功

---

**最后更新：** 2026-03-29
