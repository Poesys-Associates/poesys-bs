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


import java.math.BigDecimal;

import com.poesys.bs.delegate.DelegateException;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.IPrimaryKey;


/**
 * Provides business layer data for a test object with a natural key.
 * 
 * @author Robert J. Muller
 */
public class BsTestNatural extends AbstractDto<TestNatural> {
  /**
   * Create a TestNatural object by embedding a data-access TestNatural DTO.
   * 
   * @param dto the data-access DTO
   * @throws DelegateException when there is an exception creating the
   *           superclass
   */
  public BsTestNatural(TestNatural dto) throws DelegateException {
    super(dto);
  }

  /**
   * Create a new TestNatural object with supplied values.
   * 
   * @param key1 the first key value
   * @param key2 the second key value
   * @param col1 the column value
   */
  public BsTestNatural(String key1, String key2, BigDecimal col1) {
    super(new TestNatural(key1, key2, col1));
  }

  @Override
  public boolean equals(Object arg0) {
    // Test whether the embedded dtos have all the same values.
    BsTestNatural that = (BsTestNatural)arg0;
    return dto.equals(that.dto);
  }

  @Override
  public int hashCode() {
    // Return the hash code for the embedded TestNatural object.
    return dto.hashCode();
  }

  /**
   * Get the first part of the primary key (read only).
   * 
   * @return the key value
   */
  public String getKey1() {
    return dto.getKey1();
  }

  /**
   * Get the second part of the primary key (read only).
   * 
   * @return the key value
   */
  public String getKey2() {
    return dto.getKey2();
  }

  /**
   * Get the col1 value.
   * 
   * @return the col1 value
   */
  public BigDecimal getCol1() {
    return dto.getCol1();
  }

  /**
   * Set the col1 value.
   * 
   * @param col1 the value to set
   */
  public void setCol1(BigDecimal col1) {
    dto.setCol1(col1);
  }

  @Override
  public IPrimaryKey getPrimaryKey() {
    return dto.getPrimaryKey();
  }

  @Override
  public int compareTo(IDto<TestNatural> o) {
    return dto.compareTo(o.toDto());
  }
}
