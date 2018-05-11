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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
//import autohit.common.Constants;

/**
 * TELL IO working engine.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 04Jan04<br>
 * /code>
 */
public class TellServerEngine implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 * TELL tokens
	 */
	public static final int STATE_NEW = 0;
	public static final int STATE_GIVE = 1;
	public static final int STATE_DATA = 2;
	public static final int STATE_ERROR = 99;

	public int state;
	public int numeric;
	
	/**
	 * Initialize the engine.
	 * 
	 * @return a String containing the dump.
	 */
	public void init() {
		state = STATE_NEW;
		numeric = 1;
	}

	/**
	 * Initialize the engine.
	 * 
	 * @return a String containing the dump.
	 */
	public void connected(BufferedInputStream bis) {

		try {

			int next = bis.read();
			while (next >= 0) {
				
				switch (state) {
				
					case STATE_NEW:
						state_new(next, bis);
						break;
						
					case STATE_GIVE:
						//state_give(next, bis);
						break;
						
					case STATE_DATA:
						//state_data(next, bis);
						break;
						
					default:
						throw new TellException("Tellio.Server.connected base state.  Dropping connection.  state=" + state + " token=" + next,TellException.CODE_SYSTEM_GENERIC_ERROR);
				}
					
				next = bis.read();
			}
			
		} catch (TellException te) {

		} catch (EOFException eeof) {

		} catch (IOException iof) {

		} catch (Exception ee) {

		}

	}

	/**
	 * Accept.  (SERVER)
	 * 
	 * @return a String containing the dump.
	 */
	public int state_new(int token, BufferedInputStream bis) throws TellException {

		Tell current = null;
		
		switch (token) {
			case Tell.TELL_ASK :
				//current 
				break;
				
			case Tell.TELL_FETCH :
				break;

			case Tell.TELL_GIVE :
			case Tell.TELL_DATA :
			case Tell.TELL_DONE :
			case Tell.TELL_INVALID :
			case Tell.TELL_BAD :
			case Tell.TELL_TELL :
			case Tell.TELL_STOP :	
			default :
				throw new TellException("Server.accept broken protocol.  Dropping connection.  Token=" + token,TellException.CODE_SYSTEM_TELLIO_BROKEN_PROTOCOL);
		}
		return token;
	}

}
