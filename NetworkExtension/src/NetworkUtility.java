/* The MIT License (MIT)
 *
 * Copyright (c) 2015 Reinventing Geospatial, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import com.rgi.common.Pair;
import com.rgi.geopackage.GeoPackage;
import com.rgi.geopackage.extensions.implementation.BadImplementationException;
import com.rgi.geopackage.extensions.network.AttributeDescription;
import com.rgi.geopackage.extensions.network.AttributedType;
import com.rgi.geopackage.extensions.network.GeoPackageNetworkExtension;
import com.rgi.geopackage.extensions.network.Network;
import com.rgi.geopackage.verification.ConformanceException;

/**
 *
 * @author Luke Lambert
 *
 */
@SuppressWarnings("javadoc")
public class NetworkUtility
{
    private static final File geoPackageFile = new File("contour.1.gpkg");

    public static void main(final String[] args)
    {
        try(GeoPackage gpkg = new GeoPackage(geoPackageFile))
        {
            final double longitude = 272661.49;
            final double latitude  = 4240831.49;

            final int closestNode = snap(gpkg, "mynetwork", longitude, latitude);

            System.out.println(closestNode);
        }
        catch(ClassNotFoundException | ConformanceException | IOException | SQLException | BadImplementationException e)
        {
            e.printStackTrace();
        }
    }

    private static int snap(final GeoPackage gpkg,
                            final String     networkTable,
                            final double     longitude,
                            final double     latitude) throws SQLException, BadImplementationException
    {
        final GeoPackageNetworkExtension networkExtension = gpkg.extensions().getExtensionImplementation(GeoPackageNetworkExtension.class);
        final Network network = networkExtension.getNetwork(networkTable);

        final AttributeDescription longitudeDesription = networkExtension.getAttributeDescription(network, "longitude", AttributedType.Node);
        final AttributeDescription latitudeDesription  = networkExtension.getAttributeDescription(network, "latitude",  AttributedType.Node);

        final Pair<Integer, Double> closest = networkExtension.accumulateNodes(network,
                                                                               Pair.of(null, Double.MAX_VALUE),
                                                                               (nodeIdentifier, attributes) -> { final double nodeLongitude = (Double)attributes.get(0);
                                                                                                                 final double nodeLatitude  = (Double)attributes.get(1);

                                                                                                                 final double longitudeDifference = nodeLongitude - longitude;
                                                                                                                 final double latitudeDifference  = nodeLatitude  - latitude;

                                                                                                                 final double distanceSquared = (longitudeDifference*longitudeDifference) + (latitudeDifference*latitudeDifference);

                                                                                                                 return Pair.of(nodeIdentifier, distanceSquared);
                                                                                                               },
                                                                               (currentValue, newValue) -> (currentValue.getRight() < newValue.getRight()) ? currentValue : newValue,
                                                                               longitudeDesription,
                                                                               latitudeDesription);
        return closest.getLeft();
    }
}
