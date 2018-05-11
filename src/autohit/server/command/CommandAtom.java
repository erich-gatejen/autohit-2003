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

import autohit.common.channels.Atom;
import autohit.common.channels.Injector;

/**
 * A command Atom.  It carries a command.  It is based on a channel Atom
 * so it should be transmittable.<p>
 * The object must be a String if it carries a single, required parameter,
 * or a Vector if it carries multiple, required or optional parameters.
 * See the specific target command for usage.
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 24Jul03</code>
 * 
 */
public class CommandAtom extends Atom {

	final static long serialVersionUID = 1;
	
	/**
	 * Numeric values
	 */
	public final static int	UNKNOWN_COMMAND = 0;
	
	/**
	 *  Default constructor.  Default priority of ROUTINE.  Null object.
	 *  Generic type.  Timestamped.   You may need to set ri to a
	 * response injector, though the handler may use a default.
	 */
	public CommandAtom() {
		super();
		numeric = UNKNOWN_COMMAND;
		type = Atom.TYPE_CONTROL;
	}
	
	/**
	 *  Set response injector constructor.  
	 *  Default priority of ROUTINE.  Null object.
	 *  Generic type.  Timestamped.
	 */
	public CommandAtom(Injector response) {
		super();
		numeric = UNKNOWN_COMMAND;
		type = Atom.TYPE_CONTROL;
	}
	
	/**
	 * Constructor Sets command numeric and param object.
	 * @param n the command numeric
	 * @param o the object
	 */
	public CommandAtom(int  n, Object	o) {
		super(Atom.TYPE_CONTROL, Atom.ROUTINE, n, o);
	}

	/**
	 * Use this constructor, as it is the most convenient
	 * @param n the command numeric
	 * @param o the object
	 * @param p the priority
	 */
	public CommandAtom(int  n, Object	o, int  p) {
		super(Atom.TYPE_CONTROL,p,n,o);
	}
	
}
