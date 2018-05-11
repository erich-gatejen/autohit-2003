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
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

/**
* Universe server interface.  The locking mechinism is 
* optional.
*
* @author Erich P. Gatejen
* @version 1.0
* <i>Version History</i>
* <code>EPG - New - 18Apr03</code> 
* 
*/
public interface Universe {

	/**
	 * This will always be called when the universe server is created.
	 * It is the one and only chance to get a reference to the properties
	 * file and do any post-construction setup.
	 * @param props a universe property set
	 * @return the loaded object
	 * @see autohit.universe.UniverseProperties
	 * @throws autohit.universe.UniverseException
	 */
	public void genesis(UniverseProperties props) throws UniverseException;

	/**
	 * This will always be called when the universe is destroyed
	 * @throws autohit.universe.UniverseException
	 */
	public void close() throws UniverseException;

	/**
	 *  Load an object from the universe.  (Not a class!  It is possible
	 *  that this is a shared, loaded object.  That is up to the implimentor.)
	 * @param name universe name
	 * @return the loaded object
	 * @throws autohit.universe.UniverseException
	 */
	public Object get(String name) throws UniverseException;

	/**
	 *  Load an object from the universe.  Guarantee it is a unique
	 *  instance of it.
	 * @param name universe name
	 * @return the loaded object
	 * @throws autohit.universe.UniverseException
	 */
	public Object getUnique(String name) throws UniverseException;

	/**
	 *  Reserve unique object universe.  Guarantee it is a unique
	 *  instance of it.  Great for temp objects.
	 * @param base base path for the object (including root object name)
	 * @return name of the reserved unique object.
	 * @throws autohit.universe.UniverseException
	 */
	public String reserveUnique(String base) throws UniverseException;
	
	/**
	 *  Get an InputStream that can read from a universe object
	 * @param name universe name
	 * @return a stream to the object
	 * @throws autohit.universe.UniverseException
	 */
	public InputStream getStream(String name) throws UniverseException;

	/**
	 *  Get a DataSource that can interact with this universe object
	 * @param name universe name
	 * @return a data source
	 * @throws autohit.universe.UniverseException
	 */
	public DataSource getDataSource(String name) throws UniverseException;

	/**
	 * Get a FileDataSource that can interact with this universe object.
	 * The universe must handle making sure the object at least appears local to the
	 * FileDataSource 
	 * @param name universe name
	 * @return a data source
	 * @throws autohit.universe.UniverseException
	 */
	public FileDataSource getFileDataSource(String name) throws UniverseException;

	
	/**
	 *  Save an object into the universe.  It should use standard serialization.  
	 *  If this won't work correctly, then serialize it yourself to saveStream(String  name, OutputStream  s).
	 * @param name universe name
	 * @param o the object
	 * @throws autohit.universe.UniverseException
	 */
	public void put(String name, Object o) throws UniverseException;

	/**
	 *  Get an output stream to a universe object.  Caller responsible
	 * for streaming and closing.
	 * @param name universe name
	 * @return a stream to the object
	 * @throws autohit.universe.UniverseException
	 */
	public OutputStream putStream(String name) throws UniverseException;

	/**
	 *  Lock an object.  This is blocking.
	 * @param name universe name
	 * @throws autohit.universe.UniverseException
	 */
	public void lock(String name) throws UniverseException;

	/**
	 *  Lock an object.  This is NON blocking.
	 * @param name universe name
	 * @return true if lock completed, false is already locked
	 * @throws autohit.universe.UniverseException
	 */
	public boolean lockIfNotLocked(String name) throws UniverseException;

	/**
	 *  Check to see if the object is locked
	 * @param name universe name
	 * @return true if the object is locked, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public boolean isLocked(String name) throws UniverseException;

	/**
	 *  Release a lock on an object.
	 * @param name universe name
	 * @throws autohit.universe.UniverseException
	 */
	public void release(String name) throws UniverseException;

	/**
	 *  Check to see if an object exists
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public boolean exists(String name) throws UniverseException;

	/**
	 *  Flush an object.  Typically not useful unless caching is implemented
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public void flush(String name) throws UniverseException;

	/**
	 *  Discard an object.  Typically not useful unless caching is implemented
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public void discard(String name) throws UniverseException;

	/**
	 *  Remove an object from the universe
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public void remove(String name) throws UniverseException;
	
	/**
	 *  Report the size object from the universe
	 * @param name universe name
	 * @return the size or 0 if empty
	 * @throws autohit.universe.UniverseException
	 */
	public long size(String name) throws UniverseException;	

}
