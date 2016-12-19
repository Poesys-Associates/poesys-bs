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


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.poesys.bs.dto.AbstractDto;
import com.poesys.db.connection.ConnectionException;
import com.poesys.db.connection.ConnectionFactoryFactory;
import com.poesys.db.connection.IConnectionFactory;
import com.poesys.db.dao.DaoManagerFactory;
import com.poesys.db.dao.IDaoManager;
import com.poesys.db.dto.IDbDto;


/**
 * <p>
 * An abstract base class for the business delegates in the application that
 * require a database connection and managed transactions. This abstract class
 * requires classes that support getting and closing a database connection and
 * managing transactions with commit and rollback. It has no further methods and
 * so is useful in situations where you need a database connection and
 * transactions but you don't want the standard DTO-oriented API.
 * </p>
 * <p>
 * The primary goal of the Data Delegate is to proxy the business-level services
 * based on database access for the presentation layer.
 * </p>
 * <p>
 * The concrete subclass implements the abstract methods, most of which create
 * the appropriate SQL objects and the DAOs that use them. The code examples in
 * this documentation show a simple implementation with direct instantiation of
 * classes. You can achieve more sophisticated goals by implementing DAO and/or
 * SQL class factories, then using the factories to implement the abstract
 * methods in this class. This kind of approach, for example, would let you
 * support different SQL statements for different DBMS implementations of the
 * application, with the SQL statement factory generating the appropriate SQL
 * statements based on the identification of the DBMS. You could also add a
 * factory method that generates the delegate object by passing in the
 * appropriate DBMS identification to the constructor.
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
 */
abstract public class AbstractConnectionDelegate {
  /** Log4j logger for the class */
  Logger logger = Logger.getLogger(AbstractConnectionDelegate.class);

  /** Error message on closing a connection */
  protected static final String CLOSE_CONNECTION =
    "com.poesys.bs.delegate.msg.closeConnectionError";

  /** Error message on getting a connection */
  protected static final String NO_CONNECTION =
    "com.poesys.bs.delegate.msg.noConnection";

  /** Error message on rolling back a connection */
  protected static final String ROLLBACK =
    "com.poesys.bs.delegate.msg.rollbackError";

  /** Error message on committing a connection */
  protected static final String COMMIT =
    "com.poesys.bs.delegate.msg.commitError";

  /**
   * The name of the subsystem, for use in the resource bundle containing JDBC
   * parameters
   */
  protected final String subsystem;

  /** The type of the database for the subsystem */
  protected final IConnectionFactory.DBMS dbms;

  /** The connection factory */
  private final IConnectionFactory factory;

  /**
   * Standard constructor that sets the name of the subsystem and the database
   * type for construction of connections to the database. A common use for this
   * constructor is to supply JNDI as the dbms when you are executing within an
   * application server container.
   * 
   * @param subsystem the name of the subsystem
   * @param dbms the kind of database that implements the subsystem
   */
  public AbstractConnectionDelegate(String subsystem,
                                    IConnectionFactory.DBMS dbms) {
    this.subsystem = subsystem;
    this.dbms = dbms; // before creating factory
    this.factory = createFactory();
  }

  /**
   * Create a connection delegate by getting the target DBMS and password from
   * the properties file for the subsystem. The most common use for this
   * constructor is when you are executing standalone, such as in a JUnit
   * testing environment or a standalone application.
   * 
   * @param subsystem the data source subsystem
   */
  public AbstractConnectionDelegate(String subsystem) {
    this.subsystem = subsystem;
    this.factory = createFactory();
    this.dbms = factory.getDbms(); // after creating factory
  }

  /**
   * Get the connection factory for the delegate to use.
   * 
   * @return the factory
   */
  private IConnectionFactory createFactory() {
    IConnectionFactory factory;
    try {
      factory = ConnectionFactoryFactory.getInstance(subsystem, dbms);
    } catch (IllegalArgumentException e) {
      throw new DelegateException(e.getMessage(), e);
    } catch (IOException e) {
      throw new DelegateException(e.getMessage(), e);
    }
    return factory;
  }

