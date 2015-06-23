SWAGD : Software to Aggregate Geospatial Data
===============

A Java-based application for converting geospatial images into tiles, packaging those tiles, and viewing them on a map.

[![Build Status](https://magnum.travis-ci.com/GitHubRGI/swagd.svg?token=sMDZhk629WtJNL1nWEEK&branch=master)](https://magnum.travis-ci.com/GitHubRGI/swagd)
[![Coverage Status](https://coveralls.io/repos/GitHubRGI/swagd/badge.svg?branch=master)](https://coveralls.io/r/GitHubRGI/swagd?branch=master)
[![Stories in Ready](https://badge.waffle.io/GitHubRGI/swagd.png?label=ready&title=Ready)](https://waffle.io/GitHubRGI/swagd)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/3993/badge.svg)](https://scan.coverity.com/projects/3993)

## Release 2.0 Progress
[See current status towards Release 2.0 here.](https://github.com/GitHubRGI/swagd/milestones/Release%202.0)

## Release 1.1 fixes tiling directly to a GeoPackage
* [See notes for Release 1.1 here.](https://github.com/GitHubRGI/swagd/releases/tag/v1.1)

## Release 1.0 Complete!
* [See notes for Release 1.0 here.](https://github.com/GitHubRGI/swagd/releases/tag/v1.0)
* Windows executable download link will be available soon.

## Projects
* __Common__: Contains functionality for coordinates, tiles, tile stores, and tasks
  * _Dependency_: Nothing
* __DataStore__: Functionality responsible for manipulating Tile Stores
  * _Dependency_: Common, GeoPackage
* __Gdal2Tiles__: Functionality necessary for converting raster imagery into tiles
  * _Dependencies_: Common, GDAL (refer to build environment wiki)
* __Geopackage__: Functions and validators for creating or reading GeoPackages
  * _Dependency_: Common
* __GeoViewer__: Workflow code for the JMapViewer-based map canvas viewer for GeoPackages
  * _Dependencies_: Common, GeoPackage, JMapViewer (refer to build environment wiki)
* __NetworkExtension__: Networking and routing capability as a GeoPackage extension
  * _Dependency_: Nothing
* __Packager__: Workflow code that will use the Geopackage library to create Geopackage data products
  * _Dependencies_: Common, GeoPackage
* __RGISuite__: Main project and workflow aggregation
  * _Dependencies_: Common, Gdal2Tiles, Geopackage, GeoViewer, Packager
* __SwagDroid__: Android-specific implementations of the GeoPackage and Common projects
  * _Dependency_: Nothing
* __VerifierTool__: Verify GeoPackages against the OGC GeoPackage Specification
  * _Dependencies_: Common, GeoPackage
