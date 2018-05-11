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
package autohit.universe.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.activation.DataSource;
import javax.activation.FileDataSource;

import autohit.common.AutohitProperties;
import autohit.common.Utils;
import autohit.universe.Universe;
import autohit.universe.UniverseDataSource;
import autohit.universe.UniverseException;
import autohit.universe.UniverseProperties;

/**
* Universe server implimentation for a local filesystem.  This
* does NOT implement caching!  Every object is unique.
* 
* The 'root' property should be a path to the root of the 
* universe on the filesystem.  It should begin with a '/', and
* should have no trailing slashes.
* 
* This universe assumes that storable objects can be serialized completely.
* If you plan on moving data that isn't in objects or are not inherently
* able to seriously completely, then you should obtain streams for put and get, and handle
* the IO yourself.
*
* @author Erich P. Gatejen
* @version 1.0
* <i>Version History</i>
* <code>EPG - New - 24Apr03</code> 
* 
*/
public class UniverseLocal implements Universe {

	private UniverseProperties myProp;
	private String root;
	private Random relement;

	/**
	 * Impliment the genesis.
	 * 
	 * @param props a universe properties set
	 * @throws autohit.universe.UniverseException
	 * @return the loaded object
	 */
	public void genesis(UniverseProperties props) throws UniverseException {

		// Save the properties
		myProp = props;
		
		// Get random element
		relement = new Random();

		// cleanse the root
		root = myProp.getRoot().trim();
		if ((root.length() < 1)
			|| (root.charAt(0) != AutohitProperties.literal_PATH_SEPERATOR)) {
			throw new UniverseException(
				"Bad root.  Either empty or doesn't start with /.",
				UniverseException.UE_MALFORMED_REFERENCE);
		}
		try {
			while (root.charAt(root.length() - 1)
				== AutohitProperties.literal_PATH_SEPERATOR) {
				root = root.substring(0, root.length() - 2);
			}
		} catch (Exception e) {
			throw new UniverseException(
				"Malformed root property =" + myProp.getRoot(),
				UniverseException.UE_MALFORMED_REFERENCE, e);
		}
	}

	/**
	 * This will always be called when the universe is destroyed
	 * @throws autohit.universe.UniverseException
	 */
	public void close() throws UniverseException {
		// Don't do anything
	}

	/**
	 *  Load an object from the universe.  It will be unique.
	 *  There is no caching. 
	 * @param name universe name
	 * @return the loaded object
	 * @throws autohit.universe.UniverseException
	 */
	public Object get(String name) throws UniverseException {
		Object thing = null;

		try {
			// construct the path
			File target = new File(root + AutohitProperties.literal_PATH_SEPERATOR + name);
			if (target.exists() == false) {
				throw new UniverseException(
					"No object =" + name,
					UniverseException.UE_OBJECT_DOESNT_EXIST);
			}
			// deserialize it
			FileInputStream istream = new FileInputStream(target);
			ObjectInputStream p = new ObjectInputStream(istream);
			thing = p.readObject();
			istream.close();

		} catch (UniverseException e) {
			throw e;
		} catch (Exception e) {
			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error on object get.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
		return thing;
	}

	/**
	 *  Same as get, so just chain it.
	 * @param name universe name
	 * @return the loaded object
	 * @throws autohit.universe.UniverseException
	 */
	public Object getUnique(String name) throws UniverseException {
		return get(name);
	}

	/**
	 *  Reserve unique object universe.  Guarantee it is a unique
	 *  instance of it.  Great for temp objects.
	 * @param base base path for the object (including root object name)
	 * @return name of the reserved unique object.
	 * @throws autohit.universe.UniverseException
	 * TODO make sure the object can actually be used
	 */
	public synchronized String reserveUnique(String base) throws UniverseException {

		// Cludge hack!
		String name = base + "-" + System.currentTimeMillis() + "-" + relement.nextInt(1000000);
		
		// Check to see if it is unique.  Since this is sychroninzed
		// any wait will guarentee the new name is unique.
		try {
			File target = new File(root + AutohitProperties.literal_PATH_SEPERATOR + name);
			if (target.exists() == true) {
				this.wait(1);
				name = base + "-" + System.currentTimeMillis() + "-" + relement.nextInt(1000000);
			}
		} catch (Exception io) {
			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error on object.  Message=" + io.getMessage(),
				UniverseException.UE_IO_ERROR, io);			
		}
		return name;
	}
	
