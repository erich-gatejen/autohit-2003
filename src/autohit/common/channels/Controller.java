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

import java.util.Hashtable;

/**
 * Channel controller.  This controller assumes the 
 * hashtable provides enough thread safety.  I'll get fancy in the future.
 * <p>
 * Someone, somewhere needs to create an instance of this or the channel
 * system will not work.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03</code>
 * 
 */
public class Controller {

	/**
	 * Registry of channels.  There is only ONE per java VM
	 */
	static private Hashtable registry;

	/**
	 * Default constructor
	 */
	public Controller() {
		// build only once--ever.
		if (registry == null)
			registry = new Hashtable();
	}

	/**
	 * Register a channel
	 * @param name String name
	 * @param ch A channel
	 */
	public static void register(String name, Channel ch)
		throws ChannelException {

		// see if a controller instance exists
		if (registry == null) {
			throw new ChannelException(
				"No one has created a controller",
				ChannelException.CODE_CHANNEL_BAD_CONTROLLER_FAULT);
		}

		if (registry.containsKey(name)) {
			throw new ChannelException(
				"Channel " + name + "already registered",
				ChannelException.CODE_CHANNEL_ALREADY_EXISTS_ERROR);
		}
		registry.put(name, ch);
	}

	/**
	 * Remove a channel
	 * @param name String name
	 */
	public static void remove(String name) throws ChannelException {

		// see if a controller instance exists
		if (registry == null) {
			throw new ChannelException(
				"No one has created a controller",
				ChannelException.CODE_CHANNEL_BAD_CONTROLLER_FAULT);
		}

		if (!registry.containsKey(name)) {
			throw new ChannelException(
				"Channel " + name + " does not exist.",
				ChannelException.CODE_CHANNEL_DOESNT_EXIST_ERROR);
		}
		registry.remove(name);
	}

	/**
	 * Tune to a channel
	 * @param name String name
	 */
	public static Channel tune(String name) throws ChannelException {

		// see if a controller instance exists
		if (registry == null) {
			throw new ChannelException(
				"No one has created a controller",
				ChannelException.CODE_CHANNEL_BAD_CONTROLLER_FAULT);
		}

		if (!registry.containsKey(name)) {
			throw new ChannelException(
				"Channel " + name + " does not exist.",
				ChannelException.CODE_CHANNEL_DOESNT_EXIST_ERROR);
		}
		return (Channel) registry.get(name);
	}

}
