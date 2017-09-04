@echo off
cd /d %~dp0
cd ../work/
node build/js/Bootstrap.js ../demo/app/config.js
pause  
