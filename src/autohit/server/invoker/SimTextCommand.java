/**
 * AUTOHIT 2003
 * Copyright Erich P Gatejen (c) 1989,1997,2003
 * ALL RIGHTS RESERVED.  See license for details.
 * @author Erich P Gatejen
 */
package autohit.server.invoker;

import java.io.InputStream;

import autohit.common.AutohitProperties;
import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.common.CommandLine;
import autohit.common.channels.Injector;
import autohit.common.channels.SimpleInjector;
import autohit.server.ServerException;
import autohit.server.SystemContext;
import autohit.server.command.Command;
import autohit.server.command.CommandAtom;
import autohit.server.command.CommandServer;
import autohit.vm.VMExecutableWrapper;

/**
 * Text command processor for SimVM. It'll create the command atoms and return
 * them.
 * <p>
 * 
 * <pre>
 *  compile(name) - force compile a script dump(name) - dump an compiled object run(name) {
 *  	vm }
 *  -spawn a script into an automat ps
 *  	- process list by PID kill(PID)
 *  	- kill processes exit
 *  	- exit props
 *  	- list properties set
 *  	- set a property name =
 *  	{ value }
 * </pre>
 * 
 * <p>
 * Only handles VMExecutable compiles now. Compiles to cache space.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 25Apr03
 */
public class SimTextCommand {

	/**
	 * Command dictionary. IMPORTANT! The command tokens must match the numbers
	 * in the CommandRegistry!
	 */

	public final static int TOKEN_COMMAND_BAD = 0;
	public final static String COMMAND_COMPILE = "com";
	public final static int COMMAND_COMPILE_TOKEN = 1;

	public final static String COMMAND_DUMP = "dum";
	public final static int COMMAND_DUMP_TOKEN = 2;

	public final static String COMMAND_RUN = "run";
	public final static int COMMAND_RUN_TOKEN = 3;

	public final static String COMMAND_PS = "ps";
	public final static int COMMAND_PS_TOKEN = 4;

	public final static String COMMAND_KILL = "kill";
	public final static int COMMAND_KILL_TOKEN = 5;

	public final static String COMMAND_PROPS = "props";
	public final static int COMMAND_PROPS_TOKEN = 6;

	public final static String COMMAND_SET = "set";
	public final static int COMMAND_SET_TOKEN = 7;

	public final static String COMMAND_LOADPROPS = "loadprops";
	public final static int COMMAND_LOADPROPS_TOKEN = 8;
	
	public final static String COMMAND_SAVEPROPS = "saveprops";
	public final static int COMMAND_SAVEPROPS_TOKEN = 9;
	
	// Special case. Says we are done.
	public final static String COMMAND_EXIT = "exit";

	public final static int TOKEN_COMMAND_CORRUPT = 99999;

	protected SystemContext sc;
	protected CommandServer cServer;

	private AutohitLogInjectorWrapper log;
	private Injector controlInjector;
	private Injector clientInjector;
	private String stc_id = null; // object unique ID

	/**
	 *  Default constructor */
	public SimTextCommand() {
		sc = null;
		log = null;
	}

	/**
	 * Initialize with defaults. It'll use the default SystemContext injectors
	 * which are tied to the AutohitLogManager. You can call this as often as
	 * you want, but must be called at least once.
	 * 
	 * @param c
	 *           the SystemContext
	 */
	public void init(SystemContext c) throws Exception {

		sc = c;
		log = c.getRootLogger();

		// Default control injector is the root
		try {
			SimpleInjector __controlInjector;
			__controlInjector = (SimpleInjector)log.sinjector;
			__controlInjector.setDefaultSenderID(AutohitProperties.SYSTEM_COMMANDCONTROL_ID);
			controlInjector = __controlInjector;
		} catch (Exception eeee) {
			// Means someone is using something other than a simple logger
			// for the root.  This is ok, but it is up to them to handle
			// setting the default ID!
			controlInjector = log.sinjector;
		}	

		// Create a default client injector
		stc_id = Integer.toString(c.uniqueInteger());
		SimpleInjector __clientInjector = new SimpleInjector();
		__clientInjector.setDefaultSenderID(AutohitProperties.SYSTEM_COMMANDRESPONSE_ID);
		clientInjector = __clientInjector;
		c.getLogManager().addClient(clientInjector, stc_id);
	}

