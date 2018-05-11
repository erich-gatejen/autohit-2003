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
package autohit.server.command;

import java.util.Vector;
import java.io.Serializable;

import autohit.common.channels.Atom;
import autohit.common.channels.Injector;
import autohit.common.channels.Receipt;
import autohit.server.ServerException;
import autohit.server.SystemContext;
import autohit.universe.Universe;
import autohit.common.AutohitErrorCodes;
import autohit.common.channels.ChannelException;

/**
 * A command base class. All command inplementations should use extend it.
 * <p>
 * THIS IS NOT THREAD SAFE!!!! EVERY CommandServer should create their own
 * instances!!!! There are object fields that store the command parameters.
 * It's the cleanest way to do it. Cope and deal.
 * <p>
 * All exceptions should/will be caught and reported on the response channel
 * except if the command is poorly formed (it still will try to report) or is a
 * very serious problem.
 * <p>
 * <code>
 * Command sequence is as follows:
 * 1- Command class instantiation
 * 2- Get command parameters.  Kept in a vector.  Contstructed with
 * the helper static method createCommand()
 * 		0-UNI 		- (has default) Universe
 * 		1-RESPONSE 	- (has default) Injector for control response channel
 * 		2-TARGET	- (has default) Injector for target channel
 * 		3-CLASS		- Implementation class
 * 		4-COMMAND	- Command string
 * 		5-OBJECT	- Data object
 * 3- Call the command with call()
 * 		- Setup base
 * 4- Call verify in subclass()
 * 		- Subclass should call assert method in base for required/optional 
 *        for the parameters.
 * 		- The assert method will set the object fields.
 * 		- The base assert will throw an exception for any error.  It
 * 		  MUST be passed along.  The base will handle any reporting.
 * 5- Base will accept the command.
 * 6- Passed into chained execute()
 * 		- Problems should be thrown as exceptions
 * 7- Base will ack or nak the command.
 * 8- Return a receipt
 * </code>
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 24Jul03
 * EPG - Rewrite - 31Jul03
 * </code>
 */
public abstract class Command implements Serializable {

	/**
	 * Constants */
	public final static char RESPONSE_ELEMENT_SEPERATOR = '|';
	public final static int UNI_LIST_INDEX = 0;
	public final static int RESPONSE_LIST_INDEX = 1;
	public final static int TARGET_LIST_INDEX = 2;
	public final static int CLASS_LIST_INDEX = 3;
	public final static int COMMAND_LIST_INDEX = 4;
	public final static int OBJECT_LIST_INDEX = 5;

	/**
	 * Command objects. Don't change these! */
	public Universe uni;
	public Injector response;
	public Injector target;
	public String classobject;
	public String command;
	public Object data;
	public int uniqueID;

	private Vector commandlist;
	protected SystemContext sc;

	/**
	 * Execute the command.
	 * 
	 * @throws ServerException
	 * @return return the manreadable message for success.
	 */
	abstract public String execute() throws ServerException;

	/**
	 * Verify the command. Basically, it should just call the assert method
	 * with the six parameters indicating if the fields are required or not
	 * (optional). The first three (uni, target, and reponse) have defaults, if
	 * nothing is passed. However, the the assert will throw an exception if it
	 * is marked as required but isn't present.
	 * <p>
	 * The exception from accept should not be intercepted! The following is an
	 * example implementation.
	 * <p>
	 * <code>
	 * 
	 * public String verify() throws ServerException {
	 *   this.assert(true,true,false,false,false,true);
	 * 	 return "parameters are good.";
	 * }
	 * </code>
	 * 
	 * @throws ServerException
	 * @return return the manreadable message for accepting the command.
	 */
	abstract public String verify() throws ServerException;

	/**
	 * Get the textual name for the command.
	 * 
	 * @return return the manreadable name of this command.
	 */
	abstract public String getName();

