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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.poesys.bs.dto.AbstractDto;
import com.poesys.bs.dto.IDto;
import com.poesys.db.BatchException;
import com.poesys.db.ConstraintViolationException;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dao.ddl.ExecuteSql;
import com.poesys.db.dao.ddl.IExecuteSql;
import com.poesys.db.dao.ddl.ISql;
import com.poesys.db.dao.ddl.TruncateTableSql;
import com.poesys.db.dao.delete.IDelete;
import com.poesys.db.dao.delete.IDeleteBatch;
import com.poesys.db.dao.delete.IDeleteSql;
import com.poesys.db.dao.insert.IInsertBatch;
import com.poesys.db.dao.insert.IInsertSql;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dao.query.IQueryList;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dao.update.IUpdate;
import com.poesys.db.dao.update.IUpdateBatch;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An abstract base class that implements the IDataDelegate interface for the
 * JDBC-based business delegates in the application that require a JDBC database
 * connection and managed transactions. The abstract class contains the specific
 * methods for querying, inserting, updating, deleting, and truncating data in a
 * database table and inherit the internal methods for managing connections and
 * transactions from the AbstractConnectionDelegate superclass.
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
 * The implementation abstracts the SQL-specification-object instatiations
 * required by the various methods. A concrete subclass simply implements those
 * SQL statement construction methods to create a working delegate.
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
 * @see com.poesys.db.dto.AbstractDto
 * @see com.poesys.db.pk.IPrimaryKey
 * 
 * @author Robert J. Muller
 * @param <T> the business layer DTO type
 * @param <S> the database layer DTO type
 * @param <K> the primary key type
 */
