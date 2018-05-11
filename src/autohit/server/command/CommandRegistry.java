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

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;

import autohit.common.Constants;
import autohit.server.ServerException;

/**
 * Command registry implementation.  This is immutable.  Once created it is
 * set.  If you need to catch changes in the registry, you need to create
 * a new instance.  Using the default constructor will cause an exception!
 * <p>
 * This version of the registry will implement it as a ExtendedProperties
 * set.
 * <p>
 * The registry has two sections.  The first section associates the commands
 * with numerics.  There may ne NO duplicate numerics.  (If there are, the system 
 * state is undefined.)  This section is constructed as follows:<p>
 * <code>
 * command.1=compile
 * command.2=dump
 * <br>
 * ("command.")(numeric)("=")(string name)</code><p>
 * The second section contains entries for the commands defined in section one.
 * This section is as follows:<p>
 * <code>
 * compile.class=autohit.server.command.CommandCompile
 * compile.help=This will compile a script found at the target.
 * <br>
 * (string name)(".class=")(class name for implementation)
 * (string name)(".help=")(string giving a hint about the command)</code><p>
 * 
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Jul03</code>
 * 
 */
public class CommandRegistry extends ExtendedProperties {

	final static long serialVersionUID = 1;
	
	/**
	 * field literals
	 */
	public final static String FL_COMMAND = "command.";
	public final static String FL_CLASS = ".class";
	public final static String FL_HELP = ".help";

	/**
	 * Default Constructor.  Don't use!
	 * @throws ServerException
	 */
	public CommandRegistry() throws ServerException {
		throw new ServerException(
			"Programmer used default constructor for CommandRegsitry.  this is a bug.",
			ServerException.CODE_SW_DETECTED_FAULT);
	}

	/**
	 * Constructor to create and load from an input stream.
	 * @param is input stream from where to read the registry
	 * @throws ServerException
	 */
	public CommandRegistry(InputStream is) throws ServerException {
		super();
		try {
			load(is);
		} catch (Exception e) {
			throw new ServerException(
				"Failed to load the command registry.  message="
					+ e.getMessage(),
				ServerException.CODE_COMMAND_REGISTRY_FAULT);
		}
	}

	/**
	 * Get an instance of the command specified by the numeric
	 * @param numeric numeric for the command
	 * @throws ServerException
	 * @return the object instance.
	 */
	public Object instance(int numeric) throws ServerException {

		String name = Constants.UNKNOWN;
		Command target = null;
		String classname = Constants.UNKNOWN;

		try {
			// See if the atom points to a valid command
			String numericS = FL_COMMAND + Integer.toString(numeric);
			if (this.containsKey(numericS)) {

				// Find the name
				name = this.getString(numericS);
				classname = this.getString(name + FL_CLASS);

				// create the instance
				Class t = Class.forName(classname);
				target = (Command) t.newInstance();

			} else {
				throw new ServerException(
					"Command Registry does not have requested command.  Command ignored.  Command numeric="
						+ numeric,
					ServerException.CODE_COMMAND_UNKNOWN);
			}
			
		} catch (ClassCastException cee) {
			throw new ServerException(
				"Command Registry instance() failed because of malformed '.class' entry for "
					+ name + ".  numeric="	+ numeric,
				ServerException.CODE_COMMAND_REGISTRY_FAULT, cee);
		} catch (ClassNotFoundException cne) {
			throw new ServerException(
				"Command Registry instance() failed because of malformed '.class' entry for "
					+ name
					+ ".  ClassNotFound for entry=["
					+ classname
					+ "].",
				ServerException.CODE_COMMAND_REGISTRY_FAULT,
				cne);
		} catch (ServerException se) {
			throw se;
		} catch (Exception ee) {
			throw new ServerException(
				"Command Registry instance() failed to gross exception.  message="
					+ ee.getMessage(),
				ServerException.CODE_COMMAND_REGISTRY_FAULT,
				ee);
		}
		return target;
	}
}
