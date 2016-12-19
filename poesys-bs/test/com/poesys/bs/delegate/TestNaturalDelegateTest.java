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
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.poesys.bs.dto.BsTestNatural;
import com.poesys.db.col.AbstractColumnValue;
import com.poesys.db.col.StringColumnValue;
import com.poesys.db.dao.ConnectionTest;
import com.poesys.db.pk.NaturalPrimaryKey;


/**
 * CUT: AbstractDataDelegate subclassed by TestNaturalDelegate
 * 
 * @author Robert J. Muller
 */
public class TestNaturalDelegateTest extends ConnectionTest {
  private static final Logger logger =
    Logger.getLogger(TestNaturalDelegateTest.class);

  private static final TestNaturalDelegate del = new TestNaturalDelegate();
  private static final BigDecimal n1 = new BigDecimal("1.234");
  private static final BigDecimal n2 = new BigDecimal("2.3456");
  private static final BigDecimal n3 = new BigDecimal("2.3456");
  private static final NaturalPrimaryKey key_a_b = createKey("a", "b");
  private static final NaturalPrimaryKey key_b_c = createKey("b", "c");
  private static final NaturalPrimaryKey key_z_z = createKey("z", "z");

  /**
   * Create a Natural Primary Key using two input strings. This static method
   * lets you create keys to use in testing, reducing code duplication.
   * 
   * @param key1 the first part of the key
   * @param key2 the second part of the key
   * @return the key
   */
  private static NaturalPrimaryKey createKey(String key1, String key2) {
    List<AbstractColumnValue> keyList = new ArrayList<AbstractColumnValue>(2);
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
    } catch (SQLException e) {
      logger.error("Error", e);
    } catch (IOException e) {
      logger.error("Error", e);
    } finally {
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
    BsTestNatural test1 = new BsTestNatural("a", "b", n1);
    BsTestNatural test2 = new BsTestNatural("b", "c", n2);
    BsTestNatural test3 = new BsTestNatural("c", "d", n3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<BsTestNatural>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    del.insert(list);
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#getObject(NaturalPrimaryKey)}
   * .
   */
  @Test
  public void testGetObject() {
    BsTestNatural newObject = new BsTestNatural("b", "c", n2);

    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException e) {
      logger.error("Error", e);
    } catch (IOException e) {
      logger.error("Error", e);
    } finally {
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
    List<BsTestNatural> list = new ArrayList<BsTestNatural>(3);
    list.add(newObject);
    del.process(list);
    
    BsTestNatural object = del.getObject(key_b_c);
    assertTrue("No object retrieved", object != null);
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#getAllObjects(int)}.
   */
  @Test
  public void testGetAllObjects() {
    BsTestNatural newObject = new BsTestNatural("b", "c", n2);

    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException e) {
      logger.error("Error", e);
    } catch (IOException e) {
      logger.error("Error", e);
    } finally {
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
    List<BsTestNatural> list = new ArrayList<BsTestNatural>(3);
    list.add(newObject);
    del.process(list);
    List<BsTestNatural> list2 = del.getAllObjects(2);
    assertTrue("No list of objects retrieved", list2 != null);
    for (BsTestNatural o : list2) {
      logger.info("Found object " + o.getPrimaryKey().getStringKey());
    }
    assertTrue("List of objects has no objects", list.size() > 0);
  }

  /**
   * Test method for
   * {@link com.poesys.bs.delegate.TestNaturalDelegate#update(BsTestNatural)}.
   */
  @Test
  public void testUpdate() {
    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
    } catch (SQLException e) {
      logger.error("Error", e);
    } catch (IOException e) {
      logger.error("Error", e);
    } finally {
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
    BsTestNatural test1 = new BsTestNatural("a", "b", n1);
    BsTestNatural test2 = new BsTestNatural("b", "c", n2);
    BsTestNatural test3 = new BsTestNatural("c", "d", n3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<BsTestNatural>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    del.insert(list);

    // Update the value of the a-b object to n2.
    test1.setCol1(n2);
    // Update the object.
    del.update(test1);
    // Requery the object.
    BsTestNatural test1a = del.getDatabaseObject(key_a_b);
    // Test the value with compareTo because of precision issues.
    assertTrue("Couldn't requery object", test1a != null);
    assertTrue("Object value " + test1.getCol1()
                   + " not the same as updated value " + n2,
               n2.compareTo(test1.getCol1()) == 0);
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
    } catch (SQLException e) {
      logger.error("Error", e);
    } catch (IOException e) {
      logger.error("Error", e);
    } finally {
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
    BsTestNatural test1 = new BsTestNatural("a", "b", n1);
    BsTestNatural test2 = new BsTestNatural("b", "c", n2);
    BsTestNatural test3 = new BsTestNatural("c", "d", n3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<BsTestNatural>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    del.insert(list);

    // Create an object to insert.
    BsTestNatural testInsert = new BsTestNatural("z", "z", n1);
    // Update the value of the b-c object to n1.
    test2.setCol1(n1);
    // Mark c-d object deleted.
    test3.delete();

    // Build a list of the objects and process them.
    list = new ArrayList<BsTestNatural>(3);
    list.add(testInsert);
    list.add(test2);
    list.add(test3);

    del.process(list);

    // Check the results.
    BsTestNatural test1a = del.getObject(key_z_z);
    assertTrue("Couldn't find inserted z-z object", test1a != null);
    BsTestNatural test2a = del.getObject(key_b_c);
    assertTrue("Couldn't query updated object", test2a != null);
    assertTrue("Couldn't requery object", test2a != null);
    assertTrue("Object value " + test2a.getCol1()
                   + " not the same as updated value " + n1,
               n1.compareTo(test2a.getCol1()) == 0);
    BsTestNatural test3a = del.getObject(key_a_b);
    assertTrue("Found deleted object", test3a == null);
  }

  /**
   * Test the delegate delete() method--
   * {@link TestNaturalDelegate#delete(BsTestNatural)}
   */
  @Test
  public void testDelete() {
    Connection conn = null;
    try {
      conn = getConnection();
      Statement stmt = conn.createStatement();
      stmt.execute("TRUNCATE TABLE TestNatural");
      conn.commit();
    } catch (SQLException e) {
      logger.error("Error", e);
    } catch (IOException e) {
      logger.error("Error", e);
    } finally {
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
    BsTestNatural test1 = new BsTestNatural("a", "b", n1);
    BsTestNatural test2 = new BsTestNatural("b", "c", n2);
    BsTestNatural test3 = new BsTestNatural("c", "d", n3);

    // Build the list and insert it.
    List<BsTestNatural> list = new ArrayList<BsTestNatural>(3);
    list.add(test1);
    list.add(test2);
    list.add(test3);
    del.insert(list);

    // Delete the a-b object.
    del.delete(test1);
    // Requery the object.
    BsTestNatural test1a = del.getDatabaseObject(key_a_b);
    // Test the value with compareTo because of precision issues.
    assertTrue("Object found in database, not deleted", test1a == null);
  }
}
