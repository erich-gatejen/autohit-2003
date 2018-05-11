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

/**
 * An receipt
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03
 * EPG - Added setAsInteger and toString to enable future expansion - 2Jul03</code> 
 * 
 */
public class QueueReceipt extends Receipt {

	final static long serialVersionUID = 1;
	
	/**
	 *  Default constructor.  DONT USE!  It will throw an exception!
	 * @throws Exception
	 */
	public QueueReceipt() throws Exception {
		throw new Exception("BAD PROGRAMMER!  DO NOT USE DEFAULT CONSTRUCTOR! Don't you ever read the javadoc?");
	}

	/**
	 * Constructor.  Use this one!  This will create a non-guaranteeed
	 * unique reciept.
	 * @param id ID of the channel
	 * @param name Name of the the drain
	 * @param numbered Number of the enqueue.
	 */
	public QueueReceipt(String id, String name, int numbered){
		this.info = id + '.' + name + '.' + numbered;
	}

	/**
	 *  return the type;
	 * @return a string representing the type, in this case it is "queue".
	 */
	public String getType() {
		return "queue";
	}

	/**
	 * Set as an integer.  This one doesn't do anything!
	 * @param val a value to set as the receipt.
	 */
	public void setAsInteger(int val) {
		// Don't do anything.
	}
	
}
