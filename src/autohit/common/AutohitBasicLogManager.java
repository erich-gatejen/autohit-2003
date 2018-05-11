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
import autohit.common.channels.Atom;
import autohit.common.channels.Controller;
import autohit.common.channels.SimpleChannel;
import autohit.common.AutohitLogDrain;
import autohit.common.channels.Injector;
import autohit.common.channels.SimpleInjector;
import autohit.common.AutohitLogInjectorWrapper;

import autohit.common.traps.CommonsLoggerTrap;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Basic autohit log manager.  If anyone orders a die or lets an instance
 * fall out of scope, it will invalidate all instances (by killing the channel).
 * Don't do it.  You should really only have one of these--ever.
 * <p>
 * This will not create a channel controller!
 * @see autohit.common.channels.Controller
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 23Apr03</code> 
 * 
 */
public class AutohitBasicLogManager {

	//private static Controller myLogController;  // we aren't doing this
	private static SimpleChannel controlChannel;
	private static SimpleChannel clientChannel;
	private static AutohitLogDrain controlDrain;
	private static AutohitLogDrain clientDrain;
	private static Injector rootInjector;
	private static AutohitLogInjectorWrapper rootLogger;
	private static Hashtable clientTable;

	/**
	 * Default constructor.  Creates a generic Drain to System.err.
	 */
	public AutohitBasicLogManager() throws Exception {
		// make sure we only init once.
		if (controlDrain != null)
			return;

		// Create a generic drain and call the other constructor
		controlDrain = new AutohitLogDrainDefault();
		controlDrain.init(System.err);
		clientDrain =  new AutohitLogDrainDefault();
		clientDrain.init(System.err);
		init(controlDrain, clientDrain);
	}

	/**
	 * Constructor.  Specifies a specific drain.  You should call this construcot
	 * only once ever during the life of a JVM.
	 * @param control A control drain
	 * @param client A client drain
	 * @throws Exception which is usually a very bad thing.  
	 */
	public AutohitBasicLogManager(AutohitLogDrain control, AutohitLogDrain client) throws Exception {
		if (controlDrain != null)
			throw new Exception("You can only construct with a specific drain once for the life of the JVM.");
		controlDrain = control;
		clientDrain =  client;
		init(control, client);
	}

	/**
	 * Constructor.  Specifies a specific drain.
	 * @param control A control drain
	 * @param client A client drain
	 * @see autohit.common.AutohitLogDrain
	 * @throws Exception Which usually means something very bad happened.
	 */
	private void init(AutohitLogDrain control, AutohitLogDrain client) throws Exception {

		// Create the channels
		controlChannel = new SimpleChannel();
		clientChannel = new SimpleChannel();
		
		// Register Drains
		controlChannel.register(AutohitProperties.LOGS_CONTROL_DRAIN, control);
		clientChannel.register(AutohitProperties.LOGS_CLIENT_DRAIN, client);

		// Register the root Injector
		rootInjector = new SimpleInjector();
		rootLogger = new AutohitLogInjectorWrapper();
		rootLogger.init(AutohitProperties.LOGS_ROOT_ID, rootInjector);
		controlChannel.register(AutohitProperties.LOGS_CONTROL_INJECTOR, rootInjector);

		// Set routing
		controlChannel.requestLevel(AutohitProperties.LOGS_CONTROL_DRAIN, Atom.P_TOP);
		controlChannel.requestType(AutohitProperties.LOGS_CONTROL_DRAIN, Atom.TYPE_LOG);
		clientChannel.requestLevel(AutohitProperties.LOGS_CLIENT_DRAIN, Atom.P_TOP);
		clientChannel.requestType(AutohitProperties.LOGS_CLIENT_DRAIN, Atom.TYPE_LOG);
		// for future compatibility

		// Register the channels
		Controller.register(AutohitProperties.LOGS_CONTROL_STATION, controlChannel);
		Controller.register(AutohitProperties.LOGS_CLIENT_STATION, clientChannel);

		// Trap any commons loggers
		// TODO move this to some other channel
		CommonsLoggerTrap.setTrap(rootLogger);

		// Make Logger cache
		clientTable = new Hashtable();
	}

	/**
	 * Get the root logger.
	 * @return a reference to the root AutohitLogInjectorWrapper, which wraps the root injector
	 * @see autohit.common.AutohitLogInjectorWrapper
	 */
	public AutohitLogInjectorWrapper getRootLogger() {
		return rootLogger;
	}

