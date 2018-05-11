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
package autohit.call.modules;

import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.universe.Universe;
import autohit.vm.VMCore;
import autohit.call.CallException;
import autohit.server.SystemContext;

/**
 * The abstract base class for modules.  Every module must implement this.
 * <p>
 * An implemented module needs to complete the following abstract methods:
 * execute_chain - run a named method.<br>
 * instantiation_chain() - called at module instantiation<br>
 * free_chain() - called at module destruction<br>
 * <p>
 * All protected methods are helpers for the execute.
 * <p>
 * Modules are not allowed to have methods called "name"
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 14Jun03<br>
 * EPG - make SC visible - 3 Sep03</code>
 * 
 */
public abstract class Module {

	// Services visible to the implementor
	protected VMCore visCore;
	protected Universe visUniverse;
	protected AutohitLogInjectorWrapper visLogger;
	protected String myName = "GenericModule";
	protected SystemContext visSC;

	/**
	 * Instantiate
	 * @param core is a reference to the environment core
	 * @param uni is the default universe
	 * @param logger is the default logger
	 * @see autohit.vm.VMCore
	 * @see autohit.universe.Universe
	 * @see autohit.common.AutohitLogInjectorWrapper
	 * @throws CallException
	 */
	public void instance(VMCore core, Universe uni, AutohitLogInjectorWrapper logger, SystemContext  sctx)
		throws CallException {
		visCore = core;
		visUniverse = uni;
		visLogger = logger;
		visSC = sctx;

		logger.debug(
			"MODULE: invoke instance.",
			CallException.CODE_DEBUGGING_CALLS);

		try {
			myName = this.instantiation_chain();
		} catch (CallException e) {
			throw e;
		} catch (Exception ex) {
			// Catch any wildassed exceptions
			throw new CallException(
				"MODULE: instantiate module has a runaway exception.  The module is invalid.  exception="
					+ ex.getMessage(),
				CallException.CODE_CALL_INTENTIONAL_FAULT);
		}
	}

	/**
	 * Remove an instance
	 * @throws CallException
	 */
	public void free() throws CallException {

		// nothing else to do right now
		try {
			this.free_chain();
		} catch (CallException e) {
			throw e;
		} catch (Exception ex) {
			// Catch any wildassed exceptions
			throw new CallException(
				"module:" + myName + ":free module has a runaway exception.  The module is invalid.  exception="
					+ ex.getMessage(),
				CallException.CODE_CALL_INTENTIONAL_FAULT);
		}
	}

	/**
	 * Execute a method
	 * @param methodName the name of the method
	 * @return any resultant object String
	 * @throws CallException
	 */
	public String execute(String methodName) throws CallException {

		Object thang = null;

		// Abstracting for future use.
		try {
			thang = this.execute_chain(methodName);
		} catch (CallException e) {
			throw e;
		} catch (Exception ex) {
			// Catch any wildassed exceptions
			throw new CallException(
				"module:" + myName + ":method " + methodName + " had a FAULT.  message="
					+ ex.getMessage(),
				CallException.CODE_CALL_INTENTIONAL_FAULT);
		}

		if (!(thang instanceof String)) {
			throw new CallException(
				"module:" + myName + ":method " + methodName + " returned something other than String.",
				CallException.CODE_CALL_INTENTIONAL_FAULT);
		}
		return (String) thang;
	}

	// HELPERS TO THE SUBCLASS

	/**
	 * report if we are debugging (as an accellerator)
	 * @return parameter value or null if not found
	 */
	protected boolean isDebugging() {
		return visLogger.debugState();
	}

	/**
	 * Get a parameter
	 * @param name of the parameter
	 * @return parameter value or null if not found
	 */
	protected Object getParam(String name) {
		Object thang = null;

		try {

			if (visCore.exists(name))
				thang = visCore.fetch(name);

		} catch (Exception e) {
			// it's already null
		}
		return thang;
	}

	/**
	 * Get a persisted object
	 * @param name of the object in the persist
	 * @return parameter value or null if not found
	 */
	protected Object getPersist(String name) {
		Object thang = null;

		try {

			if (visCore.has(name))
				thang = visCore.get(name);

		} catch (Exception e) {
			// it's already null
		}
		return thang;
	}

	/**
	 * Local method for logging an event
	 * @param msg event message
	 */
	protected void log(String msg) {
		visLogger.info(
			"module:" + myName + ":" + msg,
			AutohitErrorCodes.CODE_MODULE_REPORTED_INFO_OK);
	}

	/**
	 * Local method for logging an error
	 * @param msg event message
	 */
	protected void error(String msg) {
		visLogger.error(
			"module:" + myName + ":ERROR " + msg,
			AutohitErrorCodes.CODE_MODULE_REPORTED_ERROR);
	}

	/**
	 * Local method for logging an error for a missing param.
	 * @param missing name of paramater missing
	 * @param method name of method called
	 */
	protected void errorparam(String missing, String method) {
		visLogger.error(
			"module:" + myName + ":ERROR.  Missing '" + missing +
			 "' parameter for '" + method + "  method.  Aborting.",
			AutohitErrorCodes.CODE_MODULE_REPORTED_ERROR);
	}


	/**
	 * Local method for logging an warning
	 * @param msg event message
	 */
	protected void warning(String msg) {
		visLogger.error(
			"module:" + myName + ":WARNING " + msg,
			AutohitErrorCodes.CODE_MODULE_REPORTED_WARNING);
	}

	/**
	 * Local method for logging debug information
	 * @param msg event message
	 */
	protected void debug(String msg) {
		visLogger.debug(
			"module:" + myName + ":DEBUG " + msg,
			AutohitErrorCodes.CODE_DEBUGGING_MODULES);
	}

