###Steps to build the installers:

_Prereq: Wix toolset v3.9.x is installed and in your system path._

1. Build the swagd software using maven. (mvn install)
2. Download the msi's for gdal-core and gdal-mrsid, from the urls below, into the {swagd}/WiX/Resources folder.
    -http://download.gisinternals.com/sdk/downloads/release-1600-x64-gdal-1-11-1-mapserver-6-4-1/gdal-111-1600-x64-core.msi
    -http://download.gisinternals.com/sdk/downloads/release-1600-x64-gdal-1-11-1-mapserver-6-4-1/gdal-111-1600-x64-mrsid.msi
3. Download the exe installer for java 8 update 45 (url below) into the {swagd}/WiX/Resources folder.
    -http://javadl.sun.com/webapps/download/AutoDL?BundleId=107100
4. open a command prompt into the {swagd}/WiX/ directory, and enter the commands listed below.The first two will build the installer for the actual
    Swagd software, the second will build the bundled installer with the dependencies. you can then use these installers anywhere. 

* `candle Swagd.wxs`
* `light Swagd.wixobj -out packages/Swagd.msi`

* `candle -ext WixBalExtension -ext WixUtilExtension Swagd-deps.wxs`
* `light -ext WixBalExtension -ext WixUtilExtension Swagd-deps.wixobj`

##Installing SWAGD

* _With dependencies_
    * Run the Swagd-deps.msi file located in {swagd}/WiX on any windows machine
* _Without dependencies_
    * Run the Swagd.msi file located at {swagd}/WiX/Resources on any windows machine
