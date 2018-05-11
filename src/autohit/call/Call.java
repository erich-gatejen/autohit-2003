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

import autohit.server.SystemContext;
import autohit.universe.Universe;
import autohit.vm.VMCore;
import autohit.common.AutohitLogInjectorWrapper;

/**
 * The abstract class to all the callable functions. Every CALL should
 * implement this. The call will get passed parameters by name out of core.
 * IMPORTANT!!!! Calls should not have any fields! Those that are already
 * provided are inherently thread safe. Calls are cached per VM and reused as
 * often as possible. There will be no thread-safety issues with the VMCore or
 * log, but the SystemContecxt and Universe may be shared.
 * 
 * @author Erich P. Gatejen
 * @version 1.1 <i>Version History</i><code>EPG - Initial - 14May03<br>
 * EPG - reorganize to make Call the base class - 10Sep03</code>
 */
public abstract class Call {

	public final static String CALL_TEXT_HEADER = "call:";

	/**
	 * Core */
	public VMCore vmc;

	/**
	 * System Context */
	public SystemContext sc;

	/**
	 * Primary Logger */
	public AutohitLogInjectorWrapper log;

	/**
	 * Our simple little universe. */
	public Universe u;

	/**
	 * Implement this to handle load time initialization. The four main fields
	 * will already be set--vmc, sc, log, and u. You must implement this, but
	 * you don't have to do anything. Remember that calls are cached per VM and
	 * reused as often as possible. There will be no thread-safety issues with
	 * the VMCore or log, but the SystemContecxt and Universe may be shared.
	 * @throws CallException
	 */
	public abstract void load_chain() throws CallException;

	/**
	 * Implement this to return the name of the CALL
	 * @return name of the CALL
	 */
	public abstract String name();

	/**
	 * Execute it.
	 * 
	 * @return the result or null if there is no result
	 */
	public abstract String call() throws CallException;

	/**
	 * Execute using the passed universe, rather than the loaded.
	 * @param uni
	 *           a universe
	 * @return the result or null if there is no result
	 * @see autohit.universe.Universe
	 */
	public abstract String call(Universe uni) throws CallException;

	// METHODS

	/**
	 * This will be called to set references to the environment and usable Universe.
	 * 
	 * @param core
	 *           is a reference to the environment core
	 * @param sctx
	 *           is a system context
	 * @param logger
	 *           the log target
	 * @see autohit.vm.VMCore
	 * @see autohit.server.SystemContext
	 */
	public void load(VMCore core, SystemContext sctx, AutohitLogInjectorWrapper logger) throws CallException {
		log = logger;
		vmc = core;
		sc = sctx;
		u = sc.getUniverse();
		this.load_chain();
	}

	// SERVICES

	/**
	 * Log a debugging statement.
	 * 
	 * @param text
	 *           The text of the statement.
	 */
	public void debug(String text) {
		if (log.debugState())
			log.debug(CALL_TEXT_HEADER + this.name() + ':' + text, CallException.CODE_DEBUGGING_CALLS);
	}

	/**
	 * Log an error statement.
	 * 
	 * @param text
	 *           The text of the statement.
	 */
	public void error(String text) {
		log.error(CALL_TEXT_HEADER + this.name() + ':' + text, CallException.CODE_CALL_ERROR);
	}

	/**
	 * Log an info statement.
	 * 
	 * @param text
	 *           The text of the statement.
	 */
	public void info(String text) {
		log.error(CALL_TEXT_HEADER + this.name() + ':' + text, CallException.CODE_INFORMATIONAL_OK_VERBOSE);
	}

	/**
	 * Return a formatted text. Useful for making Exception messages.
	 * 
	 * @param text
	 *           The text of the statement.
	 * @return the formatted text.
	 */
	public String format(String text) {
		return CALL_TEXT_HEADER + this.name() + ':' + text;
	}

