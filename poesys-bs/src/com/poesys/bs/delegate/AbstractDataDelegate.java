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


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;

import com.poesys.bs.dto.IDto;
import com.poesys.db.Message;
import com.poesys.db.NoPrimaryKeyException;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.PoesysTrackingThread;
import com.poesys.db.dao.ddl.ExecuteSql;
import com.poesys.db.dao.ddl.IExecuteSql;
import com.poesys.db.dao.ddl.ISql;
import com.poesys.db.dao.ddl.TruncateTableSql;
import com.poesys.db.dao.delete.IDeleteBatch;
import com.poesys.db.dao.delete.IDeleteSql;
import com.poesys.db.dao.insert.IInsertBatch;
import com.poesys.db.dao.insert.IInsertSql;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IQueryByKey;
import com.poesys.db.dao.query.IQueryList;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dao.update.IUpdateBatch;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dto.IDbDto;
import com.poesys.db.dto.IDbDto.Status;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An abstract base class that implements the IDataDelegate interface for the
 * business delegates in the application that require managed transactions in
 * the database. The abstract class contains the specific methods for querying,
 * inserting, updating, deleting, and truncating data in a class and its nested
 * object classes.
 * </p>
 * <p>
 * Business delegates should throw only DelegateException exceptions, so place
 * all code that throws checked exceptions in try-catch blocks that handle the
 * exceptions or throw a DelegateException with an appropriate message. The
 * message should be translated if you are using a property from the I18N
 * properties file by using the Message.getMessage() method.
 * </p>
 * <p>
 * Each subclass must set the delegateName protected member with the class name
 * of the delegate for error reporting.
 * </p>
 * <p>
 * This version of Poesys/DB is a redesign that maintains the insert, update,
 * and delete methods for backward compatibility but under the covers uses only
 * the process method, centralizing delegate processing in that method to
 * improve cohesion and maintainability. It also introduces the insert(object)
 * and process(object) methods for single-object inserts.
 * </p>
 * 
 * @author Robert J. Muller
 * @param <T> the business layer DTO type
 * @param <S> the database layer DTO type
 * @param <K> the primary key type
 */
