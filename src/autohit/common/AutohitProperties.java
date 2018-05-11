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
package autohit.common;

/**
 * Properties for configuration
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 9Apr03 
 * 
 */
public interface AutohitProperties {

	/**
	 * General system properties.
	 */
	public final static String SYSTEM_DEBUG = "debug";
	public final static String SYSTEM_WIRE_DEBUG = "wire";
	public final static String BOOTSTRAP_CONTEXT_CLASS = "bootstrap.context.class";

	/**
	 * ROOT path of install.
	 * Plus path modifier literals. 
	 */
	public final static String ROOT_PATH = "root";
	
	public final static String literal_DTD_PATH = "/lib/dtd";
	public final static char literal_PATH_SEPERATOR = '/';
	public final static char literal_NAME_SEPERATOR = Constants.ANAMESPACE_SEPERATOR;	

	/**
	 * LOG FILES
	 * type = the type of handler
	 * path = directory path to the logs; does not include log file name
	 */
	public final static String LOGS_CONTROL_STATION = "autohit.control.logger";
	public final static String LOGS_CLIENT_STATION = "autohit.client.logger";
	public final static String LOGS_CONTROL_INJECTOR = "autohit.control.injector.log";
	public final static String LOGS_CLIENT_INJECTOR = "autohit.client.injector.log";
	public final static String LOGS_CONTROL_DRAIN = "autohit.control.drain.log";
	public final static String LOGS_CLIENT_DRAIN = "autohit.client.drain.log";

	public final static String LOGS_PRETTY_PRINT = "log.pretty";
	public final static String LOGS_TIMESTAMP = "log.stamp";
	public final static String LOGS_LINE_LIMIT = "log.linelimit";
	public final static String LOGS_LINE_SIZE = "log.linesize";	
	public final static int LOGS_ARBITRARY_ENTRY_LIMIT_DEFAULT = 16000;
	public final static int LOGS_LINE_SIZE_DEFAULT = 120;

	public final static String LOGS_LOCATION_CONTROL = "log.control.location";
	public final static String LOGS_TYPE_CONTROL = "log.control.type";
	public final static String LOGS_LOCATION_CLIENT = "log.client.location";
	public final static String LOGS_TYPE_CLIENT = "log.client.type";
	public final static String LOGS_TYPE__FILE = "file";
	public final static String LOGS_TYPE__CONSOLE = "console";
	public final static String LOGS_TYPE__CLASS = "class";
	
	/**
	 * LOG IDs
	 */	
	public final static String LOGS_ROOT_ID = "AH";	 
	public final static String SYSTEM_GENERIC_ID = "!!";	
	public final static String SYSTEM_COMMANDCONTROL_ID = "cO";	
	public final static String SYSTEM_COMMANDRESPONSE_ID = "rE";	
	
	/**
	 * UNIVERSE
	 */
	public final static String DEFAULT_UNIVERSE_HANDLE = "universe.root.handle";
	public final static String DEFAULT_UNIVERSE_PATH = "universe.root.path";
	public final static String DEFAULT_UNIVERSE_PROP = "universe.root.config";	
	public final static String literal_DEFAULT_UNIVERSE_HANDLE = "root";
	public final static String literal_UNIVERSE_CACHE = "cache";
	public final static String literal_DEFAULT_UNIVERSE_PATH = "/universe";
	public final static String literal_DEFAULT_UNIVERSE_PROP = "universe.prop";
	
	/**
	 * COMMAND SYSTEM
	 */	
	public final static String COMMAND_DEFAULT_REGISTRY = "/etc/cmdregistry-builtin.prop"; 

	public final static String COMMAND_SERVER_STATION = "autohit.command";
	public final static String COMMAND_SERVER_DRAIN_NAME = "Command Server";
	public final static String COMMAND_SERVER_DRAIN = "autohit.command.drain";
	public final static String COMMAND_SERVER_INJECTOR = "autohit.command.injector";
	
	/**
	 * CONFIGURATION SYSTEM
	 */	
	public final static String vDEFAULT_CHECKPOINT = "all.config";
	public final static String vCONFIG_ROOT = "/etc/config/";
	public final static String vCONFIG_FILE = "config";
	public final static String vFACTORY_STORE = "factory";
	public final static String vKICK_STORE = "kick";
	public final static String vVAR_ROOT = "/etc/";
	public final static String vBIN_ROOT = "/bin/";
	public final static String vLOG_ROOT = "/log/";
	public final static String CONFIG_ROOT_CONFIG = "ROOT";
	public final static String CONFIG_JAVA_CONFIG = "JAVA_EXEC";
	
	// It this file is there, then assume the path is indeed an autohit root
	public final static String vKICK_VERIFICATION_FILE = "/bin/kick.bat";
	
	/**
	 * FILESYSTEM RELATED
	 */	
	public final static String literal_FS_DUMP_EXTENSION =".dmp";
	public final static String literal_FS_LOG_EXTENSION =".log";	

	/**
	 * SERVICES
	 */	
	public final static String SERVICE_SOCKETRELAY_DESTINATION_ADDR = "service.socketrelay.destination.addr";
	public final static String SERVICE_SOCKETRELAY_DESTINATION_PORT = "service.socketrelay.destination.port";
	public final static String SERVICE_SOCKETRELAY_DESTINATION_ADDR_default = "127.0.0.1";
	public final static String SERVICE_SOCKETRELAY_WIRELOGGING = "service.socketrelay.wire";
	public final static int SERVICE_SOCKETRELAY_DESTINATION_PORT_default = 25;

	public final static String SERVICE_SOCKETRELAY_SERVER_PORT = "service.socketrelay.server.port";
	public final static String SERVICE_HTTPCOMMAND_SERVER_PORT = "service.httpcommand.server.port";
	
	public final static int default_SOCKETRELAY_SERVER_PORT = 6060;
	public final static int default_HTTPCOMMAND_SERVER_PORT = 6061;

}

 
