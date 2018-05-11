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
package autohit.system.tellio;

import java.io.Serializable;
//import autohit.common.Constants;

/**
 * TELL IO working engine.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 04Jan04<br>
 * /code>
 */
public class TellClientEngine implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 * TELL tokens
	 */
	public static final int STATE_NONE = 0;
	public static final int STATE_ASKING = 1;
	public static final int STATE_ASKED = 2;
	public static final int STATE_TELLING = 3;
	public static final int STATE_TOLD = 4;
	public static final int STATE_FETCHING = 5;
	public static final int STATE_GAVE = 6;
	public static final int STATE_GETTING_DATA = 7;
	public static final int STATE_GIVING_DATA = 8;
	public static final int STATE_ERROR = 99;

	/**
	 * instruction
	 * @serial
	 */
	public int state;

	
	/**
	 *  Initialize the engine.
	 *
	 *  @return a String containing the dump.
	 */
	public void init() {
		state = STATE_NONE;
	}
	
	
	
	
	
}
