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

package autohit.universe;

import java.io.InputStream;
import java.util.Hashtable;

import autohit.universe.service.UniverseLocal;

/**
* Universe factory.  This builds universe services.  All the functions are
* thread-safe, however it will not keep one thread from destroying 
* another thread's universe.  This is *NOT* a singleton.
*
* There are four types of universes:
*	UNI_LOCAL   	Local file system
*	UNI_MASTER 		NOT IMPLIMENTED!
*	UNI_MIRROR		NOT IMPLIMENTED!
*	UNI_REMOTE		NOT IMPLIMENTED!

*
* Regardless of what kind of universe, there must be a local property file
* that describes the universe.  The factory will use it to build a
* server for that universe.
*
* @author Erich P. Gatejen
* @version 1.0
* <i>Version History</i>
* <code>EPG - New - 23Apr03</code> 
* 
*/
public class UniverseFactory {

	/**
	 * Table of universe services
	 * @param name universe name
	 * @return the loaded object
	 */	
	Hashtable		services;

	/**
	 *  Constructor.  Not a singleton, so build the services.
	 */
	public UniverseFactory() {
		services = new Hashtable();
	}

	/**
	 *  Returns true if the named universe service exists.
	 *  It will only throw an Exception on a software detected fault.
	 * @param name universe name
	 * @return true if the universe service is valid
	 */
	public synchronized boolean exists(String name) throws UniverseException {
		try {
			if (services.containsKey(name)) return true;
		} catch (Exception e) {
			// fall out to return false
		}
		return false;
	}

	/**
	 *  Get a reference to a valid, working universe service.
	 * @param name universe name
	 * @return reference to a universe service
	 * @throws autohit.universe.UniverseException
	 */
	public synchronized Universe reference(String name) throws UniverseException {
	
		if (!services.containsKey(name)) {
			throw new UniverseException("Named universe service does not exist for this factory.", UniverseException.UE_NAMED_UNIVERSE_SERVICE_DOESNT_EXIST);
		}
		return (Universe) services.get(name);
	}
	
	/**
	 *  Destroy a universe server
	 * @param name universe name
	 * @throws autohit.universe.UniverseException
	 */
	public synchronized void destroy(String name) throws UniverseException {
	
		if (!services.containsKey(name)) {
			throw new UniverseException("Named universe service does not exist for this factory.", UniverseException.UE_NAMED_UNIVERSE_SERVICE_DOESNT_EXIST);
		}
		Universe u = (Universe)services.remove(name);
		u.close();			
	}

	/**
	 *  Create a valid, working universe service
	 * @param handle handle to the universe.  non-unique handle will overwrite a refernce to an existing universe service.
	 * @param prop a path to the local universe property file
	 * @return reference to a universe service
	 * @throws autohit.universe.UniverseException
	 */
	public synchronized Universe create(String handle, String  prop) throws UniverseException {
		UniverseProperties up;
		Universe	u;
		try {
			 up= new UniverseProperties(prop);
		} catch (Exception e) { 
			throw new UniverseException("Serious universe creation error: " + e.getMessage(), UniverseException.UE_DEFAULT, e); 
		}
		u = completeCreate(up);	
		services.put(handle, u);
		return u;
	}
	
	/**
	 *  Create a valid, working universe service
	 * @param handle handle to the universe.  non-unique handle will overwrite a refernce to an existing universe service.
	 * @param prop a path to the local universe property file
	 * @return reference to a universe service
	 * @throws autohit.universe.UniverseException
	 */
	public synchronized Universe create(String handle, InputStream  prop) throws UniverseException {
		UniverseProperties up;
		Universe	u;
		try {
			 up= new UniverseProperties(prop);
		} catch (Exception e) { throw (UniverseException)e; }
		u = completeCreate(up);	
		services.put(handle, u);
		return u;	
	}

	/**
	 *  Private 
	 */
	private Universe completeCreate(UniverseProperties prop) throws UniverseException {
		
		Universe	uTemp;
		
		switch (prop.getType()) {
			case UniverseProperties.UNI_LOCAL :
				uTemp = (Universe) new UniverseLocal();
				uTemp.genesis(prop);
				break;
	
			case UniverseProperties.UNI_EXTENDED :
				throw new UniverseException("Universe Factory does not support extended types--YET", UniverseException.UE_NOT_SUPPORTED);
				//break;
				
			default :
				throw new UniverseException("Universe Factory does not support creating type=" + prop.getType(), UniverseException.UE_NOT_SUPPORTED);
		}
		return uTemp;
	}

}



