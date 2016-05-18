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
 * 
 */
package com.poesys.bs.dto;


import java.util.ArrayList;
import java.util.List;


/**
 * A utility command class that generically builds a list of data-access-layer
 * DTOs of type T from a list of business-layer DTOs of type B that encapsulate
 * DTOs of type T (AbstractDto&lt;T&gt;).
 * 
 * @author Robert J. Muller
 * @param <T> the data-access layer DTO type
 * @param <B> the business layer DTO type
 */
public class DataAccessDtoListBuilder<T extends com.poesys.db.dto.AbstractDto, B extends AbstractDto<T>> {
  /**
   * Get a list containing data-access-layer DTOs of type T based on the DTOs
   * embedded in a list of business-layer DTOs of type B.
   * 
   * @param list the list of business-layer DTOs
   * @return the list of data-access-layer DTOs
   */
  public List<T> getList(List<B> list) {
    List<T> dataList = new ArrayList<T>();
    // Test for null input list, return empty list if null
    if (list != null) {
      for (B dto : list) {
        dataList.add((T)dto.dto);
      }
    }
    return dataList;
  }
}
