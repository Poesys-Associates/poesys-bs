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

import com.poesys.bs.dto.BsTestNatural;
import com.poesys.bs.dto.IDto;
import com.poesys.db.col.IColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.pk.IPrimaryKey;
import com.poesys.db.pk.NaturalPrimaryKey;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * CUT: AbstractDataDelegate subclassed by TestNaturalDelegate
 *
 * @author Robert J. Muller
 */
public class TestNaturalDelegateTest extends ConnectionTest {
  private static final Logger logger = Logger.getLogger(TestNaturalDelegateTest.class);

  private static final TestNaturalDelegate DELEGATE = new TestNaturalDelegate();
  private static final BigDecimal N1 = new BigDecimal("1.234");
  private static final BigDecimal N2 = new BigDecimal("2.3456");
  private static final BigDecimal N3 = new BigDecimal("2.3456");
  private static final NaturalPrimaryKey KEY1 = createKey("a", "b");
  private static final NaturalPrimaryKey KEY2 = createKey("b", "c");
  private static final NaturalPrimaryKey KEY3 = createKey("c", "d");
  private static final NaturalPrimaryKey KEY4 = createKey("z", "z");

  /**
   * Create a Natural Primary Key using two input strings. This static method
   * lets you create keys to use in testing, reducing code duplication.
   *
   * @param key1 the first part of the key
   * @param key2 the second part of the key
   * @return the key
   */
  private static NaturalPrimaryKey createKey(String key1, String key2) {
    List<IColumnValue> keyList = new ArrayList<>(2);
    keyList.add(new StringColumnValue("key1", key1));
    keyList.add(new StringColumnValue("key2", key2));
    return new NaturalPrimaryKey(keyList, "com.poesys.db.dto.TestNatural");
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#insert(java.util.List)}.
   */
  @Test
  public void testInsert() {
    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException | IOException e) {
      logger.error("Error", e);
    }
    finally {
      if (conn != null) {
        try {
          logger.debug("Closing connection " + conn.hashCode());
          conn.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Build 3 test natural key objects to insert.
    BsTestNatural test1 = new BsTestNatural("a", "b", N1);
    BsTestNatural test2 = new BsTestNatural("b", "c", N2);
    BsTestNatural test3 = new BsTestNatural("c", "d", N3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    DELEGATE.insert(list);
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#getObject(IPrimaryKey)}
   * .
   */
  @Test
  public void testGetObject() {
    BsTestNatural newObject = new BsTestNatural("b", "c", N2);

    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException | IOException e) {
      logger.error("Error", e);
    }
    finally {
      if (conn != null) {
        try {
          logger.debug("Closing connection " + conn.hashCode());
          conn.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<>(3);
    list.add(newObject);
    DELEGATE.process(list);

    BsTestNatural object = DELEGATE.getObject(KEY2);
    assertTrue("No object retrieved", object != null);
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#getAllObjects(int)}.
   */
  @Test
  public void testGetAllObjects() {
    BsTestNatural newObject = new BsTestNatural("b", "c", N2);

    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException | IOException e) {
      logger.error("Error", e);
    }
    finally {
      if (conn != null) {
        try {
          logger.debug("Closing connection " + conn.hashCode());
          conn.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<>(3);
    list.add(newObject);
    DELEGATE.process(list);
    List<BsTestNatural> list2 = DELEGATE.getAllObjects(2);
    assertTrue("No list of objects retrieved", list2 != null);
    for (BsTestNatural o : list2) {
      logger.info("Found object " + o.getPrimaryKey().getStringKey());
    }
    assertTrue("List of objects has no objects", list.size() > 0);
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#update(IDto)}.
   */
  @Test
  public void testUpdate() {
    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
    } catch (SQLException | IOException e) {
      logger.error("Error", e);
    }
    finally {
      if (conn != null) {
        try {
          logger.debug("Closing connection " + conn.hashCode());
          conn.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Build 3 test natural key objects to insert.
    BsTestNatural test1 = new BsTestNatural("a", "b", N1);
    BsTestNatural test2 = new BsTestNatural("b", "c", N2);
    BsTestNatural test3 = new BsTestNatural("c", "d", N3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    DELEGATE.insert(list);

    // Update the value of the a-b object to n2.
    test1.setCol1(N2);
    // Update the object.
    DELEGATE.update(test1);
    // Re-query the object.
    BsTestNatural test1a = DELEGATE.getDatabaseObject(KEY1);
    // Test the value with compareTo because of precision issues.
    assertTrue("Couldn't re-query object", test1a != null);
    assertTrue("Object value " + test1.getCol1() + " not the same as updated value " + N2,
               N2.compareTo(test1.getCol1()) == 0);
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#process(java.util.List)}.
   */
  @Test
  public void testProcess() {
    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException | IOException e) {
      logger.error("Error", e);
    }
    finally {
      if (conn != null) {
        try {
          logger.debug("Closing connection " + conn.hashCode());
          conn.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Build 3 test natural key objects to insert.
    BsTestNatural test1 = new BsTestNatural("a", "b", N1);
    BsTestNatural test2 = new BsTestNatural("b", "c", N2);
    BsTestNatural test3 = new BsTestNatural("c", "d", N3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    DELEGATE.insert(list);

    // Create an object to insert.
    BsTestNatural testInsert = new BsTestNatural("z", "z", N1);
    // Update the value of the b-c object to n1.
    test2.setCol1(N1);
    // Mark c-d object deleted.
    test3.delete();

    // Build a list of the objects and process them.
    list = new ArrayList<>(3);
    list.add(testInsert);
    list.add(test2);
    list.add(test3);

    DELEGATE.process(list);

    // Check the results.
    BsTestNatural test1a = DELEGATE.getObject(KEY4);
    assertTrue("Couldn't find inserted z-z object", test1a != null);
    BsTestNatural test2a = DELEGATE.getObject(KEY2);
    assertTrue("Couldn't query updated object", test2a != null);
    assertTrue("Object value " + test2a.getCol1() + " not the same as updated value " + N1,
               N1.compareTo(test2a.getCol1()) == 0);
    BsTestNatural test3a = DELEGATE.getObject(KEY3);
    assertTrue("Found deleted object", test3a == null);
  }

  /**
   * Test the delegate delete() method--
   * {@link TestNaturalDelegate#delete(IDto)}
   */
  @Test
  public void testDelete() {
    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException | IOException e) {
      logger.error("Error", e);
    }
    finally {
      if (conn != null) {
        try {
          logger.debug("Closing connection " + conn.hashCode());
          conn.close();
        } catch (SQLException e) {
          // ignore
        }
      }
    }

    // Build 3 test natural key objects to insert.
    BsTestNatural test1 = new BsTestNatural("a", "b", N1);
    BsTestNatural test2 = new BsTestNatural("b", "c", N2);
    BsTestNatural test3 = new BsTestNatural("c", "d", N3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    DELEGATE.insert(list);

    // Delete the a-b object.
    test1.delete();
    DELEGATE.delete(test1);
    // Re-query the object.
    BsTestNatural test1a = DELEGATE.getDatabaseObject(KEY1);
    // Test the value with compareTo because of precision issues.
    assertTrue("Object found in database, not deleted", test1a == null);
  }
}
