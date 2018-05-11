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

import autohit.common.AutohitProperties;
import autohit.common.Utils;
import java.util.HashSet;
import autohit.server.service.Service;
import autohit.vm.VMProcess;

/**
 * Bootstrap the System Context
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 19Jul03</code> 
 */
public class BasicBootstrap {

	/**
	 * System
	 */
	public SystemContext sc;

	/**
	 * Services loaded (as VMProcess')
	 */
	public HashSet services;

	/**
	 *  Default constructor. 
	 */
	public BasicBootstrap() throws Exception {
		throw new Exception("Dont use the default constructor!");
	}

	/**
	 * Properties constructor.  Give it a full path to the 
	 * properties file.
	 * @param rootProps path to properties for System Context
	 */
	public BasicBootstrap(String rootProps) throws Exception {

		// Build the properties
		ExtendedProperties ep = new ExtendedProperties(rootProps);

		// Build a context
		String logprop =
			(String) Utils.testGetProperty(
				AutohitProperties.BOOTSTRAP_CONTEXT_CLASS,
				ep);
		if (logprop == null) {
			throw new Exception("SystemContext class for bootstrap not specified in properties.");
		}
		try {
			Class t = Class.forName(logprop);
			sc = (SystemContext) t.newInstance();
		} catch (Exception ec) {
			throw new Exception(
				"Specified SystemContext class for bootstrap not found.  class name="
					+ logprop
					+ ".  message="
					+ ec.getMessage());
		}
		sc.init(ep);
		
		// Services set
		services = new HashSet();
	}
	
	/**
	 *  Run service
	 */
	public VMProcess runService(Service s) throws Exception {
		
		// build the process and unleash
		VMProcess pcb = (sc.getKernel()).get();
		pcb.execute(s);

		services.add(pcb);

		sc.getRootLogger().info("Service " + s.rootProgram +  " started with session name=" + s.sname);
		
		return pcb;
	}
	
}
