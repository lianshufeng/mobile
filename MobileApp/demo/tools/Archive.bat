@echo off
cd /d %~dp0\..\..\
@echo ��ʼ�����ļ�
if not exist backup mkdir backup
set fileName=backup\mobile_%date:~0,4%%date:~5,2%%date:~8,2%.zip
echo ѹ����%fileName%
if exist %fileName% del %fileName%
rar a %fileName% -r -x*node_modules* -x*package-lock.json* -x\demo\output\  .\demo .\setup .\work
echo ������� , 5���Զ��˳�.
ping 127.1 -n 5 >nul 