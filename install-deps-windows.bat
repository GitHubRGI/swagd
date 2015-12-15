@echo off

call mvn install:install-file -Dfile=RGISuite/lib/gdal.jar ^
    -DgroupId=org.osgeo -DartifactId=gdal -Dversion=1.11.1 ^
    -Dpackaging=jar
SET result=%ERRORLEVEL%
echo Exit Code = %result%
if not "%result%" == "0" exit /b

call mvn install:install-file -Dfile=GeoViewer/lib/JMapViewer-1.05.jar ^
    -DgroupId=org.openstreetmap -DartifactId=JMapViewer ^
    -Dversion=1.05 -Dpackaging=jar
SET result=%ERRORLEVEL%
echo Exit Code = %result%
if not "%result%" == "0" exit /b