	/**
	 * Get the root injector.
	 * @return a reference to the root Injector
	 * @see autohit.common.AutohitLogInjectorWrapper
	 */
	public Injector getRootInjector() {
		return rootInjector;
	}

	/**
	 * Add an injector to the client channel. It is a very good idea to 
	 * discard the logger when you are done.
	 * @param il An injector
	 * @param id A string id for the sender.  Technically, it doesn't have to be unique.
	 * @see autohit.common.channels.Injector
	 */
	public void addClient(Injector il, String id) throws Exception {

			String registerID = AutohitProperties.LOGS_CLIENT_INJECTOR + id;
				
			// Register it
			clientChannel.register(registerID, il);
			clientTable.put(registerID, il);
	}

	/**
	 * It will discard a client injector
	 * @param id A string id for the sender.  Needs to be the same as the one use to register it.
	 */
	public void discardClient(String id) {

		try {

			// if it is in the cache, use it
			if (clientTable.containsKey(id)) {
				clientTable.remove(id);
				clientChannel.removeInjector(id);
			}

		} catch (Exception e) { // don't care.  null should return
		}
	}

	/**
	 * Set pretty formatting on output
	 * @param p Set TRUE for on.
	 */
	public void pretty(boolean p) {
		controlDrain.setPrettyFlag(p);
		clientDrain.setPrettyFlag(p);
	}

	/**
	 * Set timestamp formatting on output
	 * @param p Set TRUE for on.
	 */
	public void stampit(boolean p) {
		controlDrain.setTimestampFlag(p);
		clientDrain.setTimestampFlag(p);
	}

	/**
	 * Get primary drain
	 * @return the primary log drain
	 */
	public AutohitLogDrain getDrain() {
		return controlDrain;
	}

	/**
	 * Get client drain
	 * @return the client log drain
	 */
	public AutohitLogDrain getClientDrain() {
		return clientDrain;
	}

	/**
	 * Discard client drain writer.  This is a terrible hack.
	 * @param id the id of the drain writer to discard
	 */
	public void discardDrainWriter(String id) {
		try {
			clientDrain.discardWriter(id);
		} catch (Exception e) {
			// do nothing
		}
	}

	/**
	 * Turn debug logging on.  It will turn debugging on for the root logger, but 
	 * not any clients!  you have to do that yourself.
	 */
	public void debugOn() {
		try {
			rootLogger.debugFlag(true);
			clientChannel.requestLevel(
				AutohitProperties.LOGS_CLIENT_DRAIN,
				Atom.P_TOP);
			controlChannel.requestLevel(
				AutohitProperties.LOGS_CONTROL_DRAIN,
				Atom.P_TOP);				
		} catch (Exception e) {
			System.out.println(
				"!! LOGGING system failure.  Cannot debugOn() for autohit.*.drain."
					+ e.getMessage());
		}
	}

	/**
	 * Turn debug logging off
	 */
	public void debugOff() {
		try {
			rootLogger.debugFlag(false);
			clientChannel.requestLevel(
				AutohitProperties.LOGS_CLIENT_DRAIN ,
				Atom.ROUTINE);
			controlChannel.requestLevel(
				AutohitProperties.LOGS_CONTROL_DRAIN ,
				Atom.ROUTINE);			
		} catch (Exception e) {
			System.out.println(
				"!! LOGGING system failure.  Cannot debugOff() for autohit.*.drain."
					+ e.getMessage());
		}
	}

	/**
	 * Die
	 */
	public void die() {
		try {

			// Pull drain and all injectors
			controlChannel.removeDrain(AutohitProperties.LOGS_CONTROL_DRAIN );
			controlChannel.removeInjector(AutohitProperties.LOGS_CONTROL_INJECTOR);
			clientChannel.removeDrain(AutohitProperties.LOGS_CLIENT_DRAIN);
			try {
				Object thingthang;
				for (Enumeration e = clientTable.keys(); e.hasMoreElements();) {
					thingthang = e.nextElement();
					clientTable.remove(e);
					clientChannel.removeInjector((String) thingthang);
				}
			} catch (Exception efc) { //dont care
			}

			// Unregister channel
			Controller.remove(AutohitProperties.LOGS_CONTROL_STATION);
			Controller.remove(AutohitProperties.LOGS_CLIENT_STATION);

		} catch (Exception e) { // don't really care - FUBAR
		}
		clientChannel = null;
		controlChannel = null;
	}

	/*
	 * finalizer
	 * Make sure the log handler is unhooked
	 */
	protected void finalize() throws Throwable {
		super.finalize();
		this.die();
	}

}