  /**
   * Get a connection.
   * 
   * @return an open connection to the database
   * @throws DelegateException when the connection factory does not return a
   *           connection
   */
  protected Connection getConnection() throws DelegateException {
    Connection connection;
    try {
      connection = factory.getConnection();
    } catch (SQLException e) {
      throw new DelegateException(NO_CONNECTION, e);
    } catch (IllegalArgumentException e) {
      throw new DelegateException(NO_CONNECTION, e);
    }
    return connection;
  }

  /**
   * Flush all resources associated with connections. This will close all
   * connections and any connection pool associated with the application and
   * will flush any temporary caches.
   * 
   * @throws ConnectionException when there is a problem releasing resources
   */
  public void flush() throws ConnectionException {
    factory.flush();
  }

  /**
   * Flush a specific DTO from the cache.
   * 
   * @param dto the DTO to flush
   */
  public void flush(IDbDto dto) {
    IDaoManager manager = DaoManagerFactory.getManager(subsystem);
    manager.removeObjectFromCache(dto.getClass().getName(), dto.getPrimaryKey());
  }

  /**
   * Get the connection factory for use in lazy loading.
   * 
   * @return a connection factory
   */
  protected IConnectionFactory getFactory() {
    return factory;
  }

  /**
   * Commit a connection, handling any exceptions from commit processing.
   * 
   * @param connection the connection to commit
   * @throws DelegateException when commit results in a SQL exception
   */
  protected void commit(Connection connection) throws DelegateException {
    if (connection == null) {
      throw new DelegateException(NO_CONNECTION);
    }
    try {
      // Test for autocommit being on before committing to avoid exception.
      if (connection.getAutoCommit()) {
        logger.info("Autocommit on and commit attempted");
      } else if (!connection.isClosed()) {
        connection.commit();
      }
    } catch (SQLException e) {
      throw new DelegateException(NO_CONNECTION, e);
    }
  }

  /**
   * Roll back a transaction, then throw a delegate exception that encapsulates
   * the exception that caused the rollback.
   * 
   * @param connection the open connection to roll back
   * @param key the bundle key for the error message
   * @param e the exception that resulted in the rollback
   * @throws DelegateException always, either with the passed exception
   *           encapsulated or with a rollback-problem exception, if there was a
   *           problem rolling back
   */
  protected void rollBack(Connection connection, String key, Throwable e)
      throws DelegateException {
    if (connection == null) {
      throw new DelegateException(NO_CONNECTION);
    }

    try {
      // Test for autocommit being on before committing to avoid exception.
      if (connection.getAutoCommit()) {
        logger.info("Autocommit on and commit attempted");
      } else if (!connection.isClosed()) {
        connection.rollback();
      }
      throw new DelegateException(key, e);
    } catch (SQLException s) {
      // Handle a DBMS problem with rollback.
      throw new DelegateException(ROLLBACK, s);
    }
  }

  /**
   * Close a connection and handle any exceptions from the attempt.
   * 
   * @param connection the open connection to close
   * @throws DelegateException when there is a problem closing the connection
   */
  protected void close(Connection connection) throws DelegateException {
    if (connection != null) {
      try {
        if (!connection.isClosed()) {
          int connectionId = connection.hashCode();
          connection.close();
          logger.debug("Closed connection " + connectionId);
        }
      } catch (SQLException e) {
        throw new DelegateException(CLOSE_CONNECTION, e);
      }
    }
  }

  /**
   * Get the subsystem.
   * 
   * @return a subsystem
   */
  public String getSubsystem() {
    return subsystem;
  }

  /**
   * Get the dbms.
   * 
   * @return a dbms
   */
  public IConnectionFactory.DBMS getDbms() {
    return dbms;
  }
}