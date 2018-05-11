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
package autohit.common.traps;

import org.apache.commons.logging.Log;

import autohit.common.AutohitLogInjectorWrapper;

/**
 * Traps Commons logging.  You must call the static method
 * setTrap() at least once per JVM to get this working.
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 19Jul03</code> 
 * 
 */
public final class CommonsLoggerTrap implements Log {

	/**
	 * Remember where out root logger is.  
	 */
	static private AutohitLogInjectorWrapper log;

	/**
	 * Me  
	 */
	private String  me;


	/**
	 * Constructor
	 * @param name Name of the logger to be constructed
	 */
	public CommonsLoggerTrap(String name) {
		me = name;
	}

	/**
	 * Log a message with debug log level.
	 * @param logger a logger in which to dump everything.
	 */
	public static void setTrap(AutohitLogInjectorWrapper logger) {
		System.setProperty(
			"org.apache.commons.logging.Log",
			"autohit.common.traps.CommonsLoggerTrap");
		log = logger;
	}

	/**
	 * Log a message with debug log level.
	 */
	public void debug(Object message) {
		//if (log.debugState())
		//	log.debug(me + " : " + String.valueOf(message));
	}

	/**
	 * Log a message and exception with debug log level.
	 */
	public void debug(Object message, Throwable exception) {
		//if (log.debugState())
		//	log.debug(me + " : " + String.valueOf(message) + " : " + exception.getMessage());
	}

	/**
	 * Log a message with error log level.
	 */
	public void error(Object message) {
		log.error(me + " : " + String.valueOf(message));
	}

	/**
	 * Log a message and exception with error log level.
	 */
	public void error(Object message, Throwable exception) {
		log.error(me + " : " + String.valueOf(message) + " : " + exception.getMessage());
	}

	/**
	 * Log a message with fatal log level.
	 */
	public void fatal(Object message) {
		log.error(me + " : " + String.valueOf(message));
	}

	/**
	 * Log a message and exception with fatal log level.
	 */
	public void fatal(Object message, Throwable exception) {
		log.error(me + " : " + String.valueOf(message) + " : " + exception.getMessage());
	}

	/**
	 * Log a message with info log level.
	 */
	public void info(Object message) {
		//log.info(me + " : " + String.valueOf(message));
	}

	/**
	 * Log a message and exception with info log level.
	 */
	public void info(Object message, Throwable exception) {
		//log.info(me + " : " + String.valueOf(message) + " : " + exception.getMessage());
	}

	/**
	 * Log a message with trace log level.
	 */
	public void trace(Object message) {
		//if (log.debugState())
			//log.debug(String.valueOf(message));
	}

	/**
	 * Log a message and exception with trace log level.
	 */
	public void trace(Object message, Throwable exception) {
		//if (log.debugState())
			//log.debug(String.valueOf(message) + " : " + exception.getMessage());
	}

	/**
	 * Log a message with warn log level.
	 */
	public void warn(Object message) {
		//log.info(me + " : " + String.valueOf(message));
	}

	/**
	 * Log a message and exception with warn log level.
	 */
	public void warn(Object message, Throwable exception) {
		//log.info(me + " : " + String.valueOf(message) + " : " + exception.getMessage());
	}

	/**
	 * Is debug logging currently enabled?
	 */
	public boolean isDebugEnabled() {
		return (log.debugState());
	}

	/**
	 * Is error logging currently enabled?
	 */
	public boolean isErrorEnabled() {
		return (true);
	}

	/**
	 * Is fatal logging currently enabled?
	 */
	public boolean isFatalEnabled() {
		return (true);
	}

	/**
	 * Is info logging currently enabled?
	 */
	public boolean isInfoEnabled() {
		return (true);
	}

	/**
	 * Is tace logging currently enabled?
	 */
	public boolean isTraceEnabled() {
		return (log.debugState());
	}

	/**
	 * Is warning logging currently enabled?
	 */
	public boolean isWarnEnabled() {
		return (true);
	}

}
