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
package autohit.vm;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.commons.collections.ExtendedProperties;

import autohit.call.Call;
import autohit.call.CallException;
import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.common.AutohitProperties;
import autohit.server.SystemContext;
import autohit.universe.Universe;
import autohit.universe.UniverseException;


/**
 * Root loader.  Basic caching loader half-singleton.  It shares the routine cache, but the
 * call cache is local.  It does not check to see if anything was updated.  Be
 * sure to call init after instantiation or behavior is undefined!  It isn't
 * entirely threadsafe, but good enough.
 * <p>
 * It is also responsible for creating cores, and giving logging and universe access to a VM.
 * <p>
 * A loader is not "valid" until both init() and create() are called.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 12may03</code> 
 * 
 */
public class VMLoader {

	// universe
	public SystemContext sc;

	// logger
	public AutohitLogInjectorWrapper log;

	// routine cache
	static private Hashtable cache;

	// core factory cache
	private VMCoreFactory corefactory;

	/**
	 * Default Constructor.
	 */
	public VMLoader() {
		if (cache == null)
			cache = new Hashtable();
		corefactory = new VMCoreFactory();
	}

	/**
	 * Initializer.
	 * @param sctx the system context
	 */
	public void init(SystemContext sctx) {
		sc = sctx;
		log = sc.getRootLogger();
	}

	/**
	 * Create a core.
	 */
	public VMCore create() {
		
		// create it
		VMCore core = corefactory.allocate();
		
		// mirror all the invoker props
		Hashtable iprop = sc.getInvokerProperties();
		String ikey;
		Object ivalue;
		for (Enumeration e = iprop.keys(); e.hasMoreElements();) {

			try {
				ikey = (String)e.nextElement(); 
				ivalue = iprop.get(ikey);
				core.store(ikey, ivalue);
			} catch (Exception ecc) {
				// ignore and just disqualify that entry
			}
		}

		return core;
	}

	/**
	 *  Load
	 *  @param name of routine to load
	 *  @return an executable
	 * 	@throws VMException unable to load.
	 */
	public VMExecutable load(String name) throws VMException {

		if (cache.containsKey(name)) {
			// cache hit
			//log.debug("Loader(routine-basic): cache hit [" + name + "].");
			return (VMExecutable) cache.get(name);
		}

		VMExecutableWrapper wrapper = new VMExecutableWrapper();
		try {
			log.debug(
				"Loader(routine-basic): Loading [" + name + "].",
				AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);

			Universe u = sc.getUniverse();
			InputStream is =
				u.getStream(
					AutohitProperties.literal_UNIVERSE_CACHE
						+ AutohitProperties.literal_NAME_SEPERATOR
						+ name);
			wrapper.load(is);

		} catch (UniverseException e) {
			if (e.numeric == UniverseException.UE_OBJECT_DOESNT_EXIST) {
				throw new VMException(
					"Loader(routine-basic): Loading program "
						+ name
						+ " not compiled and available.  ",
					VMException.CODE_VM_EXEC_DOES_NOT_EXIST_FAULT,
					e);
			} else {
				throw new VMException(
					"Loader(routine-basic): Loading program "
						+ name
						+ " caused a Universe Exception: "
						+ e.getMessage(),
					VMException.CODE_VM_SUBSYSTEM_FAULT,
					e);
			}
		} catch (Exception e) {
			throw new VMException(
				"Loader(routine-basic): Loading program "
					+ name
					+ " caused a fundimental Exception: "
					+ e.getMessage(),
				VMException.CODE_VM_GENERAL_FAULT,
				e);
		}
		cache.put(name, wrapper.exec);
		return wrapper.exec;
	}

	/**
	 *  Get a call.  If it isn't in the cache, load it.  There is no thread
	 *  safety here at all!  However, it shouldn't matter since there is a
	 *  loader per VM.
	 *  @param name of routine to load
	 * 	@param core a VMCore that holds a callcache
	 *  @param li log injector to give to the call
	 *  @return runnable call
	 * 	@throws VMException unable to load.
	 */
	public Call get(String name, VMCore core, AutohitLogInjectorWrapper li) throws VMException {

		if (core.callcache.containsKey(name)) {
			// cache hit
			//log.debug(
			//	"Loader(call-basic): cache hit [" + name + "].",
			//	AutohitErrorCodes.LOG_INFORMATIONAL_OK);
			return (Call) core.callcache.get(name);
		}

		Call c = null;

		try {
			String decoratedName = "autohit.call.Call_" + name.toUpperCase();
			Class t = Class.forName(decoratedName);
			c = (Call) t.newInstance();
			Universe u = sc.getUniverse();
			c.load(core, sc, li);

			log.debug(
				"Loader(call-basic): Instantiated a [" + name + "].",
				AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);

			core.callcache.put(name, c);

		} catch (ClassNotFoundException ef) {
			throw new VMException(
				"Loader(call-basic): CALL does not exist.  error="
					+ ef.getMessage(),
				VMException.CODE_VM_GENERAL_FAULT,
				ef);

		} catch (CallException ex) {
			throw new VMException(
				"Loader(call-basic): Instantiation error for ["
					+ name
					+ "].  Not aborting, but state of CALL undefined.  Error="
					+ ex.getMessage(),
				VMException.CODE_VM_CALL_FAULT,
				ex);

		} catch (Exception e) {
			throw new VMException(
				"Loader(call-basic): CALL does not exist.  error="
					+ e.getMessage(),
				VMException.CODE_VM_GENERAL_FAULT,
				e);
		}
		return c;
	}

	/**
	 *  Flush the entire routine cache
	 * 	@throws VMException if it locked by something else.
	 */
	public void flush() {

		Hashtable holding = cache;
		synchronized (holding) {
			cache = new Hashtable();
		}
	}

	/**
	 *  Flush a specific routine out fo the cache
	 * 	@throws VMException if it locked by something else.
	 */
	public void flush(String name) {

		Hashtable holding = cache;
		synchronized (holding) {
			try {
				cache.remove(name);
			} catch (Exception e) { // dont care
			}
		}
	}
	/**
	 * Gets a property from the SystemContext.  Normally, you should handle
	 * all system interaction through a Universe.  Don't use this unless you
	 * have no choice.
	 *  Returns the value or null if it does not exist
	 * @param name of the property
	 * @return the value as a String or null if it doesn't exist
	 */
	public String property(String name) {

		String thang = null;
		try {
			ExtendedProperties ep = sc.getPropertiesSet();
			thang = (String) ep.getProperty(name);
		} catch (Exception e) {
			// nothing.  null will return
		}
		return thang;
	}

}