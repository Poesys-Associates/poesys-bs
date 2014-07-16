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
package com.poesys.bs.delegate;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.poesys.bs.dto.AbstractDto;
import com.poesys.bs.dto.IDto;
import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dao.query.IQueryList;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An abstract base class that implements the IDataDelegate interface for the
 * JDBC-based business delegates in the application that require a JDBC database
 * connection and managed transactions. The abstract class contains the specific
 * methods for querying data in a read-only database table and inherits the
 * internal methods for managing connections and transactions from the
 * AbstractConnectionDelegate superclass.
 * </p>
 * <p>
 * The implementation implements the abstract methods, most of which create the
 * appropriate SQL objects. The code examples in this documentation show a
 * simple implementation with direct instantiation of classes. You can achieve
 * more sophisticated goals by implementing DAO and/or SQL class factories, then
 * using the factories to implement the abstract methods in this class. This
 * kind of approach, for example, would let you support different SQL statements
 * for different DBMS implementations of the application, with the SQL statement
 * factory generating the appropriate SQL statements based on the identification
 * of the DBMS.
 * </p>
 * <p>
 * The implementation abstracts the SQL-specification-object instantiations
 * required by the various methods. A concrete subclass simply implements those
 * SQL statement construction methods to create a working delegate.
 * </p>
 * <p>
 * The AbstractDataDelegate class provides the same query methods and adds the
 * insert, update, delete, process, and truncate methods for read-write tables.
 * </p>
 * <p>
 * <em>
 * Note: You should log exceptions at the top of the calling hierarchy in the
 * user interface layer that uses these business delegates, not in the business
 * delegate itself. The error messages defined in business delegates should be
 * message strings installed in a resource bundle for internationalization. Your
 * business delegate class should throw only a DelegateException that nests the
 * causing exception, if any.
 * </em>
 * </p>
 * 
 * @see DelegateException
 * @see AbstractDto
 * @see AbstractDataDelegate
 * @see com.poesys.db.dto.AbstractDto
 * @see com.poesys.db.pk.IPrimaryKey
 * 
 * @author Robert J. Muller
 * @param <T> the business layer DTO type
 * @param <S> the database layer DTO type
 * @param <K> the primary key type
 */
abstract public class AbstractReadOnlyDataDelegate<T extends IDto<S>, S extends IDbDto, K extends IPrimaryKey>
    extends AbstractDaoDelegate<S> implements
    IReadOnlyDataDelegate<T, S, K> {

  /**
   * Standard constructor that sets the name of the subsystem and the database
   * type for construction of connections to the database.
   * 
   * @param subsystem the name of the subsystem
   * @param dbms the kind of database that implements the subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractReadOnlyDataDelegate(String subsystem,
                                      IConnectionFactory.DBMS dbms,
                                      Integer expiration) {
    super(subsystem, dbms, expiration);
  }

  /**
   * Standard JNDI connection constructor that sets up connections by getting
   * the JNDI data source through the subsystem name.
   * 
   * @param subsystem the JNDI data source subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractReadOnlyDataDelegate(String subsystem, Integer expiration) {
    super(subsystem, expiration);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#getObject(K)
   */
  public T getObject(K key) throws DelegateException {
    Connection c = getConnection();
    T object = null;

    try {
      IQueryByKey<S> query = factory.getQueryByKey(getQueryByKeySql());
      S queriedDto = query.queryByKey(c, key);
      if (queriedDto != null) {
        object = wrapData(queriedDto);
      }
    } catch (SQLException e) {
      logger.error(getQueryByKeySql().getSql(key));
      logger.error("Key value: " + key.getValueList());
      throw new DelegateException(e.getMessage(), e);
    } catch (NoPrimaryKeyException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (BatchException e) {
      throw new DelegateException(e.getMessage(), e);
    } finally {
      close(c);
    }
    return object;
  }

  @Override
  public T getDatabaseObject(K key) throws DelegateException {
    Connection c = getConnection();
    T object = null;

    try {
      IQueryByKey<S> query = factory.getDatabaseQueryByKey(getQueryByKeySql());
      S queriedDto = query.queryByKey(c, key);
      if (queriedDto != null) {
        object = wrapData(queriedDto);
      }
    } catch (SQLException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (NoPrimaryKeyException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (BatchException e) {
      throw new DelegateException(e.getMessage(), e);
    } finally {
      close(c);
    }
    return object;
  }

  @Override
  public T getDatabaseObject(K key, int expiration) throws DelegateException {
    Connection c = getConnection();
    T object = null;

    try {
      IQueryByKey<S> query = factory.getDatabaseQueryByKey(getQueryByKeySql());
      query.setExpiration(expiration);
      S queriedDto = query.queryByKey(c, key);
      if (queriedDto != null) {
        object = wrapData(queriedDto);
      }
    } catch (SQLException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (NoPrimaryKeyException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (BatchException e) {
      throw new DelegateException(e.getMessage(), e);
    } finally {
      close(c);
    }
    return object;
  }

  /**
   * The concrete subclass overrides this abstract method to provide a specific
   * SQL statement object for queries.
   * 
   * <pre>
   * <code>
   * &#064;Override
   * protected IKeyQuerySql getQueryByKeySql() {
   *   return new TestNaturalKeyQuerySql();
   * }
   * </code>
   * </pre>
   * 
   * @return the SELECT SQL statement object
   */
  abstract protected IKeyQuerySql<S> getQueryByKeySql();

  /**
   * <p>
   * Wrap a Data Access Data Transfer Object (DTO) inside a Business DTO. This
   * abstract method lets you call the appropriate constructor on the business
   * DTO as part of the concrete implementation. Here is an example
   * implementation using TestNatural and returning BsTestNatural:
   * </p>
   * 
   * <pre>
   * <code>
   * &#064;Override
   * BsTestNatural wrapData(TestNatural dto) {
   *   return new BsTestNatural(dto);
   * }
   * </code>
   * </pre>
   * 
   * @param dto the data access DTO
   * @return the business DTO
   */
  abstract protected T wrapData(S dto);

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#getAllObjects(int)
   */
  public List<T> getAllObjects(int rows) throws DelegateException {
    Connection c = getConnection();
    List<T> list = new ArrayList<T>();
    try {
      IQueryList<S> query = factory.getQueryList(getQueryListSql(), rows);
      List<S> objects = query.query(c);
      for (S object : objects) {
        // Unchecked conversion of IDto to type S here
        T dto = wrapData((S)object);
        list.add(dto);
      }
    } catch (ConstraintViolationException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (SQLException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (BatchException e) {
      throw new DelegateException(e.getMessage(), e);
    }  finally {
      close(c);
    }

    return list;
  }

  /**
   * The concrete subclass overrides this abstract method to provide a specific
   * SQL statement object for multiple-object queries.
   * 
   * <pre>
   * <code>
   * &#064;Override
   * protected IQuerySql getQueryListSql() {
   *   return new TestNaturalAllQuerySql();
   * }
   * <code>
   * </pre>
   * 
   * @return the SELECT SQL statement object
   */
  abstract protected IQuerySql<S> getQueryListSql();
}