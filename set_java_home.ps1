# 设置 JAVA_HOME 环境变量脚本
# 需要以管理员权限运行

$javaPath = "C:\app\Java\jdk8"

Write-Host "正在设置 JAVA_HOME 为: $javaPath" -ForegroundColor Green

# 设置系统环境变量 JAVA_HOME
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", $javaPath, [System.EnvironmentVariableTarget]::Machine)

# 获取当前 PATH
$currentPath = [System.Environment]::GetEnvironmentVariable("Path", [System.EnvironmentVariableTarget]::Machine)

# 移除旧的 Java 路径
$pathArray = $currentPath -split ";" | Where-Object { 
    $_ -notlike "*jdk1.8.0_271*" -and $_ -ne ""
}

# 添加新的 Java bin 路径（如果不存在）
$javaBinPath = "$javaPath\bin"
if ($pathArray -notcontains $javaBinPath) {
    $pathArray = @($javaBinPath) + $pathArray
}

# 重新组合 PATH
$newPath = $pathArray -join ";"

# 设置新的 PATH
[System.Environment]::SetEnvironmentVariable("Path", $newPath, [System.EnvironmentVariableTarget]::Machine)

Write-Host "环境变量设置完成！" -ForegroundColor Green
Write-Host "JAVA_HOME = $javaPath" -ForegroundColor Yellow
Write-Host "请重启 IDE 使环境变量生效。" -ForegroundColor Cyan
