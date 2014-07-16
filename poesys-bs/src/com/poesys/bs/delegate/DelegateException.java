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

/**
 * Standard exception for the business layer. Business delegates throw this
 * exception with error message strings that refer to the appropriate elements
 * of an internationalized resource bundle and nested exceptions that show the
 * originating exception and source.
 * 
 * @author Robert J. Muller
 */
public class DelegateException extends RuntimeException {
  /** The unique UID for this serializable object */
  private static final long serialVersionUID = 3475974859548964859L;

  /**
   * Create an exception with a message.
   * 
   * @param arg0 the exception message
   */
  public DelegateException(String arg0) {
    super(arg0);
  }

  /**
   * Create an exception with a message and a nested exception
   * 
   * @param arg0 the message
   * @param arg1 the nested exception
   */
  public DelegateException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

  /**
   * Create an exception with a nested exception.
   * 
   * @param arg0 the nested exception
   */
  public DelegateException(Throwable arg0) {
    super(arg0);
  }
}
