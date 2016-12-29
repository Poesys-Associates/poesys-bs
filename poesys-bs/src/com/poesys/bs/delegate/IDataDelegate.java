/*
 * Copyright (c) 2008 Carnegie Institution for Science. All rights reserved.
 */
package com.poesys.bs.delegate;


import java.util.List;

import com.poesys.bs.dto.IDto;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An interface for the business delegates in the application that require a
 * database connection and managed transactions.
 * </p>
 * <p>
 * The primary goal of the Data Delegate is to proxy the business-level services
 * for the presentation layer based on a persistent database layer. The concrete
 * implementation will implement the methods for all the required services,
 * mostly supplying SQL objects. Such methods should use business-layer data
 * transfer objects (DTOs) to move data in and out of the system (type parameter
 * T). These DTOs provide object data and services to the user interface layer
 * and are thus different from the DTOs in the data access layer (type parameter
 * S). The business delegate gets data using data-access-layer DTOs and
 * transforms these DTOs into the business layer DTOs (which may actually wrap a
 * data-access DTO as its internal implementation).
 * </p>
 * <p>
 * The interface permits a delegate factory to generate objects of an
 * appropriate implementation given the target data access layer (for example,
 * there could be separate implementations for JDBC access, EJB access, or JMS
 * access to data). The UI layer uses the interface to access the services
 * implemented by the concrete implementation.
 * </p>
 * <p>
 * It will often be the case that you add services to the delegate in a specific
 * application beyond those supported here. If so, subclass this interface with
 * a more specific interface that contains the additional services and write a
 * factory to generate the appropriate concrete implementations of the
 * sub-interface.
 * </p>
 * 
 * @author Robert J. Muller
 * 
 * @param <T> the business layer DTO type
 * @param <S> the data-access layer DTO type
 * @param <K> the primary key type
 */
public interface IDataDelegate<T extends IDto<S>, S extends com.poesys.db.dto.IDbDto, K extends IPrimaryKey> {

  /**
   * Query an object of type T based on its primary key values.
   * 
   * @param key the primary key of the object to query
   * @return an object of type T or null if no object matches the key
   * @throws DelegateException when there is a database problem, an invalid
   *           primary key, a nested-object query problem, or a problem setting
   *           initial object status
   */
  T getObject(K key) throws DelegateException;

  /**
   * Query an object of type T based on its primary key values with a specified
   * expiration time.
   * 
   * @param key the primary key of the object to query
   * @param expiration the time in milliseconds until the queried object expires
   *          in a cache
   * @return an object of type T or null if no object matches the key
   * @throws DelegateException when there is a database problem, an invalid
   *           primary key, a nested-object query problem, or a problem setting
   *           initial object status
   */
  T getObject(K key, int expiration) throws DelegateException;

  /**
   * Query an object of type T based on its primary key values directly from the
   * database, replacing any cached version of the object.
   * 
   * @param key the primary key of the object to query
   * @return an object of type T or null if no object matches the key
   * @throws DelegateException when there is a database problem, an invalid
   *           primary key, a nested-object query problem, or a problem setting
   *           initial object status
   */
  T getDatabaseObject(K key) throws DelegateException;

  /**
   * Query an object of type T based on its primary key values with a specified
   * expiration time directly from the database, replacing any cached version of
   * the object.
   * 
   * @param key the primary key of the object to query
   * @param expiration the time in milliseconds until the queried object expires
   *          in a cache
   * @return an object of type T or null if no object matches the key
   * @throws DelegateException when there is a database problem, an invalid
   *           primary key, a nested-object query problem, or a problem setting
   *           initial object status
   */
  T getDatabaseObject(K key, int expiration) throws DelegateException;

  /**
   * Get a list of all the objects of type T in the database.
   * 
   * @param rows the number of rows to fetch at once, optimizes large queries; 0
   *          means the default
   * 
   * @return a list of T objects
   * @throws DelegateException when there is a constraint violation, a problem
   *           with executing the query, a problem with nested-object queries,
   *           or a problem assigning initial DTO status
   */
  List<T> getAllObjects(int rows) throws DelegateException;

  /**
   * Get a list of all the T objects in the database, setting the expiration
   * time to a specified value when caching the objects.
   * 
   * @param rows the number of rows to fetch at once, optimizes large queries; 0
   *          means the default
   * @param expiration the time in milliseconds until the queried objects expire
   *          in the cache
   * 
   * @return a list of T objects
   * @throws DelegateException when there is a constraint violation, a problem
   *           with executing the query, a problem with nested-object queries,
   *           or a problem assigning initial DTO status
   */
  List<T> getAllObjects(int rows, int expiration) throws DelegateException;

  /**
   * Insert a list of objects of type T into the database.
   * 
   * @param list the list of T objects
   * @throws DelegateException when there is a problem inserting the objects
   */
  void insert(List<T> list) throws DelegateException;

  /**
   * Insert an object of type T.
   * 
   * @param object the object of type T
   * @throws DelegateException when there is a problem inserting the objects
   */
  void insert(T object) throws DelegateException;

  /**
   * Update an object of type T, updating all changeable values.
   * 
   * @param object the object of type T to update
   * @throws DelegateException if there is a problem updating the object
   */
  void update(T object) throws DelegateException;

  /**
   * Update a list of objects of type T, updating all changeable values.
   * 
   * @param list the list of objects of type T
   * @throws DelegateException when there is a problem updating the objects
   */
  void updateBatch(List<T> list) throws DelegateException;

  /**
   * Delete an object of type T.
   * 
   * @param object the object to delete
   * @throws DelegateException when there is a problem deleting the object
   */
  void delete(T object) throws DelegateException;

  /**
   * Delete a list of objects of type T.
   * 
   * @param list the list of T objects
   * @throws DelegateException when there is a problem deleting the objects
   */
  void deleteBatch(List<T> list) throws DelegateException;

  /**
   * Process a list of objects of type T in various statuses. The method
   * inserts, updates, or deletes objects in the list based on each object's
   * status, then processes all nested objects.
   * 
   * @param list the list of T objects
   * @throws DelegateException when there is a problem processing the objects
   */
  void process(List<T> list) throws DelegateException;

  /**
   * Process an object of type T in various statuses. The method inserts,
   * updates, or deletes the object based on the status of the object, then
   * processes all nested objects.
   * 
   * @param object the object of type T
   * @throws DelegateException when there is a problem processing the objects
   */
  void process(T object) throws DelegateException;

  /**
   * Truncate a table, removing all rows. This is a Data Definition Language
   * statement that will commit any open transaction. Note that you must have
   * privileges to truncate the table in the underlying DBMS.
   * 
   * @param tableName the name of the table to truncate
   * @throws DelegateException when there is a SQL exception on the attempt to
   *           truncate the table
   */
  void truncateTable(String tableName) throws DelegateException;
}