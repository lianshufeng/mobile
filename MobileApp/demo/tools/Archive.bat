@echo off
cd /d %~dp0\..\..\
@echo 开始备份文件
if not exist backup mkdir backup
set fileName=backup\mobile_%date:~0,4%%date:~5,2%%date:~8,2%.zip
echo 压缩：%fileName%
if exist %fileName% del %fileName%
rar a %fileName% -r -x*node_modules* -x*package-lock.json* -x\demo\output\  .\demo .\setup .\work
echo 处理完成 , 5秒自动退出.
ping 127.1 -n 5 >nul 