abstract public class AbstractDataDelegate<T extends IDto<S>, S extends IDbDto, K extends IPrimaryKey>
    extends AbstractDaoDelegate<S> implements IDataDelegate<T, S, K> {
  /** Logger for this class */
  private static final Logger logger =
    Logger.getLogger(AbstractDataDelegate.class);

  /**
   * The list of DTOs to process, must be null at end of using method for
   * reentrancy
   */
  protected List<T> list = null;

  protected final String delegateName;

  /** timeout for the query thread */
  private static final int TIMEOUT = 10000 * 60;

  static {
    List<String> names = new ArrayList<String>(1);
    names.add("com.poesys.bs.PoesysBsBundle");
    Message.initializePropertiesFiles(names);
  }

  /** Error message when thread is interrupted or timed out */
  private static final String THREAD_ERROR = "com.poesys.db.dao.msg.thread";
  /** Error message when tracking thread gets exception */
  private static final String PROCESSING_ERROR =
    "com.poesys.bs.delegate.msg.processing";

  /**
   * Standard constructor that sets the name of the subsystem and the database
   * type for construction of connections to the database.
   * 
   * @param subsystem the name of the subsystem
   * @param dbms the kind of database that implements the subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractDataDelegate(String subsystem, DBMS dbms, Integer expiration) {
    super(subsystem, dbms, expiration);
    delegateName = AbstractDataDelegate.class.getName();
  }

  /**
   * Standard constructor that sets the name of the subsystem and the database
   * type for construction of connections to the database.
   *
   * @param className the name of the concrete subclass to instantiate
   * @param subsystem the name of the subsystem
   * @param dbms the kind of database that implements the subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractDataDelegate(String className, String subsystem, DBMS dbms, Integer expiration) {
    super(subsystem, dbms, expiration);
    delegateName = className;
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
    delegateName = AbstractDataDelegate.class.getName();
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
    // pass to expiration-based method with no expiration
    return getObject(key, -1);
  }

  @Override
  public T getObject(K key, int expiration) throws DelegateException {
    T object = null;

    try {
      IQueryByKey<S> query =
        factory.getQueryByKey(getQueryByKeySql(), subsystem);
      if (expiration != -1) {
        query.setExpiration(expiration);
      }
      S queriedDto = query.queryByKey(key);
      if (queriedDto != null) {
        object = wrapData(queriedDto);
      }
    } catch (NoPrimaryKeyException e) {
      throw new DelegateException(e.getMessage(), e);
    }

    return object;
  }

  @Override
  public T getDatabaseObject(K key) throws DelegateException {
    return getDatabaseObject(key, -1);
  }

  @Override
  public T getDatabaseObject(K key, int expiration) throws DelegateException {
    T object = null;

    try {
      IQueryByKey<S> query =
        factory.getDatabaseQueryByKey(getQueryByKeySql(), subsystem);
      if (expiration != -1) {
        query.setExpiration(expiration);
      }
      S queriedDto = query.queryByKey(key);
      if (queriedDto != null) {
        object = wrapData(queriedDto);
      }
    } catch (NoPrimaryKeyException e) {
      throw new DelegateException(e.getMessage(), e);
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

  @Override
  public List<T> getAllObjects(int rows) throws DelegateException {
    return getAllObjects(rows, -1);
  }

  @Override
  public List<T> getAllObjects(int rows, int expiration)
      throws DelegateException {
    List<T> list = new ArrayList<T>();

    try {
      IQueryList<S> query =
        factory.getQueryList(getQueryListSql(), subsystem, rows);
      if (expiration != -1) {
        query.setExpiration(expiration);
      }
      List<S> objects = query.query();
      for (S object : objects) {
        // Unchecked conversion of IDto to type S here
        T dto = wrapData((S)object);
        list.add(dto);
      }
    } catch (Throwable e) {
      throw new DelegateException(e.getMessage(), e);
    }

    return list;
  }

  /**
   * The concrete subclass overrides this abstract method to provide a specific
   * SQL statement object for multiple-object queries.
   * 
   * <pre>
   * &#064;Override
   * protected IQuerySql getQueryListSql() {
   *   return new TestNaturalAllQuerySql();
   * }
   * </pre>
   * 
   * @return the SELECT SQL statement object
   */
  abstract protected IQuerySql<S> getQueryListSql();

  @Override
  public void insert(List<T> list) throws DelegateException {
    process(list);
  }

  @Override
  public void insert(T object) throws DelegateException {
    List<T> list = new ArrayList<T>(1);
    list.add(object);
    process(list);
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

  @Override
  public void update(T object) throws DelegateException {
    List<T> list = new ArrayList<T>(1);
    list.add(object);
    process(list);
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

  @Override
  public void updateBatch(List<T> list) throws DelegateException {
    process(list);
  }

  @Override
  public void delete(T object) throws DelegateException {
    List<T> list = new ArrayList<T>(1);
    list.add(object);
    process(list);
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

  @Override
  public void deleteBatch(List<T> list) throws DelegateException {
    process(list);
  }

  @Override
  public void process(List<T> list) throws DelegateException {
    // Use a tracking thread to maintain a single transaction for all processing
    // within this method.
    Runnable query = getRunnable();
    PoesysTrackingThread thread = new PoesysTrackingThread(query, subsystem);
    // Set the instance list member to the incoming list to enable processing.
    this.list = list;
    thread.start();
    // Join the thread, blocking until the thread completes or
    // until the query times out.
    try {
      thread.join(TIMEOUT);
      if (thread.getThrowable() != null) {
        // If there are any batch errors, throw an exception.
        List<String> errors = thread.getBatchErrors();
        StringBuilder builder = new StringBuilder();
        if (errors.size() > 0) {
          builder.append("Batch processing failed for these DTOs: ");
          String sep = "";
          for (String errorKey : errors) {
            builder.append(sep);
            builder.append(errorKey);
            sep = ", ";
          }
          // Note: if there is an exception, this method throws it to the caller,
          // which should wrap it in a DelegateException.
        }
        Object[] args = { builder.toString() };
        String message = Message.getMessage(PROCESSING_ERROR, args);
        throw new DelegateException(message, thread.getThrowable());
      }
    } catch (InterruptedException e) {
      // Log and ignore this exception.
      Object[] args = { "process list", delegateName };
      String message = Message.getMessage(THREAD_ERROR, args);
      logger.error(message, e);
    }

    // Set the instance list member to null to make this method reentrant.
    list = null;
  }

  /**
   * Get a Runnable object for the tracking thread to run.
   * 
   * @return the Runnable object
   */
  private Runnable getRunnable() {
    Runnable runnable = new Runnable() {
      public void run() {
        // Get the tracking thread.
        PoesysTrackingThread thread =
          (PoesysTrackingThread)Thread.currentThread();
        try {
          doProcessing(thread);
        } catch (Throwable e) {
          thread.setThrowable(e);
        } finally {
          if (thread != null) {
            thread.closeConnection();
          }
        }
      }
    };
    return runnable;
  }

  /**
   * Process the list of DTOs using the appropriate DAOs. Throws a
   * DelegateException with any batch-processing errors.
   * 
   * @param thread the Poesys tracking thread for the transaction
   */
  private void doProcessing(PoesysTrackingThread thread) {
    // Create the 3 DAOs for inserting, updating, and deleting.
    IInsertBatch<S> inserter = factory.getInsertBatch(getInsertSql());
    IUpdateBatch<S> updater = factory.getUpdateBatch(getUpdateSql());
    IDeleteBatch<S> deleter = factory.getDeleteBatch(getDeleteSql());

    Collection<S> dtos = convertDtoList(list);

    // Add the EXISTING DTOs to the tracking thread. The DAOs never process
    // EXISTING DTOs.
    for (IDbDto dto : dtos) {
      if (dto.getStatus() == Status.EXISTING
          && thread.getDto(dto.getPrimaryKey()) == null) {
        // Not in thread yet, add it to track the DTO.
        thread.addDto(dto);
      }
    }

    try {
      // Each DAO processes the top-level DTO according to its status.
      if (deleter != null) {
        deleter.delete(dtos, dtos.size() / 2);
      }

      // Inserter always exists.
      inserter.insert(dtos, dtos.size() / 2);
      // INSERT done, set NEW to EXISTING
      for (IDbDto dto : dtos) {
        if (dto.getStatus().equals(Status.NEW)) {
          dto.setExisting();
        }
      }

      if (updater != null) {
        updater.update(dtos, dtos.size() / 2);
      }

      postprocess(dtos, thread);
    } finally {
      // Finalize inserts and deletes.
      finalizeStatus(dtos, Status.EXISTING);
      finalizeStatus(dtos, Status.DELETED);
    }
  }

  /**
   * Post-process the DTOs by processing the nested objects within each DTO,
   * inserting/updating/deleting them as required by their status. Mark each DTO
   * as fully processed when the post-processing is complete to prevent further
   * post-processing through subsequence DTOs. All processing happens within the
   * existing tracking thread and hence within a single transaction.
   * 
   * @param dtos the list of DTOs to process
   * @param thread the tracking thread
   */
  protected void postprocess(Collection<S> dtos, PoesysTrackingThread thread) {
    for (S dto : dtos) {
      dto.postprocessNestedObjects();
    }
  }

  @Override
  public void process(T object) throws DelegateException {
    List<T> list = new ArrayList<T>(1);
    list.add(object);
    process(list);
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
   * @param <R> the IDbDto type of the collection to update
   * @param dtos a collection of DTO objects
   */
  protected <R extends IDbDto> void updateChangedToExisting(Collection<R> dtos) {
    for (R dto : dtos) {
      if (dto.getStatus() == Status.CHANGED) {
        dto.setExisting();
      }
    }
  }

  @Override
  public void truncateTable(String tableName) throws DelegateException {
    ISql sql = new TruncateTableSql(tableName);
    IExecuteSql executive = new ExecuteSql(sql, subsystem);
    executive.execute();
  }
}