	/**
	 * Execute the command. String Method.
	 * <p>
	 * it will throw a ServerException if the command is poorly formed and
	 * cannot be accepted or there is a serious system problem. Outwise it
	 * should just log issues to the response (or default) and return no
	 * receipt.
	 * 
	 * @param c
	 *           the System Context
	 * @param cl
	 *           the command list in a Vector
	 * @throws ServerException
	 * @return a receipt for the transaction (given by the responseChannel).
	 *         This can be a log injector.
	 */
	public Receipt call(SystemContext c, Vector cl) throws ServerException {

		// Setup
		String victorydance = ".";
		commandlist = cl;
		sc = c;
		Receipt rr = null;
		uniqueID = sc.uniqueInteger();

		// Catch any wild-assed exceptions
		try {

			// Verify the parameters
			try {
				this.verify();
			} catch (ServerException se) {
				// Abort the command. If the response channel is valid, send it
				// there,
				// otherwise use the default
				if (response == null)
					response = sc.getRootLogger().sinjector;
				this.respond("malformed and aborted!", AutohitErrorCodes.EVENT_COMMAND_FAILED, null);
				return null;
			}

			// Accept the command
			this.respond("accepted.", AutohitErrorCodes.EVENT_COMMAND_ACCEPTED, null);

			// Execute it
			try {
				String vic = this.execute();
				if (vic != null)
					victorydance = vic;

			} catch (ServerException sse) {
				// Failed command. nak and break out
				this.respond("failed.  code[" + sse.numeric + "] " + sse.getMessage(), 
						     AutohitErrorCodes.EVENT_COMMAND_FAILED, null);
				return null;
			}

			// Issue the receipt
			rr = new Receipt();
			rr.setAsInteger(uniqueID);

			// Ack the command
			this.respond("succeeded.  Receipt issued.  " + victorydance,
				         AutohitErrorCodes.EVENT_COMMAND_COMPLELTED, rr);

		} catch (ChannelException ece) {
			// This is pretty bad. If we can't use the channel, then the system
			// state is dubious. What good is a command, if you can't give it
			// a response? So PANIC!
			throw new ServerException(
				this.getMsgHeader()
					+ "PANIC!  Could not use the responseChannel.  This makes it useless.  exception="
					+ ece.getMessage(),
				ServerException.CODE_SERVER_PANIC,
				ece);
		} catch (Exception e) {
			// This should not have happened. Bad programmer should have
			// trapped
			// all exceptions before this!
			throw new ServerException(
				this.getMsgHeader()
					+ "Software Detected Fault in Command.class.  Someone let an exception out.  File a bug.  exception="
					+ e.getMessage(),
				ServerException.CODE_SW_DETECTED_FAULT,
				e);
		}
		return rr;
	}

	/**
	 * Assert the parameters.
	 * 
	 * @param univ
	 *           is 0-UNI required?
	 * @param resp
	 *           is 1-RESPONSE required?
	 * @param targ
	 *           is 2-TARGET required?
	 * @param cobj
	 *           is 3-CLASS required?
	 * @param cmd
	 *           is 4-COMMAND required?
	 * @param dobj
	 *           is 5-OBJECT required?
	 * @throws ServerException.
	 *            Do not intercept it!
	 */
	protected void assertparam(boolean univ, boolean resp, boolean targ, boolean cobj, boolean cmd, boolean dobj)
		throws ServerException {

		uni = null;
		response = null;
		target = null;
		classobject = null;
		command = null;
		data = null;

		try {
			Object thang;

			// 0-UNI
			thang = commandlist.elementAt(UNI_LIST_INDEX);
			if (thang == null) {
				if (univ) {
					throw new ServerException("0-UNI Universe is required", ServerException.CODE_COMMAND_ERROR);
				} else {
					uni = sc.getUniverse(); //default
				}
			} else if (!(thang instanceof Universe)) {
				throw new ServerException(
					"0-UNI Universe paramter is not a Universe",
					ServerException.CODE_COMMAND_ERROR);
			} else
				uni = (Universe) thang; // accept passed

			// 1-RESPONSE
			thang = commandlist.elementAt(RESPONSE_LIST_INDEX);
			if (thang == null) {
				if (resp) {
					throw new ServerException("1-RESPONSE Response is required", ServerException.CODE_COMMAND_ERROR);
				} else {
					response = sc.getRootLogger().sinjector; //default
				}
			} else if (!(thang instanceof Injector)) {
				throw new ServerException(
					"1-RESPONSE Response paramter is not an Injector",
					ServerException.CODE_COMMAND_ERROR);
			} else
				response = (Injector) thang; // accept passed

			// 2-TARGET
			thang = commandlist.elementAt(TARGET_LIST_INDEX);
			if (thang == null) {
				if (targ) {
					throw new ServerException("2-TARGET Response is required", ServerException.CODE_COMMAND_ERROR);
				} else {
					target = sc.getRootLogger().sinjector; //default
				}
			} else if (!(thang instanceof Injector)) {
				throw new ServerException(
					"2-TARGET Response paramter is not an Injector",
					ServerException.CODE_COMMAND_ERROR);
			} else
				target = (Injector) thang; // accept passed

			// 3-CLASS
			if (cobj) {
				thang = commandlist.elementAt(CLASS_LIST_INDEX);
				if (thang == null) {
					throw new ServerException("3-CLASS Class is required", ServerException.CODE_COMMAND_ERROR);
				}
				if (!(thang instanceof String)) {
					throw new ServerException(
						"3-CLASS Class paramter is not a String",
						ServerException.CODE_COMMAND_ERROR);
				}
				classobject = (String) thang;
			}

			// 4-COMMAND
			if (cmd) {
				thang = commandlist.elementAt(COMMAND_LIST_INDEX);
				if (thang == null) {
					throw new ServerException("4-COMMAND Command is required", ServerException.CODE_COMMAND_ERROR);
				}
				if (!(thang instanceof String)) {
					throw new ServerException(
						"4-COMMAND Command paramter is not a String",
						ServerException.CODE_COMMAND_ERROR);
				}
				command = (String) thang;
			}

			// 5-OBJECT
			if (dobj) {
				thang = commandlist.elementAt(OBJECT_LIST_INDEX);
				if (thang == null) {
					throw new ServerException("5-OBJECT Data Object is required", ServerException.CODE_COMMAND_ERROR);
				}
				if (!(thang instanceof Object)) {
					throw new ServerException(
						"5-OBJECT Data Object paramter is not an Object",
						ServerException.CODE_COMMAND_ERROR);
				}
				data = (Object) thang;
			}

		} catch (ServerException se) {
			throw se;
		} catch (Exception e) {
			throw new ServerException(
				"Command list corrupt.  message=" + e.getMessage(),
				ServerException.CODE_COMMAND_FAULT,
				e);
		}
	}

