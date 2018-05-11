/**
 * AUTOHIT 2003
 * Copyright Erich P Gatejen (c) 1989,1997,2003,2004
 * 
 * This program is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Additional license information can be found in the documentation.
 * @author Erich P Gatejen
 */

package autohit.system.tellio;

import autohit.common.AutohitException;

/**
 * A n exception.  The specific error is given in the numeric
 * field.  
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 4Jan03</code> 
 * 
 */
public class TellException extends AutohitException {

	final static long serialVersionUID = 1;
	
	/**
	 *  Numeric values for the exception.
	 */

	/**
	 *  Default Constructor.
	 */
	public TellException() {
		super("Generic TellException", CODE_SYSTEM_GENERIC_ERROR);
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 */
	public TellException(String message) {
		super(message, CODE_SYSTEM_GENERIC_ERROR);
	}

	/**
	 *  Message constructor
	 * @param n numeric error
	 */
	public TellException(int n) {
		super("Generic TellException", n);
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 * @param n numeric error
	 */
	public TellException(String message, int n) {
		super(message, n);
	}

	/**
	 *  Message constructor with cause
	 * @param message text message for exception
	 * @param theCause for exception chaining
	 */
	public TellException(String message, Throwable theCause) {
		super(message, CODE_SYSTEM_GENERIC_ERROR);
	}

	/**
	 *  Message constructor with cause
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public TellException(int n, Throwable theCause) {
		super("Generic TellException", n);
	}

	/**
	 *  Message constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public TellException(String message, int n, Throwable theCause) {
		super(message, n, theCause);
		numeric = n;
	}
}
