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
import java.util.Enumeration;

/**
 * Channel interface
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i>
 * <code>EPG - Initial - 25Apr03</code>
 */
public interface Channel {

	/**
	 * Constants */
	public final static String BAD_RECEIPT = "bad";

	/**
	 * Register an injector
	 * 
	 * @param name
	 *           reference
	 * @param i
	 *           An injector
	 * @see autohit.common.channels.Injector
	 */
	public void register(String name, Injector i) throws ChannelException;

	/**
	 * Register a drain
	 * 
	 * @param name
	 *           reference
	 * @param d
	 *           A drain
	 * @see autohit.common.channels.Drain
	 */
	public void register(String name, Drain d) throws ChannelException;

	/**
	 * Get a drain by name
	 * 
	 * @param name
	 *           Name reference to the drain
	 * @return Drain reference or null if not found
	 * @see autohit.common.channels.Drain
	 */
	public Drain getDrain(String name) throws ChannelException;

	/**
	 * Get an injector by name
	 * 
	 * @param name
	 *           Name reference to the injector
	 * @return Injector reference or null if not found
	 * @see autohit.common.channels.Drain
	 */
	public Injector getInjector(String name) throws ChannelException;

	/**
	 * Enumerate injectors
	 * 
	 * @return an enumeration of injectors
	 * @see autohit.common.channels.Injector
	 */
	public Enumeration enumInjector() throws ChannelException;

	/**
	 * Remove an injector
	 * 
	 * @param name
	 *           reference
	 * @see autohit.common.channels.Injector
	 */
	public void removeInjector(String name) throws ChannelException;

	/**
	 * Remove a drain
	 * 
	 * @param name
	 *           reference
	 * @see autohit.common.channels.Drain
	 */
	public void removeDrain(String name) throws ChannelException;

	/**
	 * Typically called by an injector
	 * 
	 * @param a
	 *           An item
	 */
	public Receipt inject(Atom a) throws ChannelException;

	/**
	 * Request level for named Drain
	 * 
	 * @param name
	 *           Drain's name
	 * @param level
	 *           the level as specifies in an Atom
	 * @see autohit.common.channels.Atom
	 */
	public Receipt requestLevel(String name, int level) throws ChannelException;

	/**
	 * Remove level for named Drain
	 * 
	 * @param name
	 *           Drain's name
	 * @param level
	 *           the level as specifies in an Atom
	 * @see autohit.common.channels.Atom
	 */
	public Receipt removeLevel(String name, int level) throws ChannelException;

	/**
	 * Request type
	 * 
	 * @param name
	 *           Drain's name
	 * @param type
	 *           the type as specified in Atom
	 * @see autohit.common.channels.Atom
	 */
	public Receipt requestType(String name, int type) throws ChannelException;
	;

	/**
	 * Request type
	 * 
	 * @param name
	 *           Drain's name
	 * @param type
	 *           the type as specified in Atom
	 * @see autohit.common.channels.Atom
	 */
	public Receipt removeType(String name, int type) throws ChannelException;

	/**
	 * Set exclusive
	 * 
	 * @param name
	 *           Drain's name
	 */
	public Receipt setExclusive(String name) throws ChannelException;

	/**
	 * Remove exclusive
	 * 
	 * @param name
	 *           Drain's name
	 */
	public Receipt removeExclusive(String name) throws ChannelException;

}
