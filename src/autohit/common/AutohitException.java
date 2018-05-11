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
package autohit.common;

/**
 * A base exception for Autohit.  The specific error is given in the numeric
 * field.  All default exceptions are assumed to be ERRORS.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 14May03<br>
 * EPG - Added chaining support - 11Jun03<br>
 * EPG - Chenge to new error code scheme - 19Jul03</code> 
 * 
 */
public class AutohitException extends Exception implements AutohitErrorCodes {
	
	final static long serialVersionUID = 1;

	/**
	 * Exception numeric
	 */
	public int numeric;

	/**
	 *  Numeric values for the exception.
	 */
	public static final int AUTOHIT_EXCEPTION_GENERIC = CODE_DEFAULT_ERROR;

	/**
	 *  Default Constructor.
	 */
	public AutohitException() {
		super("Messageless AutohitException");
		numeric = AUTOHIT_EXCEPTION_GENERIC;
	}

	/**
	 *  Default Constructor with Cause
	 * @param theCause for exception chaining
	 */
	public AutohitException(Throwable theCause) {
		super("Messageless  AutohitException", theCause);
		numeric = AUTOHIT_EXCEPTION_GENERIC;
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 */
	public AutohitException(String message) {
		super(message);
		numeric = AUTOHIT_EXCEPTION_GENERIC;
	}

	/**
	 *  Message constructor with Cause
	 * @param message text message for exception
	 * @param theCause for exception chaining
	 */
	public AutohitException(String message, Throwable theCause) {
		super(message, theCause);
		numeric = AUTOHIT_EXCEPTION_GENERIC;
	}

	/**
	 *  Message constructor
	 * @param n numeric error
	 */
	public AutohitException(int n) {
		super("Numbered AutohitException =" + n);
		numeric = n;
	}

	/**
	 *  Message constructor with cause
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public AutohitException(int n, Throwable theCause) {
		super("Numbered AutohitException =" + n, theCause);
		numeric = n;
	}

	/**
	 *  Message constructor
	 * @param message text message for exception
	 * @param n numeric error
	 */
	public AutohitException(String message, int n) {
		super(message);
		numeric = n;
	}

	/**
	 *  Message constructor with cause
	 * @param message text message for exception
	 * @param n numeric error
	 * @param theCause for exception chaining
	 */
	public AutohitException(String message, int n, Throwable theCause) {
		super(message, theCause);
		numeric = n;
	}

	/**
	 *  Helper for determining level - Informational
	 * @param code numeric code
	 * @return true if it is informational
	 */
	static public boolean isInformational(int code) {
		if ((code >= FLOOR_NUMERIC) && (code < CODE_DEFAULT_WARNING))
			return true;
		else
			return false;
	}

	/**
	 *  Helper for determining level - Warning
	 * @param code numeric code
	* @return true if it is a warning
	 */
	static public boolean isWarning(int code) {
		if ((code >= CODE_DEFAULT_WARNING) && (code < CODE_DEFAULT_ERROR))
			return true;
		else
			return false;
	}

	/**
	 *  Helper for determining level - Error
	 * @param code numeric code
	* @return true if it is an error
	 */
	static public boolean isError(int code) {
		if ((code >= CODE_DEFAULT_ERROR) && (code < CODE_DEFAULT_FAULT))
			return true;
		else
			return false;
	}

	/**
	 *  Helper for determining level - Fault
	 * @param code numeric code
	* @return true if it is a fault
	 */
	static public boolean isFault(int code) {
		if ((code >= CODE_DEFAULT_FAULT) && (code < CODE_DEFAULT_PANIC))
			return true;
		else
			return false;
	}

	/**
	 *  Helper for determining level - Panic
	 * @param code numeric code
	* @return true if it is a panic
	 */
	static public boolean isPanic(int code) {
		if ((code >= CODE_DEFAULT_PANIC) && (code < TOP_NUMERIC))
			return true;
		else
			return false;
	}
}
