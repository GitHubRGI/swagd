A few notes on getting GDAL working in Java:

1.	Download and install gdal-111-1600-core.msi from:

	http://www.gisinternals.com/sdk/PackageList.aspx?file=release-1600-gdal-1-11-0-mapserver-6-4-1.zip

2.	Configure environment variables:
	a.	GDAL_DATA = C:\Program Files (x86)\GDAL\gdal-data
	b.	GDAL_DRIVER_PATH = C:\Program Files (x86)\GDAL\gdalplugins
	c.	PATH = %PATH%;C:\Program Files (x86)\GDAL

