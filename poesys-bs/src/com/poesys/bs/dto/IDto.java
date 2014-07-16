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


import com.poesys.bs.delegate.DelegateException;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * Base interface for the business-layer Data Transfer Objects (DTOs). A DTO
 * holds the data required by the presentation layer and provides an
 * object-focused API for managing the object's data. The basic purpose of the
 * DTO is to transfer data from the business layer to the presentation layer
 * (user interface).
 * </p>
 * <p>
 * Each DTO implementation has accessors (getters and setters) that provide an
 * interface to the data elements of the logical object, usually implemented as
 * passthrough calls to the embedded data-access DTO. The implementation may
 * also contain methods that accomplish calculations relevant to the user
 * interface (transformations, parsing, data generation, and so on) not
 * performed as part of the data access layer. It is possible for a DTO
 * implementation to combine underlying DTOs into a complex, dynamic DTO with a
 * dynamically generated interface (a data view, in database terminology). This
 * kind of DTO might not be based on the standard AbstractDto implementation and
 * may not be modifiable or updatable depending on its structure.
 * </p>
 * <p>
 * Each DTO should have a default constructor, a data-access DTO constructor for
 * building a business DTO from the data access DTO, and a new-object
 * constructor that contains all required data elements and any additional
 * elements likely to be set when you create the object from presentation layer
 * data. These latter constructors should correspond to factory methods in the
 * data access layer that create new data access DTO objects.
 * </p>
 * <p>
 * The DTO supports comparison (ordinal inequalities and equality), equality
 * comparison based on all data in the object, and hashing based on the primary
 * key.
 * </p>
 * <p>
 * The DTO supports a lifecycle that starts with either status NEW or EXISTING.
 * An in-memory factory creates a NEW object; a database query creates an
 * EXISTING object. When you store a NEW object in the database, it becomes
 * EXISTING. When you call a mutator (set method), it should change the status
 * to CHANGED. When you call update(), the CHANGED status transitions to
 * EXISTING. When you call delete(), the implementation should change the status
 * to DELETED. When you call undoStatus(), the implementation should restore the
 * previous status (but not necessarily the concrete state). The insert, update,
 * and delete Data Access Object (DAO) methods also transition the status to
 * EXISTING, DELETED, or FAILED. This latter status provides a way for batch
 * processing, in particular, to inform the client which objects failed to
 * process correctly. You can fix the problem, reset the status using
 * undoStatus(), and resubmit the object for processing.
 * </p>
 * <p>
 * The AbstractDto class implements a constructor-initialized version of this
 * interface as a reference implementation with status undo.
 * </p>
 * <p>
 * <strong>Please see the documentation for AbstractDto for some examples of DTO
 * coding.</strong>
 * </p>
 * 
 * @see AbstractDto
 * 
 * @author Robert J. Muller
 * @param <T> the type of database-layer IDbDto the IDto wraps
 */
public interface IDto<T extends IDbDto> extends Comparable<IDto<T>> {
  /** Enumerated type with the four possible states of the object */
  enum Status {
    /** A newly created object */
    NEW, 
    /** An object queried from the database */
    EXISTING, 
    /** A queried object with changed values */
    CHANGED, 
    /** A queried object marked as deleted */
    DELETED, 
    /** An object in an invalid state after a failed operation */
    FAILED
  };

  /**
   * Get the primary key of the object. The primary key uniquely identifies the
   * object in a collection of objects.
   * 
   * @return an IPrimaryKey primary key object
   */
  IPrimaryKey getPrimaryKey();

  /**
   * Mark the object as deleted if it is EXISTING or CHANGED.
   * 
   * @throws DelegateException when the current status is not EXISTING or
   *             CHANGED
   */
  void delete() throws DelegateException;

  /**
   * Convert the business-layer DTO to a data-access-layer DTO.
   * 
   * @return the data-access DTO
   */
  T toDto();
}
