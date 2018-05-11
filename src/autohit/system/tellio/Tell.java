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
import java.io.Serializable;
import autohit.common.Utils;
//import autohit.common.Constants;

/**
 * TELL IO primitive interface.  WIP WIP WIP
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><code>EPG - Initial - 04Jan04<br>
 * /code>
 */
public class Tell implements Serializable {

	final static long serialVersionUID = 1;
	
	public static final int MAX_DATA_SIZE = (65535 * 8) - 12;

	/**
	 * TELL tokens
	 */
	public static final int TELL_INVALID = 0;
	public static final int TELL_ASK = 'A';
	public static final int TELL_BAD = 'B';
	public static final int TELL_TELL = 'T';
	public static final int TELL_FETCH = 'F';
	public static final int TELL_GIVE = 'G';
	public static final int TELL_DATA = 'D';
	public static final int TELL_DONE = 'O';
	public static final int TELL_STOP = 'S';

	/**
	 * Instruction
	 * 
	 * @serial
	 */
	public int tell;

	/**
	 * Numeric
	 * 
	 * @serial
	 */
	public int numeric;

	/**
	 * Size
	 * 
	 * @serial
	 */
	public int size;

	/**
	 * Data
	 * 
	 * @serial
	 */
	public byte[] data;

	/**
	 * Dump this Instruction. Mostly for debugging.
	 * 
	 * @return a String containing the dump.
	 */
	public String toString() {
		return tell + ".";
	}

	/**
	 * Build.
	 * 
	 * @return a String containing the dump.
	 */
	public boolean read(BufferedInputStream bis) throws TellException {

		boolean result = false;
		byte[] buf = new byte[4];
		try {

			int temp = 0;

			// read numeric
			buf[1] = (byte) bis.read();
			buf[2] = (byte) bis.read();
			buf[3] = (byte) bis.read();
			buf[4] = (byte) bis.read();
			numeric = Utils.packInteger(buf);

			// read size
			buf[1] = (byte) bis.read();
			buf[2] = (byte) bis.read();
			buf[3] = (byte) bis.read();
			buf[4] = (byte) bis.read();
			size = Utils.packInteger(buf);
			if (size > MAX_DATA_SIZE) {
				throw new TellException(
						"Tell.read broken protocol.  Data over MAX_SIZE(" + MAX_DATA_SIZE + ")  actual size=" + size,
						TellException.CODE_SYSTEM_TELLIO_BROKEN_PROTOCOL);
			}

			// read data
			data = new byte[size];
			int cursor = 0;
			int run = bis.read(data,0,size);
			while (run >=0) {
				
				
			}
			


		
		} catch (TellException tee) {
			throw tee;
		} catch (Exception ee) {

		}
		return result;
	}

	/**
	 * Accept.
	 * 
	 * @return a String containing the dump.
	 */
	public int client_accept(int token) throws TellException {
		switch (token) {

			case TELL_ASK :
			case TELL_BAD :
			case TELL_TELL :
			case TELL_FETCH :
			case TELL_GIVE :
			case TELL_DATA :
			case TELL_DONE :
			case TELL_STOP :

			default :
			case TELL_INVALID :

				}
		tell = token;
		return token;
	}

}
