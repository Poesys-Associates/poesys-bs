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
package com.poesys.bs.delegate;


import com.poesys.bs.dto.BsTestNatural;
import com.poesys.db.connection.IConnectionFactory.DBMS;
import com.poesys.db.dao.delete.DeleteSqlTestNatural;
import com.poesys.db.dao.delete.IDeleteSql;
import com.poesys.db.dao.insert.IInsertSql;
import com.poesys.db.dao.insert.InsertSqlTestNatural;
import com.poesys.db.dao.query.IKeyQuerySql;
import com.poesys.db.dao.query.IQuerySql;
import com.poesys.db.dao.query.TestNaturalAllQuerySql;
import com.poesys.db.dao.query.TestNaturalKeyQuerySql;
import com.poesys.db.dao.update.IUpdateSql;
import com.poesys.db.dao.update.UpdateSqlTestNatural;
import com.poesys.db.dto.TestNatural;
import com.poesys.db.pk.NaturalPrimaryKey;


/**
 * Tests the connection-related methods on the AbstractDataDelegate using the
 * TestNatural DTO class. This delegate simply implements query, insert, update,
 * and delete methods. This class provides a reference implementation for a
 * data-oriented business delegate. Each data-related method is a single,
 * complete transaction that gets a connection, executes the required operation,
 * and handles any exceptions by embedding them in a DelegateException.
 * 
 * @author Robert J. Muller
 */
public class TestNaturalDelegate extends
    AbstractDataDelegate<BsTestNatural, TestNatural, NaturalPrimaryKey> {
  /**
   * Create a TestNaturalDelegate object that accesses the Poesys test database
   * using MySQL.
   */
  public TestNaturalDelegate() {
    super("com.poesys.db.poesystest.mysql", DBMS.MYSQL, 100*1000);
  }

  @Override
  protected IDeleteSql<TestNatural> getDeleteSql() {
    return new DeleteSqlTestNatural();
  }

  @Override
  protected IInsertSql<TestNatural> getInsertSql() {
    return new InsertSqlTestNatural();
  }

  @Override
  protected IKeyQuerySql<TestNatural> getQueryByKeySql() {
    return new TestNaturalKeyQuerySql();
  }

  @Override
  protected IQuerySql<TestNatural> getQueryListSql() {
    return new TestNaturalAllQuerySql();
  }

  @Override
  protected IUpdateSql<TestNatural> getUpdateSql() {
    return new UpdateSqlTestNatural();
  }

  @Override
  protected BsTestNatural wrapData(TestNatural dto) {
    return new BsTestNatural(dto);
  }

  @Override
  protected String getClassName() {
    return TestNatural.class.getName();
  }
}
