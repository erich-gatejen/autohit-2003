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

import autohit.common.AutohitLogDrainDefault;
import autohit.common.channels.Atom;
import autohit.common.Utils;

import java.io.File;

/**
 * A simple CLI 
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 25Apr03 
 * 
 */
public class TestLogFormatting {

	/**
	 * Globals
	 */

	/**
	 *  Help
	 */
	public void help() {
		System.out.println("LOG TESTER");
		System.out.println("t inputfile = pretty print with timestamp");
		System.out.println("n inputfile = pretty print without timestamp");
		System.out.println("?           = show this help");
	}
	
	/**
	 *  Object main
	 */
	public void ppWithT(String it) throws Exception {

		File f = new File (it);
		String thang = Utils.loadFile2String(f);
		
		System.out.println("THANG ------------------- ");
		System.out.println(thang);
		System.out.println("------------------------- ");
		
		Atom a = new Atom(1,1,1,thang);
		a.senderID = "id";
		AutohitLogDrainDefault d = new AutohitLogDrainDefault();
		d.setPrettyFlag(true);
		d.setTimestampFlag(true);
		d.post(a);
	}

	/**
	 *  Object main
	 */
	public void ppWithNT(String it) throws Exception {

		File f = new File (it);
		String thang = Utils.loadFile2String(f);
		
		System.out.println("THANG ------------------- ");
		System.out.println(thang);
		System.out.println("------------------------- ");
		
		Atom a = new Atom(1,1,1,thang);
		a.senderID = "id";
		AutohitLogDrainDefault d = new AutohitLogDrainDefault();
		d.setPrettyFlag(true);
		d.setTimestampFlag(false);
		d.post(a);
	}

	/**
	 * main interface
	 */
	public static void main(String[] args) {

		// handle arguments
		if (args.length < 1) {
			System.out.println("No command");
			return;
		}
		
		try {
			TestLogFormatting me = new TestLogFormatting();
			char cmd = args[0].charAt(0); 
			switch (cmd) {
	
				case 't' :
				case 'T' : if (args.length < 2) {
								System.out.println("BAD.  No input file given.");
								System.out.println("params= " + args[0]);
							} else {
								me.ppWithT(args[1]); 
							}
							break;
				case 'n' :
				case 'N' : if (args.length < 2) {
								System.out.println("BAD.  No input file given.");
								System.out.println("params= " + args[0]);
							} else {
								me.ppWithNT(args[1]); 
							}
							break;						
				case '?' : 
				default  : me.help(); break;		
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
