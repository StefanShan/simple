#!/bin/bash
# 首次执行，即没有修改任何代码，先全部发一遍 aar
#MODIFY_MODULE=$(echo $(./gradlew :findModifiedModule) | grep -o '\[[^][]*\]' | tr -d '[],')
# 获取当前提交的差异，找到被修改的模块
MODIFY_MODULE=$(echo $(./gradlew :findModifiedModule -PmodifyFile="$(git diff --cached --name-only | uniq)") | grep -o '\[[^][]*\]' | tr -d '[],')
echo "MODIFY_MODULE: $MODIFY_MODULE"
for module in $MODIFY_MODULE; do
    echo "module: $module"
   ./gradlew :$module:assembleRelease
   ./gradlew :configMaven -Pmodule=$module
done
./gradlew :updateConfig
