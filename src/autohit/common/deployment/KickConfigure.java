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
package autohit.common.deployment;

import java.io.File;
import org.apache.commons.collections.ExtendedProperties;
import autohit.common.AutohitProperties;

/**
 * A deployment manager.  It will handle configuration, installation, and checkpointing.
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03 
 * 
 */
public class KickConfigure {

	/**
	 * Command system
	 */
	public ExtendedProperties props;

	/**
	 *  Default constructor. 
	 */
	public KickConfigure() {
	}

	/**
	 *  Object main
	 */
	public void go(String it, String javaexec) {

		try {
			DeploymentConfigure dc = new DeploymentConfigure();

			// validate it
			File f = new File(it + AutohitProperties.vKICK_VERIFICATION_FILE);
			if (!f.exists()) {
				help();
				return;
			}

			props = new ExtendedProperties();
			props.put(AutohitProperties.CONFIG_ROOT_CONFIG, it);
			props.put(AutohitProperties.CONFIG_JAVA_CONFIG, javaexec);
			dc.configure(AutohitProperties.vKICK_STORE, it, props);
		} catch (Exception e) {
			System.out.println("Catastrophic ERROR.  System is likely corrupt.");
			System.out.println("The error is= " + e.getMessage());
			return;

		}
	}

	/**
	 * help
	 */
	public static void help() {
		System.out.println("KickConfigure.call AUTOHIT_ROOT JAVA_EXEC");
		System.out.println("You MUST call this with two parameters, no more or less.");
		System.out.println("AUTOHIT_ROOT : The root of the install.");
		System.out.println("JAVA_EXEC    : Path to a java executable.  It should be qualified, if not in the path.");	}

	/**
	 * main interface
	 */
	public static void main(String[] args) {

		// handle arguments
		if (args.length != 2) {
			help();
			return;
		}

		try {
			KickConfigure me = new KickConfigure();
			me.go(args[0], args[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}