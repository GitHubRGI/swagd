///* The MIT License (MIT)
// *
// * Copyright (c) 2015 Reinventing Geospatial, Inc.
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//import java.io.File;
//import java.io.IOException;
//import java.sql.SQLException;
//
//import com.rgi.geopackage.GeoPackage;
//import com.rgi.geopackage.extensions.implementation.BadImplementationException;
//import com.rgi.geopackage.extensions.routing.GeoPackageRoutingExtension;
//import com.rgi.geopackage.extensions.routing.RoutingNetworkDescription;
//import com.rgi.geopackage.verification.ConformanceException;
//
///**
// *
// * @author Luke Lambert
// *
// */
//@SuppressWarnings("javadoc")
//public class RoutingUtility
//{
//    private static final File geoPackageFile = new File("contour.1.gpkg");
//
//    public static void main(final String[] args)
//    {
//        try(GeoPackage gpkg = new GeoPackage(geoPackageFile))
//        {
//            final double longitude = 272661.49;
//            final double latitude  = 4240831.49;
//
//            final GeoPackageRoutingExtension routingExtension = gpkg.extensions().getExtensionImplementation(GeoPackageRoutingExtension.class);
//            final RoutingNetworkDescription routingNetwork = routingExtension.getRoutingNetworkDescriptions().get(0);
//
//            //final Integer closestNode = routingExtension.getClosestNode(routingNetwork, longitude, latitude);
//            //System.out.println(closestNode);
//
//            final int[] counter = {0};
//
//            long startTime = System.nanoTime();
//
//            routingExtension.visitEdgesInCircle(routingNetwork,
//                                                278991.0,
//                                                4248780.0,
//                                                600.0,
//                                                edge -> ++counter[0]);
//
//            System.out.println(String.format("in circle took %.2f seconds.", (System.nanoTime() - startTime)/1.0e9));
//
//            System.out.println(counter[0]);
//
//            counter[0] = 0;
//
//            startTime = System.nanoTime();
//
//            routingExtension.visitEdgesCloseToCircle(routingNetwork,
//                                                     278991.0,
//                                                     4248780.0,
//                                                     600.0,
//                                                     edge -> ++counter[0]);
//
//            System.out.println(String.format("'close to' circle took %.2f seconds.", (System.nanoTime() - startTime)/1.0e9));
//
//            System.out.println(counter[0]);
//        }
//        catch(ClassNotFoundException | ConformanceException | IOException | SQLException | BadImplementationException e)
//        {
//            e.printStackTrace();
//        }
//    }
//}
