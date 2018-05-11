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
  *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 27Apr03</code> 
 * 
 */
public interface Injector {

	/**
	 * Post an item
	 * @return a receipt
	 */
	public Receipt post(Atom	a) throws ChannelException;
	
	/**
	 * Post a a default item.  Use this if you aren't sure what
	 * kind of Atom this channal normally services.
	 * @param numeric value
	 * @param o object to post (often a string)
	 * @return a receipt
	 * @throws ChannelException
	 */
	public Receipt defaultPost(int numeric, Object o) throws ChannelException;

	/**
	 * Instantiate a default atom for this kind of injector.  It will be completely
	 * bare.
	 * @return default atom
	 */
	public Atom defaultAtom();
	
	/**
	 * Set channel callback
	 * @return a receipt
	 */
	public void setChannel(Channel c) throws ChannelException;
		
}
