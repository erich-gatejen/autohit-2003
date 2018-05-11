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
package autohit.server;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.collections.ExtendedProperties;

import autohit.common.AutohitBasicLogManager;
import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitException;
import autohit.common.AutohitLogDrain;
import autohit.common.AutohitLogDrainDefault;
import autohit.common.AutohitLogDrainRouting;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.common.AutohitProperties;
import autohit.common.Utils;
import autohit.common.channels.Controller;
import autohit.common.channels.Injector;
import autohit.creator.compiler.SimCompiler;
import autohit.creator.compiler.XmlCompiler;
import autohit.server.command.CommandRegistry;
import autohit.universe.Universe;
import autohit.universe.UniverseFactory;
import autohit.vm.VMLoader;

/**
 * A simple system context for a server or invoker.
 * <p><pre>
 * It includes:
 * a basic log manager (@see #logger)
 * a root logger
 * a basic properties set (@see #prop)
 * a single script SIM compiler (@see #compiler)
 * a universe factory (@see #uf)
 * a LOCAL universe server
 * a Kernel (uninitialized!)
 * a root loader (uninitialized!)
 * </pre><p>
 * It requires the root property be set.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03 
 * 
 */
public class SimpleSystemContext implements SystemContext {

	/**
	 * Primary log manager
	 */
	public AutohitBasicLogManager logManager;

	/**
	 * Root log injector
	 */
	public AutohitLogInjectorWrapper logger;

	/**
	 * Script compiler.  Keep at least one around.
	 */
	public SimCompiler compiler;

	/**
	 * Our simple little universe.
	 */
	public Universe uni;

	/**
	 * The system properties set
	 */
	public ExtendedProperties prop;

	/**
	 * Invoker properties set
	 */
	public ExtendedProperties invokerprop;

	/**
	 * Universe factory
	 */
	public UniverseFactory uf;

	/**
	 * Channel controller
	 */
	public Controller cc;

	/**
	 * Kernel
	 */
	public Kernel k;

	/**
	 * Kernel
	 */
	private VMLoader loader;

	/**
	 * Debugging flag
	 */
	public boolean debug;

	/**
	 * Root path
	 */
	public String root;

	/**
	 * Unique number counter -- it will be unique for all instances of SimpleSystemContext.
	 */
	public static int uniqueN = 0;

	/**
	 *  Default Constructor.
	 */
	public SimpleSystemContext() throws Exception {
		// Doesn't do anything
	}

