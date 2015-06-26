@echo off

call mvn install:install-file -Dfile=libs/sqldroid-1.0.4-SNAPSHOT.jar ^
    -DgroupId=org.sqldroid -DartifactId=sqldroid -Dversion=1.0.4-SNAPSHOT ^
    -Dpackaging=jar
echo Exit Code = %ERRORLEVEL%
if not "%ERRORLEVEL%" == "0" exit /b
