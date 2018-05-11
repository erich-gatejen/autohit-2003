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
package autohit.common.channels;

import java.io.Serializable;

/**
 * An receipt
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03
 * EPG - Added setAsInteger and toString to enable future expansion - 2Jul03</code> 
 * 
 */
public class Receipt implements Serializable {

	final static long serialVersionUID = 1;
	
	/**
	 * Generic receipt
	 */
	protected String info;

	/**
	 *  return the type;
	 * @return a string representing the type, in this case it is "generic".
	 */
	public String getType() {
		return "generic";
	}

	/**
	 *  default receipt
	 * @param val a value to set as the receipt.
	 */
	public void setAsInteger(int val) {
		info = Integer.toString(val);
	}
	
	/**
	 * This is a bit clunky, but I expect real receipt
	 * implementations will overload it.
	 * @return a string representation of the receipt.
	 */
	public String toString() {
		return info;
	}

}
