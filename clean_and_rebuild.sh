#!/bin/bash

echo "=== 清理项目构建文件 ==="
rm -rf app/build
rm -rf build
rm -rf .gradle
rm -rf app/.gradle
rm -rf .idea

echo "=== 清理完成 ==="
echo "请在 Android Studio 中执行以下操作："
echo "1. File -> Invalidate Caches / Restart -> Invalidate and Restart"
echo "2. 等待 IDE 重启"
echo "3. Build -> Clean Project"
echo "4. Build -> Rebuild Project"
echo "5. File -> Sync Project with Gradle Files"
