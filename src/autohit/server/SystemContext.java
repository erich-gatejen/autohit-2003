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
package autohit.server;

import org.apache.commons.collections.ExtendedProperties;

import autohit.common.AutohitBasicLogManager;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.common.channels.Injector;
import autohit.creator.compiler.XmlCompiler;
import autohit.universe.Universe;
import autohit.vm.VMLoader;
import autohit.server.command.CommandRegistry;

/**
 * A system context interface.  A context contains the cummulative knowledge
 * of a system.  It will return references to various subsystems.  All the
 * subsystems should be thread safe, though only the core and universe provide
 * methods and services specifically intended for IPC communication.
 * <p><pre>
 * Each will have references to:
 *  - A system properties set
 *  - A universe factory
 *  - A default universe
 *  - A root logger and log manager
 *  - A uninitialized Kernel
 *  - A uninitialized root loader
 *  - An invoker properties set
 * </pre>
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03 
 * EPG - Add the invoker properties - 30Jul03</code>
 */
public interface SystemContext {

	/**
	 * Initialize using a set of properties.  It will destroy any
	 * previous context.  It will build the components as specified
	 * in the passed properties.
	 * @param props properties set
	 */
	public void init(ExtendedProperties props) throws Exception;

	/**
	 *  Load properties.  It will only overwrite duplicate properties.
	 *  It will not change services/components build during an init()!
	 * @param props properties set
	 */
	public void loadProperties(ExtendedProperties props) throws Exception;

	/**
	 *  Get the default universe.  Return null if not available.
	 * @return Universe service interface
	 */
	public Universe getUniverse();

	/**
	 *  Get a universe service by handle.  Return null if not available.
	 *  What a handle means is determined by the implemementing class.
	 * @param handle handle to the universe
	 * @return Universe service interface
	 */
	public Universe getUniverse(String handle);

	/**
	 *  Get the XML compiler.  Return null if not available.
	 * @return XmlCompiler base class
	 */
	public XmlCompiler getCompiler();

	/**
	 *  Get a reference to a generic, root log injector
	 * @return XmlCompiler base class
	 */
	public AutohitLogInjectorWrapper getRootLogger();

	/**
	 *  Get event dispatcher injector
	 * @return Injector reference
	 */
	public Injector getEventDispatcher();

	/**
	 *  Get log manager
	 * @return reference to the main log manager
	 */
	public AutohitBasicLogManager getLogManager();

	/**
	 *  Get properties set.  Return null if not available.
	 * @return reference to the properties set
	 */
	public ExtendedProperties getPropertiesSet();

	/**
	 *  Get the Kernel
	 * @return reference to the kernel
	 */
	public Kernel getKernel();

	/**
	 *  Get the VM Loader
	 * @return reference to the kernel
	 */
	public VMLoader getLoader();

	/**
	 *  Get the command registry.  It is up to the implimentor as how to do this.  It is not cached.
	 * @return a command registry instance
	 */
	public CommandRegistry getCommandRegistry();

	/**
	 *  Unique number
	 * @return an integer number unique (at least) to this Context
	 */
	public int uniqueInteger();

	/**
	 *  Get debugging state
	 * @return true if debugging active
	 */
	public boolean debuggingState();
	
	/**
	 * Get a reference to the invoker properties set.   Generally, only
	 * invokers should add anything to the set.  It should be safe for anyone
	 * to read from it.  It is up to the invoker to maintain its contents.
	 * @return reference to the invoker properties set
	 */
	public ExtendedProperties getInvokerProperties();

}
