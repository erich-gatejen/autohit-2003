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
package autohit.server.service;

// import java.io.BufferedInputStream;
// import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.StringReader;
import java.net.Socket;

import autohit.common.AutohitErrorCodes;
import autohit.common.AutohitException;
import autohit.common.AutohitLogInjectorWrapper;
import autohit.common.channels.ChannelException;
import autohit.common.channels.Injector;
import autohit.server.ServerException;
import autohit.server.command.CommandAtom;
import autohit.server.invoker.SimTextCommand;

/**
 * Http Command Helper.<br>
 * GET /command?param1&param2&paramN<br>
 * The ? and & are interchangable.
 * <p>
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i> 
 * <code>EPG - Initial - 05Apr05</code>
 */
public class HttpCommandHelper extends Thread {

	final private static int BUFFER_SIZE = 512;

	/**
	 * Socket connection
	 */
	public Socket connection;
	
	private DataInputStream is;
	private DataOutputStream os;
	private Injector commandInjector;
	private SimTextCommand commander;
	private HttpCommandService callingService;

	/**
	 * Logging mechinism
	 */
	public AutohitLogInjectorWrapper myLog;

	/**
	 * Default constructor
	 */
	public HttpCommandHelper() {
		super();
	}

	/**
	 * Complete construction. This will be called when the VM is initialized.
	 */
	public void init(Socket ins, AutohitLogInjectorWrapper logger, Injector cin, SimTextCommand c, HttpCommandService caller) {
		connection = ins;
		myLog = logger;
		callingService =  caller;
		try {
			is = new DataInputStream(ins.getInputStream());
			os = new DataOutputStream(ins.getOutputStream());
			commandInjector = cin;
			commander = c;

		} catch (Exception eee) {
			myLog.error(
					"HttpCommandHelper init() failed.  Something bad will happen.  message="
							+ eee.getMessage(),
					AutohitErrorCodes.CODE_SERVICE_GENERAL_FAULT);
		}
	}

	/**
	 * Run the context
	 */
	public void run() {

		String input;
		String path;
		String result = "SOFTWARE BUG!  You should never see this in HttpCommandHelper.run()";
		try {

			// pull request line
			input = is.readLine();
			input = input.trim();
			int spot = input.indexOf(' ');
			
			// Validate
			if (spot < 0) {
		         throw new Exception("Invalid HTTP Request Line");
		    }	
			
			// Second
			input = input.substring(spot).trim();
			spot = input.indexOf(' ');
		      
			// Split
			if (spot < 0) {
		       // No protocol.  Must be old protocol
		       path = input;
			} else {
				path = input.substring(1, spot);
			}
			
			// Decode and build command
			String command = decodePath(path);
			CommandAtom a = commander.create(command);

			// loop if no command
			if (a == null) throw new Exception("Bad command.  Caused a null.");

			// dispatch it
			commandInjector.post(a);
			
			// Report OK
			result = "PASS: Command accepted";
			myLog.info(result,AutohitException.CODE_INFORMATIONAL_OK);

		// } catch (InterruptedException ie) {

		} catch (ServerException se) {
			if (se.numeric==AutohitErrorCodes.CODE_SERVER_DONE) {
				// Signal exiting
				callingService.die();
			} else {
				result = "FAIL: HttpCommandService Server HALTED because of serious issue.  message=" + se.getMessage();
				myLog.info(result,AutohitException.CODE_SERVICE_PANIC);
			}
			// We got the exit command
			
		} catch (ChannelException e) {
			result = "FAIL: HttpCommandService Server HALTED because there is no command service running or command channel available.  This may be intentional during a shutdown.";
			myLog.info(result,AutohitException.CODE_SERVICE_PANIC);
			
		} catch (Throwable e) {
			result = "FAIL: Error processing HTTP Request: " + e;
			myLog.info(result,AutohitException.CODE_SERVER_IO_ERROR);
			
		} finally {
			try {
				respond(result);
				os.close();
				connection.close();
			} catch (Throwable e) {
				// Bad place here
			}
		}
	}

	public void respond(String  content) throws Throwable {

		println("HTTP 200 OK");
        println("Server: HttpCommandService");
        println("Content-Type: text/html");
        println("Content-Length: " + content.length());
        println("Accept-ranges: bytes");
        println("");
        print(content);
        try { 
        	Thread.sleep(100); 
        } catch (Exception e) {
        	// dont care
        }
	}
	
	private void println(String text) throws Exception {
		print(text);
		os.write('\n');
	}
   
	private void print(String text) throws Exception {
   		byte bytes[] = text.getBytes();
		os.write(bytes);
	}

	/*
	 * Return as Command->param1->paramN
	 */
	private static String decodePath(String s) throws Throwable {
		
		int value;
		String candidate;
		
		StringReader rin = new StringReader(s);
		StringBuffer accumulator = new StringBuffer();
		
		int current = rin.read();
		while (current >= 0) {
			
			// escape it
			if (current=='%') {
				current = rin.read();
				if (current < 0) throw new Exception("Uncompleted escaped character in URL");
				value = Character.digit((char)current,16);
				value = value << 4;
				current = rin.read();
				if (current < 0) throw new Exception("Uncompleted escaped character in URL");
				current = value + Character.digit((char)current,16);
			}
			
			// process it
			if ((current=='?')||(current=='&')) {
				accumulator.append(' ');
			} else {
				accumulator.append((char)current);
			}
			
			//DO NOT EDIT BELOW
			current = rin.read();
		}
		
		return (accumulator.toString());   
	}   

}
