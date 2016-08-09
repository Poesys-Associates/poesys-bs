/*
 * Copyright (c) 2011 Poesys Associates. All rights reserved.
 * 
 * This file is part of Poesys-DB.
 * 
 * Poesys-DB is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Poesys-DB is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Poesys-DB. If not, see <http://www.gnu.org/licenses/>.
 */

package com.poesys.bs.delegate;


import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * An abstract base class that implements the IDataDelegate interface for the
 * JDBC-based business delegates in the application that require a JDBC database
 * connection and DAOs. The abstract class contains the DAO manager and factory
 * and methods that use them.
 * </p>
 * 
 * @author Robert J. Muller
 * @param <S> the database layer DTO type
 */
public abstract class AbstractDaoDelegate<S extends IDbDto> extends
    AbstractConnectionDelegate {
  /** DAO Manager singleton for factory generation */
  protected final IDaoManager manager;

  /** Factory for DAOs the delegate and its subclasses use */
  protected final IDaoFactory<S> factory;
  
  /**
   * Create a DAO Delegate. This is a standard constructor that sets the name of
   * the subsystem and the database type for construction of connections to the
   * database.
   * 
   * @param subsystem the name of the subsystem
   * @param dbms the kind of database that implements the subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractDaoDelegate(String subsystem, DBMS dbms, Integer expiration) {
    super(subsystem, dbms);
    // Create the DAO factory with the object's class name.
    manager = DaoManagerFactory.getManager(subsystem);
    factory = manager.getFactory(getClassName(), subsystem, expiration);
  }

  /**
   * Create a DAO Delegate. This is a standard JNDI connection constructor that
   * sets up connections by getting the JNDI data source through the subsystem
   * name.
   * 
   * @param subsystem the JNDI data source subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractDaoDelegate(String subsystem, Integer expiration) {
    super(subsystem);
    // Create the DAO factory with the object's class name.
    manager = DaoManagerFactory.getManager(subsystem);
    factory = manager.getFactory(getClassName(), subsystem, expiration);
  }

  /**
   * Get the fully qualified class name of the IDto concrete subclass that this
   * DAO Delegate manages. It has to be passed in because due to type erasure
   * there is no way to get it from the generic parameter. A subclass that knows
   * the type must implement this method.
   * 
   * <pre>
   * <code>
   * protected String getClassName() {
   *   return TestNatural.class.getName();
   * }
   * </code>
   * </pre>
   * 
   * @return the class name
   */
  abstract protected String getClassName();

}
