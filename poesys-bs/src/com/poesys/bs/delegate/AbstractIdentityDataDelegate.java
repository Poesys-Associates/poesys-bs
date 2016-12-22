/*
 * Copyright (c) 2008 Poesys Associates. All rights reserved.
 */
package com.poesys.bs.delegate;


import java.util.Collection;
import java.util.List;

import com.poesys.bs.dto.AbstractDto;
import com.poesys.bs.dto.IDto;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.delete.IDeleteCollection;
import com.poesys.db.dao.insert.IInsertCollection;
import com.poesys.db.dao.update.IUpdateCollection;
import com.poesys.db.pk.IPrimaryKey;


/**
 * <p>
 * An abstract subclass of the AbstractDataDelegate for the JDBC-based business
 * delegates in the application that require a JDBC database connection and
 * managed transactions and that also have an identity or auto-generated primary
 * key. Such a key limits inserts to non-batch processing because JDBC does not
 * support retrieving the generated keys when using batch processing. The class
 * overrides the insert and process methods to use collection-based processing.
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
public abstract class AbstractIdentityDataDelegate<T extends IDto<S>, S extends com.poesys.db.dto.IDbDto, K extends IPrimaryKey>
    extends AbstractDataDelegate<T, S, K> {

  /**
   * Standard JNDI connection constructor that sets up connections by getting
   * the JNDI data source through the subsystem name.
   * 
   * @param subsystem the JNDI data source subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractIdentityDataDelegate(String subsystem, Integer expiration) {
    super(subsystem, expiration);
  }

  /**
   * Standard constructor that sets the name of the subsystem and the database
   * type for construction of connections to the database.
   * 
   * @param subsystem the name of the subsystem
   * @param dbms the kind of database that implements the subsystem
   * @param expiration the cache expiration time in milliseconds for objects
   *          this delegate caches in a cache that supports object expiration
   */
  public AbstractIdentityDataDelegate(String subsystem,
                                      DBMS dbms,
                                      Integer expiration) {
    super(subsystem, dbms, expiration);
  }

  @Override
  public void insert(List<T> list) throws DelegateException {
    IInsertCollection<S> inserter =
      factory.getInsertCollection(getInsertSql(), false);

    Collection<S> dtos = convertDtoList(list);

    // Unchecked conversion here
    inserter.insert(dtos);
  }

  @Override
  public void process(List<T> list) throws DelegateException {
    // Create the 3 DAOs for inserting, updating, and deleting.
    IInsertCollection<S> inserter =
      factory.getInsertCollection(getInsertSql(), false);
    IUpdateCollection<S> updater = factory.getUpdateCollection(getUpdateSql());
    IDeleteCollection<S> deleter = factory.getDeleteCollection(getDeleteSql());

    Collection<S> dtos = convertDtoList(list);

    // Delete, insert, and update the objects. Each DAO will process only those
    // objects that have the appropriate status for the operation.
    if (deleter != null) {
      deleter.delete(dtos);
    }
    inserter.insert(dtos);
    if (updater != null) {
      updater.update(dtos);
    }
  }
}