	/**
	 *  Properties constructor.  Give it a full path to the 
	 *  properties file.
	 */
	public void init(ExtendedProperties props) throws Exception {

		// save 'em
		prop = props;

		// defaults
		debug = false;

		// Make my universe factory
		uf = new UniverseFactory();

		// Make a channel controller
		cc = new Controller();

		// BUILD LOG MANAGER ---------------------------------------
		
		int ald_line_size = AutohitProperties.LOGS_LINE_SIZE_DEFAULT;
		
		// LINE SIZE?
		String logsize =
		(String) Utils.testGetProperty(
				AutohitProperties.LOGS_LINE_SIZE,
				prop);
		if (logsize != null) {
			try {
				ald_line_size = Integer.parseInt(logsize);
			} catch (Exception e) {
				// No where to report it anyway
			}
		}
				
		// If a log type is file, use a file otherwise use console, 
		// even if not set.
		String logprop =
			(String) Utils.testGetProperty(
				AutohitProperties.LOGS_TYPE_CONTROL,
				prop);
		String logClientprop =
			(String) Utils.testGetProperty(
				AutohitProperties.LOGS_TYPE_CLIENT,
				prop);
		AutohitLogDrain controlDrain;
		AutohitLogDrain clientDrain;

		// CONTROL DRAIN
		if ((logprop == null)
			|| (logprop.equals(AutohitProperties.LOGS_TYPE__FILE))) {
			try {
				logprop =
					(String) Utils.testGetProperty(
						AutohitProperties.LOGS_LOCATION_CONTROL + AutohitProperties.literal_FS_LOG_EXTENSION,
						prop);
				FileOutputStream fco = new FileOutputStream(logprop, true);
				controlDrain = new AutohitLogDrainDefault();
				controlDrain.init(fco, ald_line_size);
			} catch (Exception e) {
				// If something goes wrong, default to console
				controlDrain = new AutohitLogDrainDefault();
				controlDrain.init(System.err, ald_line_size);
			}
		} else {
			controlDrain = new AutohitLogDrainDefault(); // drain to stderr
			controlDrain.init(System.err, ald_line_size);
		}
		// CLIENT DRAIN
		if ((logClientprop == null)
			|| (logClientprop.equals(AutohitProperties.LOGS_TYPE__FILE))) {
			try {
				logClientprop =
					(String) Utils.testGetProperty(
						AutohitProperties.LOGS_LOCATION_CLIENT,
						prop);
				FileOutputStream fco =
					new FileOutputStream(logClientprop, true);
				AutohitLogDrainRouting rclientDrain = new AutohitLogDrainRouting();
				rclientDrain.init(fco, ald_line_size);
				rclientDrain.setup(logClientprop);
				clientDrain = rclientDrain;
			} catch (Exception e) {
				// If something goes wrong, default to console
				clientDrain = new AutohitLogDrainDefault();
				clientDrain.init(System.err, ald_line_size);
				System.out.println("SimpleSystemContext:Could not build AutohitLogDrainRouting.  Using stderr instead.  message=" + e.getMessage());
			}
		} else {
			clientDrain = new AutohitLogDrainDefault(); // drain to stderr
			clientDrain.init(System.err, ald_line_size);
		}
		logManager = new AutohitBasicLogManager(controlDrain, clientDrain);

		// CONFIGURE LOGGERS -----------------------------------------
		// Get root logger.  Set some attributes
		logger = logManager.getRootLogger();

		// Configure the logger
		logger.info(
			"Root logger starting.",
			AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);
		logprop =
			(String) Utils.testGetProperty(
				AutohitProperties.LOGS_PRETTY_PRINT,
				prop);
		if ((logprop != null) && (logprop.equals("true"))) {
			logger.info(
				"Root logger pretty-print = true.",
				AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);
			logManager.pretty(true);
		}
		logprop =
			(String) Utils.testGetProperty(
				AutohitProperties.LOGS_TIMESTAMP,
				prop);
		if ((logprop != null) && (logprop.equals("true"))) {
			logger.info(
				"Root logger timestamp = true.",
				AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);
			logManager.stampit(true);
		}

		logprop =
			(String) Utils.testGetProperty(
				AutohitProperties.LOGS_LINE_LIMIT,
				prop);
		if (logprop != null) {
			try {
				int ll = Integer.parseInt(logprop);
				logger.info(
					"Root logger line limit set to " + ll,
					AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);
				logManager.getDrain().setLineLimit(ll);
				logManager.getClientDrain().setLineLimit(ll);
			} catch (Exception e) {
				logger.error(
					"Value for "
						+ AutohitProperties.LOGS_LINE_LIMIT
						+ " cannot be parsed as an integer.  Using default.  Value="
						+ logprop,
					AutohitErrorCodes.CODE_CONFIGURATION_ERROR);
			}
		}

		// Are we debugging???
		logprop =
			(String) Utils.testGetProperty(
				AutohitProperties.SYSTEM_DEBUG,
				prop);
		if ((logprop != null) && (logprop.equals("true"))) {
			logger.info(
				"DEBUGGING IS ON.",
				AutohitErrorCodes.CODE_INFORMATIONAL_OK);
			logManager.debugOn();
			debug = true;
		}

		logger.debug(
			"DEBUGGING root logger up.",
			AutohitErrorCodes.CODE_INFORMATIONAL_OK);
		logger.info(
			"Root logger up.",
			AutohitErrorCodes.CODE_INFORMATIONAL_OK_VERBOSE);

		// At this point, log any exceptions and abort
		try {

			// Check for 
			root =
				(String) Utils.testGetProperty(
					AutohitProperties.ROOT_PATH,
					prop);
			if (root == null) {
				logger.error(
					"ERROR.  Root property not set!",
					AutohitErrorCodes.CODE_STARTUP_CONFIGURATION_FAULT);
				throw new AutohitException(
					"Required root property not set.",
					AutohitException.CODE_STARTUP_CONFIGURATION_FAULT);
			}

			// BUILD COMPILER
			compiler = new SimCompiler(this);

			// Get universe config
			String handle =
				(String) Utils.testGetProperty(
					AutohitProperties.DEFAULT_UNIVERSE_HANDLE,
					prop);
			if (handle == null) {
				handle = AutohitProperties.literal_DEFAULT_UNIVERSE_HANDLE;
			}
			String upath =
				(String) Utils.testGetProperty(
					AutohitProperties.DEFAULT_UNIVERSE_PATH,
					prop);
			if (upath == null) {
				upath = AutohitProperties.literal_DEFAULT_UNIVERSE_PATH;
			}
			String uprop =
				(String) Utils.testGetProperty(
					AutohitProperties.DEFAULT_UNIVERSE_PROP,
					prop);
			if (uprop == null) {
				uprop = AutohitProperties.literal_DEFAULT_UNIVERSE_PROP;
			}
			
			// INVOKER PROPERTIES
			invokerprop = new ExtendedProperties();

			// BUILD OUR UNIVERSE
			uni = uf.create(handle, root + upath + "/" + uprop);

			// BUILD THE ROOT KERNEL
			k = new Kernel();
			k.init(this);

			// BUILD THE LOADER
			loader = new VMLoader();
			loader.init(this);

		} catch (Exception e) {
			logger.error(
				"ERROR during context instantiation.  Aborting.  Exception="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_STARTUP_FAULT);
			throw e;
		}
	}

