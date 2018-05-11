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

import autohit.common.AutohitProperties;

/**
 * A very simple injector.  Post anything and everything.  You can set a default 
 * sender ID with setDefaultSenderID.  If it is set as anything but null, when an atom without
 * a sender id is posted, the sender id will be changed to this default.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 27Apr03<br>
 * EPG - Add default post - 24Jul03<br>
 * EPG - Set default sender ID - 23Sep03
 * </code> 
 * 
 */
public class SimpleInjector implements Injector {

	private Channel myChannel;
	
	/**
	 * Default sender ID.  It starts as null.  You must set it.
	 */	
	public String defaultSenderID = null;

	/**
	 * Default contructor
	 */
	public SimpleInjector() {
		myChannel = null;
	}

	/**
	 * Post an item
	 * @param id The new default sender ID.  If set to null, it will not try and set the sender ID.
	 */
	public void setDefaultSenderID(String id) {
		defaultSenderID =  id;
	}
	
	/**
	 * Post an item
	 * @param a An atom to post
	 * @return a receipt
	 */
	public Receipt post(Atom a) throws ChannelException {
		if (myChannel == null)
			throw new ChannelException(
				"Not registered to a channel.",
				ChannelException.CODE_CHANNEL_DOESNT_EXIST_ERROR);
		
		if (a.senderID == null) {
			a.senderID = defaultSenderID;
		}
		
		return myChannel.inject(a);
	}

	/**
	 * Set channel callback
	 * @return a receipt
	 */
	public void setChannel(Channel c) throws ChannelException {
		myChannel = c;
	}

	/**
	 * Post a a default item.  Use this if you aren't sure what
	 * kind of Atom this channal normally services.
	 * @param numeric value
	 * @param o object to post (often a string)
	 * @return a receipt
	 * @throws ChannelException
	 */
	public Receipt defaultPost(int numeric, Object o) throws ChannelException {
		String thingthang;
		Receipt result = null;

		try {
			Atom a = new Atom(Atom.TYPE_GENERIC, Atom.ROUTINE, numeric, o);
			if (defaultSenderID == null) {
				a.senderID = AutohitProperties.SYSTEM_GENERIC_ID;
			} else {
				a.senderID = defaultSenderID;
			}
			result = this.post(a);
		} catch (Exception e) {
			throw new ChannelException(
				"Sender ["
					+ AutohitProperties.SYSTEM_GENERIC_ID
					+ "] could not defaultPost.",
				ChannelException.CODE_CHANNEL_FAULT);
		}
		return result;
	}

	/**
	 * Instantiate a default atom for this kind of injector.  It will be completely
	 * bare.
	 * @return default atom
	 */
	public Atom defaultAtom() {
		return new Atom();
	}

}
