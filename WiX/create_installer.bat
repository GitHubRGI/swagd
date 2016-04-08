@echo off

call candle Swagd.wxs
SET result=%ERRORLEVEL%
echo Exit Code = %result%
if not "%result%" == "0" exit /b

call light Swagd.wixobj -out Resources/Swagd.msi
SET result=%ERRORLEVEL%
echo Exit Code = %result%
if not "%result%" == "0" exit /b

call candle -ext WixBalExtension -ext WixUtilExtension Swagd-deps.wxs
SET result=%ERRORLEVEL%
echo Exit Code = %result%
if not "%result%" == "0" exit /b

call light -ext WixBalExtension -ext WixUtilExtension Swagd-deps.wixobj
SET result=%ERRORLEVEL%
echo Exit Code = %result%
if not "%result%" == "0" exit /b