	// HELPER
	public String getMsgHeader() {
		return "CMD:" + this.getName() + " id[" + uniqueID + "] ";
	}

	/**
	 * Send response.  The response MUST be set.
	 * 
	 * @param info
	 *           text of the response
	 * @param code
	 *           code of the response. Usually an EVENT
	 * @param rr
	 *           Receipt for the command. May be null, if none was issued.
	 */
	public void respond(String info, int code, Receipt rr) throws Exception {
		response.post(new CommandResponseAtom(code, this.getMsgHeader() + info, Atom.ROUTINE, uniqueID, rr));
	}

	/**
	 * Send target.   The target MUST be set.
	 * 
	 * @param info
	 *           text of the response
	 * @param code
	 *           code of the response. Usually an EVENT
	 * @param rr
	 *           Receipt for the command. May be null, if none was issued.
	 */
	public void sendTarget(String info, int code, Receipt rr) throws Exception {
		target.post(new CommandResponseAtom(code, this.getMsgHeader() + info, Atom.ROUTINE, uniqueID, rr));
	}

	/**
	 * Create a command list. It is a very good object to make all passed items
	 * Serializable. You cannot predict how the command will be issued. you can
	 * pass null for any parameter you don't want to pass.
	 * 
	 * @param univ
	 *           Specify a universe object?
	 * @param resp
	 *           Specify a response Injector?
	 * @param targ
	 *           Specify a target Injector?
	 * @param cobj
	 *           Specify a command Class implementation (string name)?
	 * @param cmd
	 *           Specify a command string?
	 * @param dobj
	 *           Specify a data object?
	 * @return a Vector representing the command.
	 */
	static public Vector createCommand(
		Universe univ,
		Injector resp,
		Injector targ,
		String cobj,
		String cmd,
		Object dobj) {

		Vector cmdList = new Vector(OBJECT_LIST_INDEX + 1);
		cmdList.setSize(OBJECT_LIST_INDEX + 1);
		cmdList.set(UNI_LIST_INDEX, univ);
		cmdList.set(RESPONSE_LIST_INDEX, resp);
		cmdList.set(TARGET_LIST_INDEX, targ);
		cmdList.set(CLASS_LIST_INDEX, cobj);
		cmdList.set(COMMAND_LIST_INDEX, cmd);
		cmdList.set(OBJECT_LIST_INDEX, dobj);

		return cmdList;
	}

}
