/*
 * Copyright (c) 2009 Poesys Associates. All rights reserved.
 * 
 * This file is part of Poesys-BS.
 * 
 * Poesys-BS is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Poesys-BS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Poesys-BS. If not, see <http://www.gnu.org/licenses/>.
 */
package com.poesys.bs.dto;


import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;


/**
 * An abstract utility command class that generically builds a collection of
 * objects of type B from a collection of objects of type T, given a conversion
 * operation implemented in the concrete implementation of the abstract class.
 * The output collection is a threadsafe object such as a CopyOnWriteArrayList.
 * 
 * @author Robert J. Muller
 * @param <T> the input object type (DB DTO)
 * @param <B> the output object type (BS DTO)
 */
public abstract class CollectionBuilder<T, B> {
  private static final Logger logger =
    Logger.getLogger(CollectionBuilder.class);

  /**
   * Get a collection containing objects of type B based on the objects in a
   * collection of objects of type T.
   * 
   * @param collection the collection of input objects
   * @return the collection of output objects
   */
  public Collection<B> getCollection(Collection<T> collection) {
    Collection<B> newCollection = new ArrayList<B>();
    // Test for null collection, return empty new one if null.
    if (collection != null) {
      for (T dto : collection) {
        // Checking for null DTO here because for some reason the iteration
        // is getting null DTOs and passing them down, which results in the
        // message com.poesys.bs.delegate.msg.noObject from a constructor.
        if (dto == null) {
          // Log the problem and ignore the DTO.
          logger.error("Null DTO in iteration over collection");
          logger.debug("collection size " + collection.size());
          for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            logger.debug(ste);
          }
        } else {
          // Get the BS object wrapping the DTO and add to the BS collection.
          newCollection.add(get(dto));
        }
      }
    }
    // Return a typesafe collection.
    return new CopyOnWriteArrayList<B>(newCollection);
  }

  /**
   * Get an object of type B based on an object of type T. The concrete subclass
   * implements this with an appropriate constructor of type B, usually in a
   * nested class:
   * 
   * <pre>
   * private ConvertTest extends CollectionBuilder&lt;ITest, ITest&gt; {
   *   public ITest get(ITest dto) {
   *     return new BsTest(dto);
   *   }
   * }
   * </pre>
   * 
   * @param dto the DTO of type T
   * @return a DTO of type B
   */
  abstract public B get(T dto);
}
