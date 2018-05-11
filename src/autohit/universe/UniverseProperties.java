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

import org.apache.commons.collections.ExtendedProperties;

/**
* Universe properties set.
* 
* There are four types of universes:
*	UNI_LOCAL   	Local file system
*	UNI_MASTER 		NOT IMPLIMENTED!
*	UNI_MIRROR		NOT IMPLIMENTED!
*	UNI_REMOTE		NOT IMPLIMENTED!
*   UNI_EXTENDED	NOT IMPLIMENTED!
*
* Regardless of what kind of universe, there must be a local property file
* that describes the universe.  The factory will use it to build a
* server for that universe.
*
* PROPERTIES PROCESSED
* name		: string name discriptor
* type		: "local" only now
*
* @author Erich P. Gatejen
* @version 1.0
* <i>Version History</i>
* <code>EPG - New - 23Apr03</code> 
* 
*/
public class UniverseProperties {

	/**
	 * Types of universe
	 */
	public static final int UNI_INVALID = 0;
	public static final int UNI_LOCAL = 1;
	public static final int UNI_MASTER = 2;
	public static final int UNI_MIRROR = 3;
	public static final int UNI_REMOTE = 4;
	public static final int UNI_EXTENDED = 5;

	/**
	 * type of universe
	 */
	private int type;

	/**
	 * name of universe
	 */
	private String name;

	/**
	 * name of universe
	 */
	private String root;

	/**
	 * name of extended class
	 */
	private String extendedClass;

	/**
	 * The internal properties set
	 */
	private ExtendedProperties prop;

	/**
	 *  Constructor.  Do not allow this!
	 */
	public UniverseProperties() throws Exception {
		throw new Exception("BAD PROGRAMMER!  Don't use me--ever.");
	}

	/**
	 *  Constructor.  Build from a file.  We will propagate any exception.
	 * @param propsPath path to properties file
	 */
	public UniverseProperties(String propsPath) throws Exception {
		try {
			prop = new ExtendedProperties(propsPath);
			completeConstruction();
		} catch (Exception e) {
			// FUBAR.  kill the properties.
			prop = null;
			throw e;
		}
	}

	/**
	 *  Constructor.  Build from an input stream.  We will propagate any exception.
	 * @param props input stream to the properties file
	 */
	public UniverseProperties(InputStream props) throws Exception {
		try {
			prop = new ExtendedProperties();
			prop.load(props);
			completeConstruction();
		} catch (Exception e) {
			// FUBAR
			prop = null;
			throw e;
		}
	}

	/**
	 *  Get type accessor
	 * @return type
	 */
	public int getType() {
		return type;
	}

	/**
	 *  Get name accessor
	 * @return type
	 */
	public String getName() {
		return name;
	}

	/**
	 *  Get root accessor
	 * @return type
	 */
	public String getRoot() {
		return root;
	}

	/**
	 *  Get extended class
	 * @return extended class string 
	 */
	public String getExtendedClass() {
		return extendedClass;
	}

	/**
	 *  Helper
	 */
	private void completeConstruction() throws Exception {
		String temp;

		// Process type
		if (!prop.containsKey("type")) {
			throw new UniverseException(
				"type property missing",
				UniverseException.UE_REQUIRED_PROPERTY_MISSING);
		}
		temp = prop.getString("type");
		if (temp.startsWith("local")) {
			type = UNI_LOCAL;
		} else {
			type = UNI_INVALID;
			throw new UniverseException(
				"type " + temp + " not supported in this version.",
				UniverseException.UE_NOT_SUPPORTED);
		}

		// Process the name
		if (!prop.containsKey("name")) {
			throw new UniverseException(
				"name property missing",
				UniverseException.UE_REQUIRED_PROPERTY_MISSING);
		}
		name = prop.getString("name");

		// Process the root
		if (!prop.containsKey("root")) {
			throw new UniverseException(
				"root property missing",
				UniverseException.UE_REQUIRED_PROPERTY_MISSING);
		}
		root = prop.getString("root");

		// Process the root
		if (!prop.containsKey("root")) {
			throw new UniverseException(
				"root property missing",
				UniverseException.UE_REQUIRED_PROPERTY_MISSING);
		}
		root = prop.getString("root");
	}

}
