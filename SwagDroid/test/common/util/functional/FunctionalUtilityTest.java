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

package common.util.functional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.rgi.android.common.util.functional.Function;
import com.rgi.android.common.util.functional.FunctionalUtility;
import com.rgi.android.common.util.functional.Predicate;

/**
 * @author Mary Carome
 *
 */
@SuppressWarnings("static-method")
public class FunctionalUtilityTest
{
    /**
     * Tests anyMatch throws an IllegalArgumentException when given a null
     * parameter instead of a Collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void anyMatchIllegalArgumentException1()
    {
        FunctionalUtility.anyMatch(null,
                                   new Predicate<String>()
                                   {
                                       @Override
                                       public boolean apply(final String t)
                                       {
                                           return false;
                                       }
                                   });

        fail("Expected FunctionalUtility method anyMatch to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests anyMatch throws an IllegalArgumentException when given a null
     * parameter instead of a Predicate
     */
    @Test(expected = IllegalArgumentException.class)
    public void anyMatchIllegalArgumentException2()
    {
        final Collection<String> collection = new ArrayList<String>();
        FunctionalUtility.anyMatch(collection, null);
        fail("Expected FunctionalUtility method anyMatch to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests filter throws an IllegalArgumentException when given an null
     * parameter instead of a Collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void filterIllegalArgumentException1()
    {
        FunctionalUtility.filter(null,
                                 new Predicate<String>()
                                 {
                                     @Override
                                     public boolean apply(final String t)
                                     {
                                         return false;
                                     }
                                 });

        fail("Expected FunctionalUtility method filter to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests filter throws an IllegalArgumentException when given an null
     * parameter instead of a Predicate
     */
    @Test(expected = IllegalArgumentException.class)
    public void filterIllegalArgumentException2()
    {
        final Collection<String> collection = new ArrayList<String>();
        FunctionalUtility.filter(collection, null);
        fail("Expected FunctionalUtility method filter to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests map throws an IllegalArgumentException when given an null parameter
     * instead of a Collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapIllegalArgumentException1()
    {
        FunctionalUtility.map(null,
                              new Function<String, String>()
                              {
                                  @Override
                                  public String apply(final String input)
                                  {
                                      return "test";
                                  }
                              });

        fail("Expected FunctionalUtility method map to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests map throws an IllegalArgumentException when given an null parameter
     * instead of a Function
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapIllegalArgumentException2() {
        final Collection<String> collection = new ArrayList<String>();
        FunctionalUtility.map(collection, null);
        fail("Expected FunctionalUtility method map to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests mapFilter throws an IllegalArgumentException when given an null
     * parameter instead of a Collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterIllegalArgumentException1()
    {
        FunctionalUtility.mapFilter(null,
                                    new Function<String, String>()
                                    {
                                        @Override
                                        public String apply(final String input)
                                        {
                                            return input;
                                        }
                                    },
                                    new Predicate<String>()
                                    {
                                        @Override
                                        public boolean apply(final String t)
                                        {
                                            return false;
                                        }
                                    });

        fail("Expected FunctionalUtility method mapFilter to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests mapFilter throws an IllegalArgumentException when given an null
     * parameter instead of a Function
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterIllegalArgumentException2()
    {
        final Collection<String> collection = new ArrayList<String>();

        FunctionalUtility.mapFilter(collection,
                                    null,
                                    new Predicate<String>()
                                    {
                                        @Override
                                        public boolean apply(final String t)
                                        {
                                            return false;
                                        }
                                    });

        fail("Expected FunctionalUtility method mapFilter to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests mapFilter throws an IllegalArgumentException when given an null
     * parameter a Predicate
     */
    @Test(expected = IllegalArgumentException.class)
    public void mapFilterIllegalArgumentException3()
    {
        final Collection<String> collection = new ArrayList<String>();

        FunctionalUtility.mapFilter(collection,
                                    new Function<String, String>()
                                    {
                                        @Override
                                        public String apply(final String input)
                                        {
                                            return input;
                                        }
                                    },
                                    null);

        fail("Expected FunctionalUtility method mapFilter to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests filterMap throws an IllegalArgumentException when passed null
     * instead of a Collection
     */
    @Test(expected = IllegalArgumentException.class)
    public void filterMapIllegalArgumentException1()
    {
        FunctionalUtility.filterMap(null,
                                    new Predicate<String>()
                                    {
                                        @Override
                                        public boolean apply(final String t)
                                        {
                                            return false;
                                        }
                                    },
                                    new Function<String, Integer>()
                                    {
                                        @Override
                                        public Integer apply(final String input)
                                        {
                                            return input.length();
                                        }
                                    });

        fail("Expected FuntionalUtility method filterMap to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests filterMap throws an IllegalArgumentException when passed null
     * instead of a Predicate
     */
    @Test(expected = IllegalArgumentException.class)
    public void filterMapIllegalArgumentException2()
    {
        final Collection<String> collection = new ArrayList<String>();

        FunctionalUtility.filterMap(collection,
                                    null,
                                    new Function<String, Integer>()
                                    {
                                        @Override
                                        public Integer apply(final String input)
                                        {
                                            return input.length();
                                        }
                                    });

        fail("Expected FuntionalUtility method filterMap to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests filterMap throws an IllegalArgumentException when
     * passed null instead of a Function
     */
    @Test (expected = IllegalArgumentException.class)
    public void filterMapIllegalArgumentException3()
    {
        final Collection<String> collection = new ArrayList<String>();

        FunctionalUtility.filterMap(collection,
                                    new Predicate<String>()
                                    {
                                        @Override
                                        public boolean apply(final String t)
                                        {
                                            return false;
                                        }
                                    },
                                    null);

        fail("Expected FuntionalUtility method filterMap to throw an IllegalArgumentException when passed a null parameter.");
    }

    /**
     * Tests that mapFilter properly filters the objects in the given
     * collection, based on the given predicate, and then applies the function
     * to that filtered collection
     */
    @Test
    public void testFilterMap()
    {
        final Collection<String> collection = new ArrayList<String>();
        final String name1 = "Mary";
        final String name2 = "Mary E. Carome";
        final String name3 = "Jon";
        final String name4 = "Jon W. Smith";
        final String name5 = "Rob";
        final String name6 = "RObert E. Lee";

        collection.add(name1);
        collection.add(name2);
        collection.add(name3);
        collection.add(name4);
        collection.add(name5);
        collection.add(name6);

        final List<Integer> results = FunctionalUtility.filterMap(collection,
                                                                  new Predicate<String>()
                                                                  {
                                                                      @Override
                                                                      public boolean apply(final String t)
                                                                      {
                                                                          return t.length() > 5;
                                                                      }
                                                                  },
                                                                  new Function<String, Integer>()
                                                                  {
                                                                      @Override
                                                                      public Integer apply(final String input)
                                                                      {
                                                                          return input.length();
                                                                      }
                                                                  });

        assertTrue("FunctionalUtility method filterMap did not return the expected Collection.",
                   results.size() == 3 &&
                   results.get(0) == name2.length() &&
                   results.get(1) == name4.length() &&
                   results.get(2) == name6.length());
    }
}