	/**
	 * Initialize. Specify the contol and client channels. You can call this as
	 * often as you want, but must be called at least once.
	 * 
	 * @param control
	 *           Control injector
	 * @param client
	 *           Client injector
	 * @param c
	 *           the SystemContext
	 */
	public void init(SystemContext c, Injector control, Injector client) throws Exception {
		sc = c;
		log = c.getRootLogger();

		// Default control injector is the root
		controlInjector = control;

		// Create a default client injector
		clientInjector = client;

		// was there a prior default client
		if (stc_id != null) {
			// discard the client
			sc.getLogManager().discardClient(stc_id);
			stc_id = null;
		}
	}

	/**
	 * This will create a command atom based on the command passed
	 * 
	 * @param command
	 *           textual command
	 * @throws ServerException.
	 *            It will throw a AutohitErrorCodes.CODE_SERVER_DONE if given
	 *            the exit command.
	 * @return CommandAtom
	 */
	public CommandAtom create(String command) throws ServerException {

		CommandAtom a = null;
		CommandLine cl = new CommandLine();
		cl.start(command);

		// Check the context
		if (log == null) {
			throw new ServerException(
				"Command ERROR: Bad context or context not set.",
				ServerException.CODE_SERVER_BAD_CONTEXT_FAULT);
		}

		// Parse command and run
		try {
			String cmd = cl.get();

			if (cmd.startsWith(COMMAND_COMPILE)) {
				a = compile(cl);
			} else if (cmd.startsWith(COMMAND_DUMP)) {
				a = dump(cl);
			} else if (cmd.startsWith(COMMAND_RUN)) {
				a = run(cl);
			} else if (cmd.startsWith(COMMAND_PS)) {
				a = ps(cl);
			} else if (cmd.startsWith(COMMAND_KILL)) {
				a = kill(cl);
			} else if (cmd.startsWith(COMMAND_PROPS)) {
				a = props(cl);
			} else if (cmd.startsWith(COMMAND_SAVEPROPS)) {
				a = saveprops(cl);			
			} else if (cmd.startsWith(COMMAND_LOADPROPS)) {
				a = loadprops(cl);				
			} else if (cmd.startsWith(COMMAND_SET)) {
				a = set(cl);
			} else if (cmd.startsWith(COMMAND_EXIT)) {
				throw new ServerException("Command EXIT!", AutohitErrorCodes.CODE_SERVER_DONE);
			} else {
				log.error("Command ERROR:Unknown command.", AutohitErrorCodes.CODE_COMMAND_UNKNOWN);
			}

		} catch (ServerException se) {
			throw se;

		} catch (Exception e) {
			//log.error("Command ERROR:Command corrupt.", AutohitErrorCodes.CODE_COMMAND_UNKNOWN);
			// empty line.  ignore it
		}
		return a;
	}

	// Format helpers
	private void error(String name, String message) {
		log.error("Command:" + name + ":ERROR!  " + message, AutohitErrorCodes.CODE_COMMAND_ERROR);
	}

	// Format helpers
	private void info(String name, String message) {
		log.info("Command:" + name + ": " + message, AutohitErrorCodes.CODE_INFORMATIONAL_OK);
	}

	// Format helpers
	private void finished(String name, String r) {
		log.debug("Command:" + name + ":Finshed.  Receipt value=" + r, AutohitErrorCodes.CODE_DEBUGGING);
	}

	/**
	 * Compile helper */
	private CommandAtom compile(CommandLine cli) throws ServerException {

		CommandAtom response = null;
		try {
			// Check params
			String source = cli.get();
			if (source == null) {
				error("compile", "Required parameter 'name' missing.");
				return null;
			}

			// Build it.
			response =
				new CommandAtom(
					COMMAND_COMPILE_TOKEN,
					Command.createCommand(sc.getUniverse(), controlInjector, clientInjector, null, source, null));

		} catch (Exception e) {
			error("compile", "Compile command creation failed.  " + e.getMessage());
		}
		return response;
	}

	/**
	 * Dump helper */
	private CommandAtom dump(CommandLine cli) throws ServerException {

		InputStream is;
		String data = null;
		VMExecutableWrapper ob;

		CommandAtom response = null;
		try {

			// Check params
			String source = cli.get();
			if (source == null) {
				error("dump", "Required parameter 'name' missing.");
				return null;
			}
			response =
				new CommandAtom(
					COMMAND_DUMP_TOKEN,
					Command.createCommand(sc.getUniverse(), controlInjector, clientInjector, null, source, null));

		} catch (Exception e) {
			error("dump", "Dump command creation failed.  " + e.getMessage());
		}
		return response;
	}

