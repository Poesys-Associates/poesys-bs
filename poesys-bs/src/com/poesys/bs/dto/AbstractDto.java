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


import com.poesys.bs.delegate.DelegateException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An abstract implementation of the IDto interface for business-layer data
 * transfer objects (DTOs). The class implements a simple status tracking system
 * with status undo but no comprehensive undo strategy. The class implements the
 * DTO methods by passing them through to a nested data-access-layer DTO
 * embedded in the object, either created as NEW or retrieved from the data
 * access layer.
 * </p>
 * <p>
 * Here is an example of a DTO implementation based on this class:
 * </p>
 * 
 * <pre>
 * 
 * A data transfer object for the TestNatural table.
 * 
 *  public class BsTestNatural extends AbstractDto&lt;ITestNatural&gt;
 *      implements ITestNatural {
 *  
 *    public BsTestNatural(ITestNatural dto) throws DelegateException {
 *      super(dto);
 *    }
 * 
 *    public BsTestNatural(String key1, String key2, BigDecimal col1) {
 *      super(new TestNatural(key1, key2, col1));
 *    }
 * 
 *    &#064;Override
 *    public boolean equals(Object arg0) {
 *      // Test whether the embedded dtos have all the same values.
 *      BsTestNatural that = (BsTestNatural)arg0;
 *      return dto.equals(that.dto);
 *    }
 * 
 *    &#064;Override
 *    public int hashCode() {
 *      // Return the hash code for the embedded TestNatural object.
 *      return dto.hashCode();
 *    }
 * 
 *    public String getKey1() {
 *      return dto.getKey1();
 *    }
 * 
 *    public String getKey2() {
 *      return dto.getKey2();
 *    }
 * 
 *    public BigDecimal getCol1() {
 *      return dto.getCol1();
 *    }
 * 
 *    public void setCol1(BigDecimal col1) {
 *        dto.setCol1(col1);
 *    }
 * 
 *    public IPrimaryKey getPrimaryKey() {
 *      return dto.getPrimaryKey();
 *    }
 * 
 *    public int compareTo(AbstractDto&lt;TestNatural&gt; o) {
 *      return dto.compareTo(o.toDto());
 *    }
 *  }
 * </pre>
 * 
 * @author Robert J. Muller
 * @param <T> the data-access-layer database DTO type
 */
public abstract class AbstractDto<T extends IDbDto> implements IDto<T> {
  /** Internal data-access-layer DTO; package access for list building */
  protected T dto;

  /** Error message when no object supplied to constructor */
  static protected final String NO_OBJECT =
    "com.poesys.bs.delegate.msg.noObject";

  /**
   * Create the DTO.
   * 
   * @param dto the database layer DTO to wrap
   * 
   * @throws DelegateException when the supplied dto is null
   */
  public AbstractDto(T dto) throws DelegateException {
    if (dto == null) {
      throw new DelegateException(NO_OBJECT);
    }
    this.dto = dto;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  abstract public int hashCode();

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  abstract public boolean equals(Object arg0);

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.dto.IDto#getPrimaryKey()
   */
  public IPrimaryKey getPrimaryKey() {
    return dto.getPrimaryKey();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(IDto<T> o) {
    return dto.compareTo(o.toDto());
  }

  /**
   * Mark the DTO as deleted.
   */
  public void delete() {
    dto.delete();
  }

  /**
   * Has the DTO been marked deleted?
   * 
   * @return true if deleted, false if some other status
   */
  public boolean isDeleted() {
    return IDbDto.Status.DELETED.compareTo(dto.getStatus()) == 0;
  }

  /**
   * Mark the DTO as cascade-deleted (the database performs the actual delete
   * while the application removes the object from the cache, if any).
   */
  public void cascadeDelete() {
    dto.cascadeDelete();
  }

  /**
   * Mark the children of the dto deleted.
   */
  public void markChildrenDeleted() {
    dto.markChildrenDeleted();
  }

  /**
   * Convert the delegate DTO to a data-access layer DTO. Use this method to
   * extract the DTO to pass into data access methods.
   * 
   * @return the data-access DTO embedded within the delegate DTO
   * @see DataAccessDtoListBuilder
   */
  public T toDto() {
    return dto;
  }
}