	/**
	 * Load properties.  It will delete any previously loaded properties.
	 * @param props a properties set
	 */
	public void loadProperties(ExtendedProperties props) throws Exception {
		prop.combine(props);
	}

	/**
	 *  Get the default universe
	 * @return Universe service interface
	 */
	public Universe getUniverse() {
		return uni;
	}

	/**
	 *  Get a universe service by handle.  This implementation ignores the
	 *  handle and returns the only universe we have.
	 * @param handle handle to the universe
	 * @return Universe service interface
	 */
	public Universe getUniverse(String handle) {
		// ignore the handle.  We only have one.
		return uni;
	}

	/**
	 *  Get the XML compiler
	 * @return XmlCompiler base class
	 */
	public XmlCompiler getCompiler() {
		return compiler;
	}

	/**
	 *  Get a reference to a generic, root log injector
	 * @return XmlCompiler base class
	 */
	public AutohitLogInjectorWrapper getRootLogger() {
		return logger;
	}

	/**
	 *  Cheat.  The logger will be our even dispacter
	 * @return Injector reference
	 */
	public Injector getEventDispatcher() {
		return (Injector) logger;
	}

	/**
	 *  Get properties set
	 * @return reference to the properties set
	 */
	public ExtendedProperties getPropertiesSet() {
		return prop;
	}

	/**
	 *  Get log manager reference
	 * @return reference to the properties set
	 */
	public AutohitBasicLogManager getLogManager() {
		return logManager;
	}

	/**
	 *  Get the Kernel
	 * @return reference to the kernel
	 */
	public Kernel getKernel() {
		return k;
	}

	/**
	 *  Get the VM Loader
	 * @return reference to the kernel
	 */
	public VMLoader getLoader() {
		return loader;
	}

	/**
	 *  Get the command registry as a properties set.  It is not cached.  
	 *  this implementation will always return the default set.
	 * @return properties set representing the registry.  it will return null if it cannot be loaded
	 */
	public CommandRegistry getCommandRegistry() {

		CommandRegistry rset = null;
		try {
			rset =
				new CommandRegistry(
					new FileInputStream(
						root + AutohitProperties.COMMAND_DEFAULT_REGISTRY));

		} catch (Exception e) {
			// don't care.  it will return null.
			logger.error(
				"FAULT!  Fauled to create a CommandRegistry!  message="
					+ e.getMessage(),
				AutohitErrorCodes.CODE_COMMAND_REGISTRY_FAULT);
		}
		return rset;
	}

	/**
	 *  Unique number
	 * @return an integer number unique (at least) to this Context
	 */
	public synchronized int uniqueInteger() {
		uniqueN++;
		return uniqueN;
	}
	
	/**
	 *  Get debugging state
	 * @return true if debugging active
	 */
	public boolean debuggingState() {
		return debug;
	}

	/**
	 * Get a reference to the invoker properties set.   Generally, only
	 * invokers should add anything to the set.  It should be safe for anyone
	 * to read from it.  It is up to the invoker to maintain its contents.
	 * @return reference to the invoker properties set
	 */
	public ExtendedProperties getInvokerProperties() {
		return invokerprop;
	}

}
