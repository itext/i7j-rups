@echo off
start %~dp0light_runtime\bin\java -jar %~dp0app.jar %* && exit 0