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
import autohit.common.channels.Receipt;

/**
 * A command response Atom.  It carries a response to a command.  
 *  * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 17Sep03</code>
 */
public class CommandResponseAtom extends Atom {

	final static long serialVersionUID = 1;
	
	/**
	 * Numeric values
	 */
	public final static int	UNKNOWN_RESPONSE = 0;
	
	/**
	 * Unique ID
	 */
	public int id;
	
	/**
	 * Receipt (optional)
	 */
	public Receipt rr;
	
	/**
	 *  Default constructor.  Default priority of ROUTINE.  Null object.
	 *  Generic type.  Timestamped.   You may need to set ri to a
	 * response injector, though the handler may use a default.
	 */
	public CommandResponseAtom() {
		super();
		numeric = UNKNOWN_RESPONSE;
		type = Atom.TYPE_CONTROL;
	}
	
	/**
	 * Use this constructor, as it is the most convenient
	 * @param n the command numeric
	 * @param text text of the response
	 * @param p the priority
	 * @param sourceCommandID unique id of source command
	 * @param r receipt for command.  May be null if none was issued.
	 */
	public CommandResponseAtom(int  n, String	text,  int  p, int  sourceCommandID, Receipt r) {
		super(Atom.TYPE_EVENT,p,n,(Object)text);
		id = sourceCommandID;
		rr = r;
	}
}