	/**
	 * Build a call exception with our formatting
	 * @param message text of the message
	 * @param code the autohit error code (also available in CallException)
	 * @return a CallException
	 * @see autohit.call.CallException
	 * @see autohit.common.AutohitErrorCodes
	 */
	protected CallException buildException(String message, int code) {
		return new CallException("module:" + myName + ":" + message, code);
	}

	/**
	 * Cause a fault
	 * @param message text of the message
	 * @throws a CallException
	 * @see autohit.call.CallException
	 */
	protected void fault(String message) throws CallException {
		throw this.buildException(message, CallException.CODE_MODULE_FAULT);
	}

	/**
	 * Cause a fault with CHAIN
	 * @param message text of the message
	 * @throws a CallException
	 * @see autohit.call.CallException
	 */
	protected void fault(String message, Throwable t) throws CallException {
		throw this.buildException(message, CallException.CODE_MODULE_FAULT, t);
	}

	/**
	 * Build a call exception with our formatting - chained
	 * @param message text of the message
	 * @param code the autohit error code (also available in CallException)
	 * @param iec initiating exception
	 * @return a CallException
	 * @see autohit.call.CallException
	 * @see autohit.common.AutohitErrorCodes
	 */
	protected CallException buildException(
		String message,
		int code,
		Throwable iec) {
		return new CallException("module:" + myName + ":" + message, code, iec);
	}

	/**
	 * Required parameter.  This one requires the parameter to be a string.
	 * If it is not present or is not a String, it is a serious fault
	 * @param param parameter name
	 * @param method method being called.  Used for error reporting.
	 * @return String with the parameter value
	 * @throws CallException
	 */
	protected String required(String param, String method) throws CallException {
		Object thang =  this.getParam(param);
		if ((thang == null)||(!(thang instanceof String))) {
			throw new CallException(
				"module:"
					+ myName
					+ ":FAULT.  Required parameter'"
					+ param
					+ "' missing (or not a String) from call to method '" + method + "'.",
				CallException.CODE_CALL_FAULT);
		}
		return (String)thang;
	}

	/**
	 * Required parameter.  This one requires the parameter to be of the type specified.
	 * If it is not present or is not the type, it is a serious fault
	 * @param param parameter name
	 * @param type the class of the type required
	 * @param method method being called.  Used for error reporting.
	 * @return String with the parameter value
	 * @throws CallException
	 */
	protected Object requiredType(String param, Class  type, String method) throws CallException {
		Object thang =  this.getParam(param);
		if ((thang == null)||( !(type.isInstance(thang)) )) {
			throw new CallException(
			"module:"
				+ myName
				+ ":FAULT.  Required parameter'"
				+ param
				+ "' missing (or wrong type) from call to method '" + method + "'.",				CallException.CODE_CALL_FAULT);
		}
		return thang;
	}

	/**
	 * Desired parameter.  If it is not present, it is an error message
	 * @param param parameter name
	 * @param method method being called.  Used for error reporting.
	 * @return String with the parameter value or null if it is not present
	 */
	protected String desired(String param, String method) {
		Object thang = this.getParam(param);
		if (thang == null) {
			error("Missing '" + param +
			"' parameter for '" + method + "' method.");
		} else if (!(thang instanceof String)) {
			error("Wrong type for '" + param +
			"' parameter for '" + method + "' method.");
			thang = null;
		}
		return (String)thang;
	}

	/**
	 * Desired parameter.  If it is not present, it is an error message.
	 * @param param parameter name
	 * @param type the class of the type desired
	 * @param method method being called.  Used for error reporting.
	 * @return Object with the parameter value or null if it is not present or the wrong type.
	 */
	protected Object desiredType(String param, Class type, String method) {
		Object thang = this.getParam(param);
		if (thang == null) {
			error("Missing '" + param +
			"' parameter for '" + method + "' method.");
		} else if ( !(type.isInstance(thang)) ) {
			error("Wrong type for '" + param +
			"' parameter for '" + method + "' method.");
			thang = null;
		}
		return thang;
	}

	/**
	 * Optional parameter.  If it is not present, nothing happens.  It expects
	 * a String.  If result is something other than a String, it will return a null.
	 * @param param parameter name
	 * @return String with the parameter value or null if it is not present
	 */
	protected String optional(String param) {
		Object thang = this.getParam(param);
		if ( (thang != null) && (!(thang instanceof String)) )thang = null;
		return (String)thang;
	}

	/**
	 * Optional parameter.  If it is not present, nothing happens.   It expects
	 * the type specified.  If result is something other than that type, it will return a null.
	 * @param param parameter name
	 * @param type the class of the type optional
	 * @return Object with the parameter value or null if it is not present
	 */
	protected Object optionalType(String param, Class type) {
		Object thang = this.getParam(param);
		if ( (thang != null) && (!(type.isInstance(thang))) )thang = null;
		return thang;
	}

	// IMPLEMENTORS

	/**
	 * Execute a named method.  You must implement this method.
	 * You can call any of the helpers for data and services.
	 * The returned object better be a string (for now).  YOU MUST
	 * RETURN SOMETHING--and not null!  If you don't, there will be an exception
	 * up the chain.
	 * @param name name of the method
	 * @see autohit.common.NOPair
	 * @throws CallException
	 */
	public abstract Object execute_chain(String name) throws CallException;

	/**
	 * Allow the subclass a chance to initialize.  At a minium, an 
	 * implementor should create an empty method.
	 * @return the name of the module
	 * @throws CallException
	 */
	protected abstract String instantiation_chain() throws CallException;

	/**
	 * Allow the subclass a chance to cleanup on free.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 */
	protected abstract void free_chain() throws CallException;

}
