/*
 * Copyright (c) 2008 Poesys Associates. All rights reserved.
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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;


/**
 * An abstract utility command class that generically builds a list of objects
 * of type B from a list of objects of type T, given a conversion operation
 * implemented in the concrete implementation of the abstract class. The output
 * list is a threadsafe list such as a CopyOnWriteArrayList.
 * 
 * @author Robert J. Muller
 * @param <T> the input data-access transfer object type
 * @param <B> the output business data transfer object type
 */
public abstract class ListBuilder<T, B> {
  /** Logger for this class */
  private static Logger logger = Logger.getLogger(ListBuilder.class);

  /**
   * Get a list containing objects of type B based on the objects in a list of
   * objects of type T.
   * 
   * @param list the list of input objects
   * @return the list of output objects or null if the input list is null
   */
  public List<B> getList(List<T> list) {
    List<B> newList = null;
    if (list != null) {
      List<B> tempList = new ArrayList<B>(list.size());
      int counter = 0;
      for (T dto : list) {
        // Check for null, although this shouldn't be possible.
        if (dto == null) {
          logger.warn("Null DTO object from list");
          logger.info("List size: " + list.size());
          logger.info("List element: " + counter);
          break; // stop looping and return the list constructed so far
        }
        // Call an abstract method to get the new B DTO and add it to the list.
        tempList.add(get(dto));
        counter++;
      }
      newList = tempList;
    }

    return newList != null ? new CopyOnWriteArrayList<B>(newList)
        : new CopyOnWriteArrayList<B>();
  }

  /**
   * Get an object of type B based on an object of type T. The concrete subclass
   * implements this with an appropriate constructor of type B, usually in a
   * nested class:
   * 
   * <pre>
   * private ConvertTest extends ListBuilder&lt;ITest, ITest&gt; {
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