	/**
	 * Get a desired parameter.  If it is not found, it will post an error and return null.
	 * 
	 * @param item
	 *           Name of the parameter
	 * @return the object or null
	 */
	public Object desired(String item) {
		Object thang = null;
		try {
			thang = (String) vmc.fetch(item);
		} catch (Exception e) {
			// dont care. null will cause error
		}
		if (thang == null) {
			this.debug("Parameter " + item + " not given.");
		}
		return thang;
	}

	/**
	 * Get a desired parameter. If it is not found or is not a String, it will post an error and return null.
	 * 
	 * @param item
	 *           Name of the parameter
	 * @return the string
	 */
	public String desiredString(String item) {
		Object thang = null;
		try {
			thang = (String) vmc.fetch(item);
		} catch (Exception e) {
			// dont care. null will cause error
		}
		if (thang == null) {
			this.error("Parameter " + item + " not given.");
		} else if (!(thang instanceof String)) {
			this.error("Parameter " + item + " found but not a String.");
			thang = null;
		}
		return (String) thang;
	}

	/**
	 * Get a optional parameter. If it is found but is not a String, it will post an error and return null. If it is not found, it will just return null, without and error.
	 * 
	 * @param item
	 *           Name of the parameter
	 * @return the string
	 */
	public String optionalString(String item) {
		Object thang = null;
		try {
			thang = (String) vmc.fetch(item);
		} catch (Exception e) {
			// dont care. null will cause error
		}
		if ((thang != null) && !(thang instanceof String)) {
			this.error("Parameter " + item + " found but not a String.");
			thang = null;
		}
		return (String) thang;
	}

	/**
	 * Get a required parameter.  If it is not found, it will throw an exception.
	 * 
	 * @param item
	 *           Name of the parameter
	 * @return the object
	 * @throws CallException
	 */
	public Object required(String item) throws CallException {
		Object thang = null;
		try {
			thang = (String) vmc.fetch(item);
		} catch (Exception e) {
			// the null will express this
		}
		if (thang == null) {
			throw new CallException(
				this.format("Paramter " + item + " not given."),
				CallException.CODE_CALL_REQUIRED_PARAM_MISSING_FAULT);
		}
		return thang;
	}

	/**
	 * Get a required parameter that must be a String. If it is not found or it is not a String, it will throw an exception.
	 * 
	 * @param item
	 *           Name of the parameter
	 * @return the string
	 * @throws CallException
	 */
	public String requiredString(String item) throws CallException {
		return (String) this.required(item, String.class);
	}

	/**
	 * Get a required parameter.  If it is not found, it will throw an exception. It will also make sure it is a certain class type. If not, it will throw an exception.
	 * 
	 * @param item
	 *           Name of the parameter
	 * @param classtype
	 *           Class is should be.
	 * @return the object
	 * @throws CallException
	 */
	public Object required(String item, Class classtype) throws CallException {
		Object thing = this.required(item);
		if (!(classtype.isInstance(thing))) {
			throw new CallException(
				this.format("Parameter " + item + " not required type.  required=" + classtype.getName()),
				CallException.CODE_CALL_REQUIRED_PARAM_CLASSMISMATCH_FAULT);
		}
		return thing;
	}

	/**
	 * Get a required persist object. If it is not found, it will throw an exception. It will also make sure it is a certain class type. If not, it will throw an exception.
	 * @param item
	 *           Name of the parameter
	 * @param classtype
	 *           Class is should be.
	 * @return the object
	 * @throws CallException
	 */
	public Object requiredPersist(String item, Class classtype) throws CallException {

		Object thing = null;

		try {
			thing = (Object) vmc.get(item);
			this.debug("Persist object named " + item + "found.");
		} catch (Exception iii) {
			// the null will express this.
		}

		// See if we got it and it is ok.
		if (thing == null) {
			throw new CallException(
				this.format("Object named " + item + "does not exist in persist."),
				CallException.CODE_CALL_PERSISTNOTFOUND_FAULT);

		} else if (!(classtype.isInstance(thing))) {
			throw new CallException(
				this.format("Persist object named " + item + "found but it is not the right type."),
				CallException.CODE_CALL_PERSISTMISMATCH_FAULT);
		}
		return thing;
	}

}