abstract public class AbstractDataDelegate<T extends IDto<S>, S extends IDbDto, K extends IPrimaryKey>
    extends AbstractDaoDelegate<S> implements IDataDelegate<T, S, K> {

  /** No Object error message tag */
  private static final String NO_OBJECT_MSG =
    "com.poesys.bs.delegate.msg.noObject";

  /**
   * Standard constructor that sets the name of the subsystem and the database
   * type for construction of connections to the database.
   * 
   * @param subsystem the name of the subsystem
   * @param dbms the kind of database that implements the subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractDataDelegate(String subsystem,
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
  public AbstractDataDelegate(String subsystem, Integer expiration) {
    super(subsystem, expiration);
  }

  /**
   * Get the fully qualified class name of the IDto concrete subclass that this
   * Data Delegate manages (type T). It has to be passed in because due to type
   * erasure there is no way to get it from the generic parameter.
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

  @Override
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
  public T getObject(K key, int expiration) throws DelegateException {
    Connection c = getConnection();
    T object = null;

    try {
      IQueryByKey<S> query = factory.getQueryByKey(getQueryByKeySql());
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
    } finally {
      close(c);
    }

    return list;
  }

  @Override
  public List<T> getAllObjects(int rows, int expiration)
      throws DelegateException {
    Connection c = getConnection();
    List<T> list = new ArrayList<T>();

    try {
      IQueryList<S> query = factory.getQueryList(getQueryListSql(), rows);
      query.setExpiration(expiration);
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
    } finally {
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

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#insert(java.util.List)
   */
  public void insert(List<T> list) throws DelegateException {
    Connection c = getConnection();
    IInsertBatch<S> inserter = factory.getInsertBatch(getInsertSql());

    Collection<S> dtos = convertDtoList(list);

    try {
      inserter.insert(c, dtos, dtos.size() / 2);
      // INSERT done, update status to EXISTING
      for (IDbDto dto : dtos) {
        dto.setExisting();
      }
    } catch (ConstraintViolationException e) {
      rollBack(c, e.getMessage(), e);
    } catch (SQLException e) {
      rollBack(c, e.getMessage(), e);
    } catch (BatchException e) {
      // Don't roll back the whole transaction; the DBMS rolls back the
      // individual inserts that failed, but the rest should be committed.
      throw new DelegateException(e.getMessage(), e);
    } finally {
      commit(c);
      close(c);
      finalizeStatus(dtos, Status.EXISTING);
    }
  }

  /**
   * The concrete subclass overrides this abstract method to provide a specific
   * SQL statement object for inserts.
   * 
   * <pre>
   * <code>
   * &#064;Override
   * protected IInsertSql getInsertSql() {
   *   return new InsertSqlTestNatural();
   * }
   * </code>
   * </pre>
   * 
   * @return the INSERT SQL statement object
   */
  abstract protected IInsertSql<S> getInsertSql();

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#update(T)
   */
  public void update(T object) throws DelegateException {
    Connection c = getConnection();

    // Create the DAO for updating S objects.
    IUpdate<S> updater = factory.getUpdate(getUpdateSql());

    // Update the object using the DAO if the object is updatable.
    if (updater != null) {
      try {
        updater.update(c, object.toDto());
      } catch (ConstraintViolationException e) {
        rollBack(c, e.getMessage(), e);
      } catch (SQLException e) {
        rollBack(c, e.getMessage(), e);
      } catch (BatchException e) {
        // A batch error happened on some nested object list; don't roll back
        // the whole transaction, just let the DBMS roll back the operation that
        // failed.
        throw new DelegateException(e.getMessage(), e);
      } finally {
        commit(c);
        close(c);
      }
    }
  }

  /**
   * The concrete subclass overrides this abstract method to provide a specific
   * SQL statement object for updates.
   * 
   * <pre>
   * <code>
   * &#064;Override
   * protected IUpdateSql getUpdateSql() {
   *   return new UpdateSqlTestNatural();
   * }
   * </code>
   * </pre>
   * 
   * @return the UPDATE SQL statement object
   */
  abstract protected IUpdateSql<S> getUpdateSql();

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#updateBatch(java.util.List)
   */
  public void updateBatch(List<T> list) throws DelegateException {
    Connection c = getConnection();
    IUpdateBatch<S> updater = factory.getUpdateBatch(getUpdateSql());

    // Update if the object is updatable.
    if (updater != null) {
      Collection<S> dtos = convertDtoList(list);

      try {
        updater.update(c, dtos, dtos.size() / 2);
      } catch (ConstraintViolationException e) {
        rollBack(c, e.getMessage(), e);
      } catch (SQLException e) {
        rollBack(c, e.getMessage(), e);
      } catch (BatchException e) {
        // Don't roll back the whole transaction; the DBMS rolls back the
        // individual inserts that failed, but the rest should be committed.
        throw new DelegateException(e.getMessage(), e);
      } finally {
        commit(c);
        close(c);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#delete(T)
   */
  public void delete(T object) throws DelegateException {
    if (object == null) {
      throw new DelegateException(NO_OBJECT_MSG);
    }

    Connection c = getConnection();
    IDelete<S> deleter = factory.getDelete(getDeleteSql());

    try {
      // Set the object's status to delete.
      object.delete();
      // Delete the object with the DAO; object must implement IDbDto interface.
      deleter.delete(c, object.toDto());
    } catch (ConstraintViolationException e) {
      rollBack(c, e.getMessage(), e);
    } catch (SQLException e) {
      rollBack(c, e.getMessage(), e);
    } catch (BatchException e) {
      // A batch error happened on some nested object list; don't roll back
      // the whole transaction, just let the DBMS roll back the operation that
      // failed.
      throw new DelegateException(e.getMessage(), e);
    } finally {
      commit(c);
      close(c);
    }
  }

  /**
   * The concrete subclass overrides this abstract method to provide a specific
   * SQL statement object for deletes.
   * 
   * <pre>
   * <code>
   * &#064;Override
   * protected IDeleteSql getDeleteSql() {
   *   return new DeleteSqlTestNatural();
   * }
   * </code>
   * </pre>
   * 
   * @return the DELETE SQL statement object
   */
  abstract protected IDeleteSql<S> getDeleteSql();

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#deleteBatch(java.util.List)
   */
  public void deleteBatch(List<T> list) throws DelegateException {
    Connection c = getConnection();
    IDeleteBatch<S> deleter = factory.getDeleteBatch(getDeleteSql());
    Collection<S> dtos = convertDtoList(list);

    try {
      deleter.delete(c, dtos, dtos.size() / 2);
    } catch (ConstraintViolationException e) {
      rollBack(c, e.getMessage(), e);
    } catch (SQLException e) {
      rollBack(c, e.getMessage(), e);
    } catch (BatchException e) {
      // Don't roll back the whole transaction; the DBMS rolls back the
      // individual inserts that failed, but the rest should be committed.
      throw new DelegateException(e.getMessage(), e);
    } finally {
      commit(c);
      close(c);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#process(java.util.List)
   */
  public void process(List<T> list) throws DelegateException {
    Connection c = getConnection();

    // Create the 3 DAOs for inserting, updating, and deleting.
    IInsertBatch<S> inserter = factory.getInsertBatch(getInsertSql());
    IUpdateBatch<S> updater = factory.getUpdateBatch(getUpdateSql());
    IDeleteBatch<S> deleter = factory.getDeleteBatch(getDeleteSql());

    Collection<S> dtos = convertDtoList(list);

    // Delete, insert, and update the objects. Each DAO will process only those
    // objects that have the appropriate status for the operation.
    try {
      if (deleter != null) {
        deleter.delete(c, dtos, dtos.size() / 2);
      }
      // Inserter always exists.
      inserter.insert(c, dtos, dtos.size() / 2);
      // INSERT done, set NEW to EXISTING
      for (IDbDto dto : dtos) {
        if (dto.getStatus().equals(Status.NEW)) {
          dto.setExisting();
        }
      }
      if (updater != null) {
        updater.update(c, dtos, dtos.size() / 2);
      }
    } catch (ConstraintViolationException e) {
      rollBack(c, e.getMessage(), e);
    } catch (SQLException e) {
      rollBack(c, e.getMessage(), e);
    } catch (BatchException e) {
      // Don't roll back the whole transaction; the DBMS rolls back the
      // individual operations that failed, but the rest should be committed.
      throw new DelegateException(e.getMessage(), e);
    } finally {
      commit(c);
      close(c);
      // Finalize inserts and deletes.
      // TODO following line should be CHANGED I think, status not reset above
      finalizeStatus(dtos, Status.EXISTING);
      finalizeStatus(dtos, Status.DELETED);
    }
  }

  /**
   * Convert an input list of business-layer DTOs of type T to an output
   * collection of type R. The implementation uses a thread-safe
   * CopyOnWriteArrayList. This requires an unchecked up-cast to the type, so
   * you should be sure the R type is a superclass of S.
   * 
   * @param <R> R is an IDbDto that is a superclass of S or S itself, which
   *          permits you to convert a list of T objects to a list of S objects
   *          or to a list of superclass of S objects
   * @param list the input DTO list
   * @return a collection of type R (data-access transfer objects)
   */
  @SuppressWarnings("unchecked")
  protected <R extends IDbDto> Collection<R> convertDtoList(List<T> list) {
    Collection<R> dbList = new CopyOnWriteArrayList<R>();
    for (T dto : list) {
      // Extract data-access DTO from business DTO.
      dbList.add((R)dto.toDto());
    }
    return dbList;
  }

  /**
   * Helper method to undo the last status change for all the IDbDto objects in
   * a collection that have EXISTS status. Use this after inserting a collection
   * into a superclass table. Usually the collection comes from the conversion
   * of the business-layer object list into a list of superclass objects
   * (up-casting). The method does not undo NEW, DELETED, or CHANGED objects,
   * just EXISTING, which are either just queried or just inserted. In the
   * former case, undo does nothing; in the latter, it sets the status back to
   * what it was before the insert happened (usually NEW).
   * 
   * @param <R> the type of the object in the list (the superclass type)
   * @param list a list of IDbDto objects
   * 
   * @see #convertDtoList(List)
   */
  protected <R extends IDbDto> void undoStatus(Collection<R> list) {
    for (R dto : list) {
      if (dto.getStatus().equals(Status.EXISTING)) {
        dto.undoStatus();
      }
    }
  }

  /**
   * Helper method to finalize the status for all the IDbDto objects in a
   * collection that have a particular status. Use this after processing a
   * collection and committing the operation to indicate that the status can no
   * longer be undone.
   * 
   * @param <R> the type of the object in the list (the superclass type)
   * @param dtos a collection of IDbDto objects
   * @param status a DTO status to finalize
   */
  protected <R extends IDbDto> void finalizeStatus(Collection<R> dtos,
                                                   Status status) {
    for (R dto : dtos) {
      if (dto.getStatus().equals(status)) {
        dto.finalizeStatus();
      }
    }
  }
  
  /**
   * Set all CHANGED DTOs in a collection to EXISTING status. You should call
   * this method after updating a collection of DTOs.
   * 
   * @param dtos a collection of DTO objects
   */
  protected <R extends IDbDto> void updateChangedToExisting(Collection<R> dtos) {
    for (R dto : dtos) {
      if (dto.getStatus() == Status.CHANGED) {
        dto.setExisting();
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.poesys.bs.delegate.IDataDelegate#truncateTable(java.lang.String)
   */
  public void truncateTable(String tableName) throws DelegateException {
    Connection c = getConnection();
    ISql sql = new TruncateTableSql(tableName);
    IExecuteSql executive = new ExecuteSql(sql);

    try {
      executive.execute(c);
    } catch (SQLException e) {
      throw new DelegateException(e.getMessage(), e);
    }
  }
}