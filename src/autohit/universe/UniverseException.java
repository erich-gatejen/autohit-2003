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
package autohit.universe;

import autohit.common.AutohitException;

/**
 * A Universe exception.  A numeric error number can be set.
 * The default is UE_GENERIC.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 16Apr03<br>
 * EPG - exception chaining - 13Jun03</code> 
 * 
 */
public class UniverseException extends AutohitException {

	final static long serialVersionUID = 1;
	
	/**
	 * UNIVERSE NUMERICS.  ALL are ERRORS
	 */
	public final static int UE_DEFAULT = CODE_UNIVERSE_ERROR;
	public final static int UE_IO_ERROR = CODE_UNIVERSE_ERROR + 1;
	public final static int UE_OBJECT_DOESNT_EXIST = CODE_UNIVERSE_ERROR + 2;
	public final static int UE_CANNOT_STREAM = CODE_UNIVERSE_ERROR + 3;
	public final static int UE_CORRUPT_UNIVERSE = CODE_UNIVERSE_ERROR + 4;
	public final static int UE_CORRUPT_OBJECT = CODE_UNIVERSE_ERROR + 5;
	public final static int UE_HANDLER_ERROR = CODE_UNIVERSE_ERROR + 6;
	public final static int UE_MALFORMED_REFERENCE = CODE_UNIVERSE_ERROR + 7;
	public final static int UE_UNIVERSE_DOESNT_EXIST = CODE_UNIVERSE_ERROR + 8;
	public final static int UE_NAMED_UNIVERSE_SERVICE_DOESNT_EXIST =
		CODE_UNIVERSE_ERROR + 9;
	public final static int UE_REQUIRED_PROPERTY_MISSING =
		CODE_UNIVERSE_ERROR + 10;
	public final static int UE_NOT_SUPPORTED = CODE_UNIVERSE_ERROR + 11;
	public final static int UE_OBJECT_LOCKED = CODE_UNIVERSE_ERROR + 12;
	public final static int UE_DONT_OWN_THE_LOCK = CODE_UNIVERSE_ERROR + 13;
	public final static int UE_TOP = TOP_CODE_UNIVERSE_ERROR;

	/**
	 *  Default Constructor.
	 */
	public UniverseException() {
		super("Generic UniverseException", UE_DEFAULT);
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 */
	public UniverseException(String message) {
		super(message, UE_DEFAULT);
	}

	/**
	 *  Message constructor
	 * @param n numeric error
	 */
	public UniverseException(int n) {
		super("Generic UniverseException", n);
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 * @param n numeric error
	 */
	public UniverseException(String message, int n) {
		super(message, n);
	}

	/**
	 *  Message constructor with cause
	 * @param message text message for exception
	 * @param theCause for exception chaining
	 */
	public UniverseException(String message, Throwable theCause) {
		super(message, UE_DEFAULT);
	}

	/**
	 *  Message constructor with cause
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public UniverseException(int n, Throwable theCause) {
		super("Generic UniverseException", n);
	}

	/**
	 *  Message constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public UniverseException(String message, int n, Throwable theCause) {
		super(message, n, theCause);
		numeric = n;
	}

}