	/**
	 * Save props helper */
	private CommandAtom saveprops(CommandLine cli) throws ServerException {

		InputStream is;
		String data = null;

		CommandAtom response = null;
		try {

			// Check params
			String source = cli.get();
			if (source == null) {
				error("saveprops", "Required parameter 'universe destination' missing.");
				return null;
			}
			response =
			new CommandAtom(
					COMMAND_SAVEPROPS_TOKEN,
					Command.createCommand(sc.getUniverse(), controlInjector, clientInjector, null, source, null));

		} catch (Exception e) {
			error("saveprops", "Saveprops command creation failed.  " + e.getMessage());
		}
		return response;
	}
	
	

	/**
	 * Load props helper */
	private CommandAtom loadprops(CommandLine cli) throws ServerException {

		InputStream is;
		String data = null;

		CommandAtom response = null;
		try {

			// Check params
			String source = cli.get();
			if (source == null) {
				error("loadprops", "Required parameter 'universe source' missing.");
				return null;
			}
			response =
			new CommandAtom(
					COMMAND_LOADPROPS_TOKEN,
					Command.createCommand(sc.getUniverse(), controlInjector, clientInjector, null, source, null));

		} catch (Exception e) {
			error("loadprops", "Loadprops command creation failed.  " + e.getMessage());
		}
		return response;
	}
	
	
	/**
	 * Run helper */
	private CommandAtom run(CommandLine cli) throws ServerException {
		CommandAtom response = null;

		try {
			// Check required param
			String source = cli.get();
			if (source == null) {
				error("run", "Required parameter 'name' missing.");
				return null;
			}

			// See if they specified the VM class
			String vm = cli.get();
			if (vm == null)
				vm = "autohit.vm.SimVM";

			// build command
			response =
				new CommandAtom(
					COMMAND_RUN_TOKEN,
					Command.createCommand(sc.getUniverse(), controlInjector, clientInjector, vm, source, null));

		} catch (Exception e) {
			error("run", "RUN command creation failed.  message=" + e.getMessage());
		}
		return response;
	}

	/**
	 * PS helper */
	private CommandAtom ps(CommandLine cli) throws ServerException {

		CommandAtom response = null;
		try {
			response = new CommandAtom(COMMAND_PS_TOKEN, Command.createCommand(null, controlInjector, clientInjector, null, null, null));

		} catch (Exception e) {
			error("ps", "PS command creation failed.  " + e.getMessage());
		}
		return response;
	}

	/**
	 * Kill helper */
	private CommandAtom kill(CommandLine cli) throws ServerException {

		CommandAtom response = null;
		try {

			// Check params
			String source = cli.get();
			if (source == null) {
				error("kill", "Required parameter 'PID' missing.");
				return null;
			}

			// Build it.
			response =
				new CommandAtom(
					COMMAND_KILL_TOKEN,
					Command.createCommand(null, controlInjector, null, null, source, null));

		} catch (Exception e) {
			error("kill", "Kill command creation failed.  " + e.getMessage());
		}
		return response;
	}

	/**
	 * PS helper */
	private CommandAtom props(CommandLine cli) throws ServerException {

		CommandAtom response = null;
		try {
			response = new CommandAtom(COMMAND_PROPS_TOKEN, Command.createCommand(null, controlInjector, clientInjector, null, null, null));

		} catch (Exception e) {
			error("props", "Invoker command properties list creation failed.  " + e.getMessage());
		}
		return response;
	}

	/**
	 * Set helper */
	private CommandAtom set(CommandLine cli) throws ServerException {

		CommandAtom response = null;
		try {

			// Check params
			String source = cli.get();
			if (source == null) {
				error("set", "Required parameter 'name=value pair' missing.");
				return null;
			}
			if (source.indexOf('=') < 1)
				throw new NumberFormatException();

			// Build it
			response =
				new CommandAtom(
					COMMAND_SET_TOKEN,
					Command.createCommand(sc.getUniverse(), controlInjector, clientInjector, null, source, null));
		} catch (IndexOutOfBoundsException ie) {
			error(
				"set",
				"Set command creation failed.  No name/value string given.  It should be in the form 'name=..text..'");
		} catch (Exception e) {
			error("set", "Set command creation failed.  " + e.getMessage());
		}
		return response;
	}

	/**
	 * finalizer Clear anything we don't need */
	protected void finalize() throws Throwable {
		super.finalize();
		log.debug("SimTextCommand: Exiting.  closing any connections and channels.", AutohitErrorCodes.CODE_DEBUGGING);

		// was there a prior default client
		if (stc_id != null) {
			// discard the client
			sc.getLogManager().discardClient(stc_id);
		}
	}

}
