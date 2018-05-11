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
package autohit.call;

import autohit.common.AutohitException;

/**
 * A CALL exception.  The specific error is given in the numeric
 * field.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 14May03<br>
 * EPG - Add exception chaining - 13Jun03</code> 
 * 
 */
public class CallException extends AutohitException {

	final static long serialVersionUID = 1;
	
	/**
	 *  Default Constructor.
	 */
	public CallException() {
		super("Generic CallException", CODE_CALL_ERROR);
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 */
	public CallException(String message) {
		super(message, CODE_CALL_ERROR);
	}

	/**
	 *  Message constructor
	 * @param n numeric error
	 */
	public CallException(int n) {
		super("Numbered CallException", n);
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 * @param n numeric error
	 */
	public CallException(String message, int n) {
		super(message, n);
	}

	/**
	 *  Message constructor with cause
	 * @param message text message for exception
	 * @param theCause for exception chaining
	 */
	public CallException(String message, Throwable theCause) {
		super(message, CODE_CALL_ERROR);
	}

	/**
	 *  Message constructor with cause
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public CallException(int n, Throwable theCause) {
		super("Numbered CallException", n);
	}

	/**
	 *  Message constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public CallException(String message, int n, Throwable theCause) {
		super(message, n, theCause);
		numeric = n;
	}

}
