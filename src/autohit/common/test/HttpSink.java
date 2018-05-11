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
package autohit.common.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * A simple CLI 
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03 
 * 
 */
public class HttpSink {

	/**
	 * Globals
	 */

	/**
	 *  Help
	 */
	static public void help() {
		System.out.println("HttpSink outputfile");
		System.out.println("outputfile = name of file to send data.");
	}

	/**
	 * main interface
	 */
	public static void main(String[] args) {

// handle arguments
		if (args.length < 1) {
			System.out.println("No command");
			help();
			return;
		}

		try {

			String outputfile = args[0];
			BufferedWriter out = new BufferedWriter(new FileWriter(outputfile));
			ServerSocket ss = new ServerSocket(80);
			Socket s = ss.accept();
			System.out.println("Accepted");

			try {

				InputStream ins = s.getInputStream();
				BufferedReader in =
					new BufferedReader(new InputStreamReader(ins));
				String thang;

				thang = in.readLine();
				while (thang != null) {

					out.write(thang);
					thang = in.readLine();
				}

			} catch (Exception e) {
				System.out.println("Exception in socket read");
				e.printStackTrace();
			}

			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
