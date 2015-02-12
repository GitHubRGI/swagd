SWAGD : Software to Aggregate Geospatial Data
===============

A Java-based application for converting geospatial images into tiles, packaging those tiles, and viewing them on a map.

[![Build Status](https://travis-ci.org/GitHubRGI/swagd.svg?branch=master)](https://travis-ci.org/GitHubRGI/swagd)
[![Coverage Status](https://coveralls.io/repos/GitHubRGI/swagd/badge.svg?branch=master)](https://coveralls.io/r/GitHubRGI/swagd?branch=master)
[![Stories in Ready](https://badge.waffle.io/GitHubRGI/swagd.png?label=ready&title=Ready)](https://waffle.io/GitHubRGI/swagd)
[![Coverity Scan Build Status](https://scan.coverity.com/projects/3993/badge.svg)](https://scan.coverity.com/projects/3993)

## The Road To Release 1.0

[See current progress to Release 1.0](https://github.com/GitHubRGI/swagd/milestones)

## Projects
* __Common__: Contains functionality for coordinates, tiles, tile stores, and tasks
  * _Dependency_: Nothing
* __Gdal2Tiles__: Functionality necessary for converting raster imagery into tiles
  * _Dependencies_: Common, GDAL (refer to build environment wiki)
* __Geopackage__: Functions and validators for creating or reading GeoPackages
  * _Dependency_: Common
* __GeoViewer__: Workflow code for the JMapViewer-based map canvas viewer for GeoPackages
  * _Dependencies_: Common, Geopackage, JMapViewer (refer to build environment wiki)
* __IntegrationTest__: Test code for integration environments
  * _Dependencies_: Common, Gdal2Tiles, Geopackage, GeoViewer
* __Packager__: Workflow code that will use the Geopackage library to create Geopackage data products
  * _Dependencies_: Common, Geopackage
* __RGISuite__: Main project and workflow aggregation
  * _Dependencies_: Common, Gdal2Tiles, Geopackage, GeoViewer, Packager
