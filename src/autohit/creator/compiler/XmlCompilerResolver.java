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
package autohit.creator.compiler;

import java.io.StringReader;
import java.util.Hashtable;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

import autohit.common.AutohitLogInjectorWrapper;

/**
 * Implement our own resolver to handle XML activities.  
 * For the most part, this is used to provide DTDs.
 * <p>
 * The default constructor assumes you do not want to log.  If you use the other constructor, 
 * supply a valid CreatorLog and a log entry will be made for every resolve.
 * <p>
 * After construction, you need to register URIs to resolve.
 * If the resolver encounters any URIs that are not registered,
 * it will throw a SAXNotSupportedException exception.
 * 
 * In this version, we assume that are resources are Strings.
 * This should be easy to extend in the future.
 *
 * @author Erich P. Gatejen
 * @version 1.1
 * <i>Version History</i>
 * <code>EPG - Initial - 11Apr03</code> 
 * 
 */
public class XmlCompilerResolver implements EntityResolver {

	/**
	 * Contains the registered entities.  Leave this a Hashtable,
	 * since it should be syncronized.
	 */
	private Hashtable registeredEntities;

	private AutohitLogInjectorWrapper log;

	// --- PUBLIC METHODS ----------------------------------------------------	

	/**
	 *  Default constructor.
	 */
	XmlCompilerResolver() {
		log = null;
	}

	/**
	 *  Constructor.  This one sets a logger.
	 *
	 * @param cLog A reference to a valid, working CreatorLog.
	 * @see autohit.creator.CreatorLog
	 */
	XmlCompilerResolver(AutohitLogInjectorWrapper cLog) {
		log = cLog;
	}

	/**
	 *  Register a resource as an entity.  The entity is the system URI and *not* the public name It will store the resource in the hashtable keyed on the entity..
	 *
	 *  @param uri A string containing the textual entity to trap and resolve.
	 *  @param text A string containing actual resource.
	 */
	public void register(String uri, String text) {
		if (registeredEntities == null) {
			registeredEntities = new Hashtable();
		}
		registeredEntities.put(uri, text);
	}

	/**
	 *  overrides the resolver.
	 *
	 *  @param name not implimented.
	 *  @param uri Passed to this from the parser.  We will trap
	 *                  the uri.
	 *  @return an input source to be used by the XML parser.
	 */
	public InputSource resolveEntity(
		java.lang.String name,
		java.lang.String uri)
		throws SAXException, java.io.IOException {

		InputSource tis;

		if (log != null)
			log.debug(
				"Resolve Entity: public=[" + name + "] system=[" + uri + "]");
				
		// strip the "//" crap we've added via the InputSource
		uri = uri.substring(2, uri.length());
		
		// Is the entity registered?
		if (registeredEntities.containsKey(uri)) {

			tis =
				new InputSource(
					new StringReader((String) registeredEntities.get(uri)));

		} else {
			if (log != null)
				log.debug("        ERROR: but it is not registered!");
			throw new SAXNotSupportedException(
				"URI [" + uri + "] not registered");
		}

		return tis;
	}
}
