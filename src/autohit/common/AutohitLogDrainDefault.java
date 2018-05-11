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

/**
 * An default subclass of AutohitLogDrain.
 * output streams.
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 28Jul03</code> 
 * 
 */
public class AutohitLogDrainDefault extends AutohitLogDrain {

	/**
	 * The subclass uses this to set the Writer.  the Writer is the
	 * field myWriter.
	 * @param id
	 */
	public void setWriter(String id) {
		// use default!
	}

	/**
	 * The subclass uses this to discard the Writer.  It says this id isn't
	 * being used anymore.
	 * @param id
	 */
	public void discardWriter(String id) {
		// use default!
	}


	/**
	 * The subclass should implement this to do any initialization.
	 */
	public void initchain() {
		// don't care!
	}
}