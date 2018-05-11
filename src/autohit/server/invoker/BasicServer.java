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
package autohit.server.invoker;

import autohit.server.BasicBootstrap;
import autohit.server.service.CLIService;
import autohit.server.service.CommandService;
import autohit.server.service.HttpCommandService;
import autohit.server.service.SocketRelayService;
import autohit.vm.VMProcess;

/**
 * A very basic server context
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History</i><p>
 * <code>EPG - Initial - 16Sep03<br>
 * EPG - Add HTTP command service - 5Apr05
 * </code>
 */
public class BasicServer extends BasicBootstrap {

	/*
	 * Command system 
	 */
	public SimTextCommand cmd;
	
	/*
	 * Server mode
	 */
	public boolean serverMode;

	/**
	 *  Default constructor. */
	public BasicServer() throws Exception {
		throw new Exception("Dont use the default constructor!");
	}

	/**
	 *  Properties constructor. Give it a full path to the properties file. */
	public BasicServer(String rootProps) throws Exception {

		// CRITICAL TO CALL SUPER
		super(rootProps);
	}

	/**
	 *  Object main */
	public void go() throws Exception {

		SocketRelayService srs = new SocketRelayService();
		VMProcess srsp;
		CommandService cs = new CommandService();
		VMProcess csp;
		CLIService cli = new CLIService();
		VMProcess clip = null;
		HttpCommandService hc = new HttpCommandService();
		VMProcess hcp = null;
		
		try {

			// START SERVICES
			srs.loadcontext(sc);
			srs.init(sc.getRootLogger().sinjector, "SocketRelayService");
			srsp = this.runService(srs);

			cs.loadcontext(sc);
			cs.init(sc.getRootLogger().sinjector, "CommandService");
			csp = this.runService(cs);

			if (serverMode) {
				hc.loadcontext(sc);
				hc.init(sc.getRootLogger().sinjector, "HttpCommandService");
				hcp = this.runService(hc);
			} else {
				cli.loadcontext(sc);
				cli.init(sc.getRootLogger().sinjector, "CLIService");
				clip = this.runService(cli);
			}
			
		} catch (Exception e) {
			System.out.println("BasicServer startup failed due to exception.  message=" + e.getMessage());
			throw e;
		}

		// Server loop
		//synchronized(this) {wait(1000); };
		//while (true){
		//	if ((clip.getState() < VM.STATE_ACTIVE_THRESHOLD)||(hc.getState() < VM.STATE_ACTIVE_THRESHOLD)) break;
		//	synchronized(this) {wait(5000); };
		//}
		
		// Die
		if (serverMode) {
			hcp.joinIt();
			System.out.println("HttpCommandService down.");
		} else {
			clip.joinIt();
			System.out.println("CLIService down.");		
		}
		System.out.println("Starting shutdown.");	
		srsp.kill();
		srsp.joinIt();
		System.out.println("SocketRelayService down.");
		csp.kill();
		csp.joinIt();	
		System.out.println("CommandService down.");	

		System.out.println("Goodbye.");
	}

	/**
	 * main interface */
	public static void main(String[] args) {

		// handle arguments
		if (args.length == 0) {
			System.out.println("No .prop specified");
			return;
		}
		
		try {
			BasicServer me = new BasicServer(args[0]);
			
			if ((args.length > 1)&&(args[1].toLowerCase().equals("server"))) {
				me.serverMode = true;
				System.out.println("Starting in server mode.");
			}
			
			me.go();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
