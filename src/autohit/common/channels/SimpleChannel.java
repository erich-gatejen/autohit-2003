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
import java.util.Enumeration;

/**
 * Channel implementation
 * 
 * Supports a simple, exclusive channel.  There is no routing, but just a single
 * priority level and type.  Everything less than or equal to the priority set will
 * match.  Type is unimportant.  It will accept multiple injectors, but only one Drain.  Registering
 * a new drain will replace the old.  Only injection is thread safe.  Nothing 
 * else is.
 * <p>
 * The injectors are unimporant.  We'll just toss them in a table and 
 * let them rot.
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03</code> 
 */
public class SimpleChannel implements Channel {

	/**
	 * Injectors
	 */
	private Hashtable	injectors;
	
	/**
	 * Drain
	 */
	private Drain	myDrain;
	
	/**
	 * Priority level
	 */
	private int		priority;
	
	/**
	 * Receipt values.  let it overflow (java spec says it will do so ok)
	 */
	private int		next;
	
	/**
	 * Default constructor
	 */
	public SimpleChannel() {
		myDrain = null;
		injectors = new Hashtable();
		next = 0;
		priority = Atom.P_NONE;
	}
	
	/**
	 * Register an injector
	 * @param name reference
	 * @param i An injector
	 * @see autohit.common.channels.Injector
	 */
	public void register(String name, Injector	i) throws ChannelException {
		if (!injectors.containsKey(name)) {
			injectors.put(name, i);
		}
		i.setChannel(this);
	}

	/**
	 * Register a drain
	 * @param name reference
	 * @param d A drain
	 * @see autohit.common.channels.Drain
	 */
	public void register(String name,  Drain	d) throws ChannelException {
		synchronized(this) {
			myDrain = d;
			//don't care about the name
		}
	}

	/**
	 * Get a drain by name
	 * @param name Name reference to the drain
	 * @return Drain reference or null if not found
	 * @see autohit.common.channels.Drain
	 */
	public Drain getDrain(String 	name) throws ChannelException {
		// Dont care about the name
		return myDrain;
	}

	/**
	 * Get an injector by name
	 * @param name Name reference to the injector
	 * @return Injector reference or null if not found
	 * @see autohit.common.channels.Drain
	 */
	public Injector getInjector(String 		name) throws ChannelException {
		if (!injectors.containsKey(name)) {
			return null;
		} else {
			return (Injector)injectors.get(name);
		}
	}
	
	/**
	 * Remove an injector
	 * @param name reference
	 * @see autohit.common.channels.Injector
	 */
	public void removeInjector(String name) throws ChannelException {
		if (!injectors.containsKey(name)) {
			throw new ChannelException("Injector " + name + "not registered", ChannelException.CODE_CHANNEL_INJECTOR_INVALID_ERROR);
		} else {
			Injector i = (Injector) injectors.remove(name);
			i.setChannel(null);
		}	
	}

	/**
	 * Remove a drain
	 * @param name reference
	 * @see autohit.common.channels.Drain
	 */
	public void removeDrain(String name) throws ChannelException {
		synchronized(this) {
			myDrain = null;	
		}		
	}
	
	/**
	 * Typically called by an injector
	 * @param a An item
	 * @return receipt or null if failed
	 */
	public Receipt inject(Atom	a)  throws ChannelException {
		Receipt r = new Receipt();
		r.setAsInteger(next++);
				
		// Priority check
		if (a.priority <= priority) {
			synchronized(this) {
				if (myDrain != null) {
					myDrain.post(a);

				} else {
					r = null;	
				}
				
			} // end critical section
		}
		return r;
	}

	/**
	 * Request priority level for named Drain
	 * @param name Drain's name
	 * @param level the level as specifies in an Atom
	 * @see autohit.common.channels.Atom
	 */
	public 	Receipt	requestLevel(String name, int level)   throws ChannelException {
		Receipt r =  new Receipt();
		r.setAsInteger(next++);
		
		if ((level < Atom.P_NONE)||(level > Atom.P_TOP)) {
			throw new ChannelException("SimpleChannel: Bad priority level requested:" + level, ChannelException.CODE_CHANNEL_BAD_PRIORITY_LEVEL_ERROR);
		}
		 
		synchronized(this) {
			priority = level;
		}
		return r;
	}

	/**
	 * Remove level for named Drain
	 * @param name Drain's name
	 * @param level the level as specifies in an Atom
	 * @see autohit.common.channels.Atom
	 */
	public 	Receipt removeLevel(String name, int level)  throws ChannelException {
		// Don't care
		Receipt r =  new Receipt();
		r.setAsInteger(next++);
		return r;
	}

	/**
	 * Request type 
	 * @param name Drain's name
	 * @param type the type as specified in Atom
	 * @see autohit.common.channels.Atom
	 */
	public 	Receipt	requestType(String name, int type)   throws ChannelException {
		// Don't care
		Receipt r =  new Receipt();
		r.setAsInteger(next++);
		return r;		
	}

	/**
	 * Request type 
	 * @param name Drain's name
	 * @param type the type as specified in Atom
	 * @see autohit.common.channels.Atom
	 */
	public 	Receipt	removeType(String name, int type)   throws ChannelException {
		// Don't care
		Receipt r =  new Receipt();
		r.setAsInteger(next++);
		return r;
	}

	/**
	 * Set exclusive
	 * @param name Drain's name
	 */
	public 	Receipt	setExclusive(String name)   throws ChannelException {
		// always exclusive
		// Don't care
		Receipt r =  new Receipt();
		r.setAsInteger(next++);
		return r;
	}

	/**
	 * Remove exclusive
	 * @param name Drain's name
	 */
	public 	Receipt	removeExclusive(String name)   throws ChannelException {
		// always exclusive
		// Don't care
		Receipt r =  new Receipt();
		r.setAsInteger(next++);
		return r;
	}

	/**
	 * Enumerate injectors
	 * 
	 * @return an enumeration of injectors
	 * @see autohit.common.channels.Injector
	 */
	public Enumeration enumInjector() throws ChannelException {
		return injectors.keys();
	}
	
}