	/**
	 * Get an InputStream that can read from a universe object dump.
	 * It does not assume the target is anything--object, bytes, whatever.
	 * @param name universe name
	 * @return a stream to the object
	 * @throws autohit.universe.UniverseException
	 */
	public InputStream getStream(String name) throws UniverseException {
		InputStream thing = null;

		try {
			// construct the path
			File target = new File(root + AutohitProperties.literal_PATH_SEPERATOR + name);
			if (target.exists() == false) {
				throw new UniverseException(
					"No object =" + name,
					UniverseException.UE_OBJECT_DOESNT_EXIST);
			}
			// get a stream reference to it
			thing = new FileInputStream(target);

		} catch (UniverseException e) {
			throw e;
		} catch (Exception e) {
			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error on object.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
		return thing;
	}

	/**
	 *  Get a Data Source that can interact with this universe object
	 * @param name universe name
	 * @return a data source
	 * @throws autohit.universe.UniverseException
	 * @throws autohit.universe.UniverseDataSource
	 */
	public DataSource getDataSource(String name) throws UniverseException {
		
		UniverseDataSource uds = new UniverseDataSource();
		uds.init(name,this);
		return uds;
	}
	
	/**
	 * Get a FileDataSource that can interact with this universe object.
	 * The universe must handle making sure the object at least appears local to the
	 * FileDataSource 
	 * @param name universe name
	 * @return a data source
	 * @throws autohit.universe.UniverseException
	 */
	public FileDataSource getFileDataSource(String name) throws UniverseException {
	    
	    //This one is easy.  Just get the local file.
	    FileDataSource candidate = null;
	    
		try {
			// construct the path
			File target = new File(root + AutohitProperties.literal_PATH_SEPERATOR + name);
			if (target.exists() == false) {
				throw new UniverseException(
					"No object =" + name,
					UniverseException.UE_OBJECT_DOESNT_EXIST);
			}
			// deserialize it
			candidate = new FileDataSource(target);
			
		} catch (UniverseException e) {
			throw e;
		} catch (Exception e) {
			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error while getting FileDataSource.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
		return candidate;
	    
	}


	/**
	 * Save an object into the universe.  If a file is already there,
	 * it will overwrite it.
	 * @param name universe name
	 * @param o the object
	 * @throws autohit.universe.UniverseException
	 */
	public void put(String name, Object o) throws UniverseException {

		try {
			// construct the path
			File target = Utils.makeFile(root + AutohitProperties.literal_PATH_SEPERATOR + name);

			// deserialize it
			FileOutputStream ostream = new FileOutputStream(target);
			ObjectOutputStream sobj = new ObjectOutputStream(ostream);
			sobj.writeObject(o);
			sobj.flush();
			ostream.close();

		} catch (Exception e) {
			// Every other exception should be consider an IO error

			throw new UniverseException(
				"IO Error on object put.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
	}

	/**
	 *  Get an output stream to a universe object.  CAller responsible
	 * for streaming and closing.
	 * @param name universe name
	 * @return a stream to the object
	 * @throws autohit.universe.UniverseException
	 */
	public OutputStream putStream(String name) throws UniverseException {
		FileOutputStream tempOS = null;

		try {
			// construct the path
			File target = Utils.makeFile(root + AutohitProperties.literal_PATH_SEPERATOR + name);

			// deserialize it
			tempOS = new FileOutputStream(target);

		} catch (Exception e) {

			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error on object put.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
		return (OutputStream) tempOS;
	}

	/**
	 *  There is no locking.  Always immeadiately return.
	 * @param name universe name
	 * @throws autohit.universe.UniverseException
	 */
	public void lock(String name) throws UniverseException {
		// do nothing
	}

	/**
	 *  There is no locking, so return true, since the caller is
	 * always allowed to get an object.
	 * @param name universe name
	 * @return always true
	 * @throws autohit.universe.UniverseException
	 */
	public boolean lockIfNotLocked(String name) throws UniverseException {
		return true;
	}

	/**
	 *  Since objects can never be locked, this will always return false.
	 * @param name universe name
	 * @return always false
	 * @throws autohit.universe.UniverseException
	 */
	public boolean isLocked(String name) throws UniverseException {
		return false;
	}

	/**
	 *  Release a lock on an object.  Do nothing.
	 * @param name universe name
	 * @throws autohit.universe.UniverseException
	 */
	public void release(String name) throws UniverseException {
		// do nothing
	}

	/**
	 *  Check to see if an object exists
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public boolean exists(String name) throws UniverseException {
		// assume it doesn't exist
		boolean answer = false;

		try {
			// Check for the file
			File target = new File(root + AutohitProperties.literal_PATH_SEPERATOR + name);
			if (target.exists() == true) {
				answer = true;
			}

		} catch (Exception e) {
			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error on object check.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
		return answer;
	}

	/**
	 *  Flush an object.  Does nothing, since there is no caching.
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public void flush(String name) throws UniverseException {
		// do nothing
	}

	/**
	 *  Discard an object.  Does nothing, since there is no caching.
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public void discard(String name) throws UniverseException {
		// do nothing.
	}

	/**
	 *  Remove an object from the universe
	 * @param name universe name
	 * @return true if the object exists, otherwise false
	 * @throws autohit.universe.UniverseException
	 */
	public void remove(String name) throws UniverseException {

		try {
			// Check for the file
			File target = new File(root + AutohitProperties.literal_PATH_SEPERATOR + name);
			if (target.exists() == true) {
				// object is there.  remove it
				target.delete();
			} else {
				// object doesn't exist.  error.
				throw new UniverseException(
					"No object =" + name,
					UniverseException.UE_OBJECT_DOESNT_EXIST);
			}

		} catch (Exception e) {
			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error on object remove.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
	}

	/**
	 *  Report the size object from the universe
	 * @param name universe name
	 * @return the size or 0 if empty
	 * @throws autohit.universe.UniverseException
	 */
	public long size(String name) throws UniverseException {

	    long size = 0;
		try {
			// Check for the file
			File target = new File(root + AutohitProperties.literal_PATH_SEPERATOR + name);
			if (target.exists() == true) {
				// object is there.  remove it
				size = target.length();
			} else {
				// object doesn't exist.  error.
				throw new UniverseException(
					"No object =" + name,
					UniverseException.UE_OBJECT_DOESNT_EXIST);
			}

		} catch (Exception e) {
			// Every other exception should be consider an IO error
			throw new UniverseException(
				"IO Error on object remove.  Message=" + e.getMessage(),
				UniverseException.UE_IO_ERROR, e);
		}
		return size;
	}

	
}
