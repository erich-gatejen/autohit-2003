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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import org.apache.commons.collections.ExtendedProperties;

/**
 * A bunch of utils
 *
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Rewrite - 9Apr03 
 * 
 */
public class Utils {
	
	// internal state values
	private final static int STATE_FRESH = 0;
	private final static int STATE_FRONT = 1;
	private final static int STATE_READ  = 2;
	private final static int STATE_TAIL  = 3;
	// Again.  I can't beleive I have to do this.  This MAY break in future JDKs.
	// It seems that BufferedReader.read() does NOT return -1 for an empty string; it
	// returns the freaking 2scompliment of -1.  
	// (2003-May: This may work now.  I'm too lazy to check it.)
	private final static int MORONIC_TOP_VALUE = 65534;

	/**
	 *  Converts dot namespace to a path
	 * @param header prefix for the path
	 * @param dot path in dot notation
	 * @return a converted path
	 */
	public static String dot2path(String header, String dot) {
		return header
			+ AutohitProperties.literal_PATH_SEPERATOR
			+ dot.replace(
				AutohitProperties.literal_NAME_SEPERATOR,
				AutohitProperties.literal_PATH_SEPERATOR);
	}

	/**
	 *  Make a file reference.  If the file already exists, kill it.
	 *  If the path doesn't exist, make it.  It'll throw an exception if
	 * it fails.
	 * @param name path name
	 * @return file object
	 * @throws Exception
	 */
	public static File makeFile(String name) throws Exception {

		File dir;
		String dirpath;
		int last;

		// construct the path
		File target = new File(name);

		if (target.exists()) {
			target.delete();
		} else {
			// futz with the directory and make it is neccessary
			last = name.lastIndexOf(AutohitProperties.literal_PATH_SEPERATOR);
			if (last > 1) {
				dirpath = name.substring(0, last);
				dir = new File(dirpath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
		}
		return target;
	}

	/**
	 *  Test and get property.  Return null if not found or something went wrong.
	 *  Traps all exceptions
	 * @param key key for the property
	 * @param props the property set
	 * @return the property object
	 */
	public static Object testGetProperty(
		String key,
		ExtendedProperties props) {
		Object thing = null;
		try {
			thing = props.getProperty(key);
		} catch (Exception e) {
			thing = null;
		}
		return thing;
	}

	/**
	 *  Test and get property for truth.  If will return true if the property
	 *  containts 'true', otherwise it will return false;
	 *  Traps all exceptions
	 * @param key key for the property
	 * @param props the property set
	 * @return true if the proprerty is 'true', otherwise false.
	 */
	public static boolean testGetPropertyTruth(
		String key,
		ExtendedProperties props) {
		boolean thing = false;
		String thang;
		try {
			thang = (String)props.getProperty(key);
			thing = thang.equals(Constants.TRUE);
		} catch (Exception e) {
		}
		return thing;
	}
	
	/**
	 *  Copy files.  I can't believe I have to write this.  It will always
	 *  overwrite an existing file.
	 * @param source source file path
	 * @param dest destination file path
	 * @return string containing errors or messages
	 */
	public static String copy(String source, String dest) {

		String overwrite = " ";

		BufferedOutputStream bos = null;
		try {

			File destf = new File(dest);
			if (destf.exists()) {
				destf.delete();
				overwrite = " [overwrite] ";
			}

			byte[] buf = new byte[1024];
			int sbuf;

			BufferedInputStream bis =
				new BufferedInputStream(new FileInputStream(source));
			bos = new BufferedOutputStream(new FileOutputStream(dest));

			sbuf = bis.read(buf, 0, 1024);
			while (sbuf > 0) {
				bos.write(buf, 0, sbuf);
				sbuf = bis.read(buf, 0, 1024);
			}
		} catch (Exception e) {
		}
		try {
			bos.close();
		} catch (Exception e) {
			return "Copy failed for=" + source + Constants.CRUDE_SEPERATOR;
		}
		return "Copied file=" + source + overwrite + Constants.CRUDE_SEPERATOR;
	}

	/**
	 *  Merge a file with properties.  It will overwrite an existing file.
	 *  This is not exactly FAST.
	 * @param source source file
	 * @param dest destination file
	 * @param props properties that are candidates for substitution
	 * @return string containing errors or messages
	 */
	public static String merge(
		String source,
		String dest,
		ExtendedProperties props) {

		int state = STATE_FRESH;
		BufferedReader inBR = null;
		BufferedWriter outT = null;
		int workingC;
		StringBuffer cBuf = null;
		String theVar = null;
		String ts = null;
		StringBuffer messages = new StringBuffer();
		String overwrite = " ";

		// Read and fix
		try {
			// Clear an existing file
			File destf = new File(dest);
			if (destf.exists()) {
				destf.delete();
				overwrite = " [overwrite] ";
			}
			inBR = new BufferedReader(new FileReader(source));
			outT = new BufferedWriter(new FileWriter(dest));
			
			// start the engine
			workingC = inBR.read();
			while ((workingC >= 0)
				&& (workingC
					< MORONIC_TOP_VALUE)) {

				switch (state) {

					case STATE_FRESH :
						if (workingC == Constants.CONFIG_OPEN)
							state = STATE_FRONT;
						else
							outT.write(workingC);
						break;

					case STATE_FRONT :
						if (workingC == Constants.CONFIG_OPEN) {
							state = STATE_READ;
							cBuf = new StringBuffer();
						} else {
							state = STATE_FRESH;
							outT.write(Constants.CONFIG_OPEN);
							outT.write(workingC);
						}
						break;

					case STATE_READ :
						if (workingC == Constants.CONFIG_CLOSE) {
							state = STATE_TAIL;
						} else {
							cBuf.append((char) workingC);
						}
						break;

					case STATE_TAIL :
						if (workingC == Constants.CONFIG_CLOSE) {
							state = STATE_FRESH;
							theVar = cBuf.toString();

							if (props.containsKey(theVar)) {
								ts = (String) props.get(theVar);
								outT.write(ts, 0, ts.length());
							} else {
								outT.write(Constants.CONFIG_BLANK);
							}
						} else {
							outT.write(Constants.CONFIG_CLOSE);
							outT.write(workingC);
						}
						break;
				} // end case
				workingC = inBR.read();
			}
		} catch (Exception e) {
			// Might just be EOF
			//System.out.println(e);			
		} finally {
			try {
				inBR.close();
				outT.close();
				if (state != STATE_FRESH) {
					messages.append(
						"Bad File.  Incomplete escape <<>> in file="
							+ source + Constants.CRUDE_SEPERATOR);
					messages.append(".........  Destination file might be corrupt.  file=" + dest + Constants.CRUDE_SEPERATOR);
				}
				messages.append("Merged file to=" + dest + overwrite + Constants.CRUDE_SEPERATOR);
			} catch (Exception e) {
				messages.append("Catastrophic error merging" + source + Constants.CRUDE_SEPERATOR);
				messages.append(".........  Destination file might be corrupt.  file=" + dest + Constants.CRUDE_SEPERATOR);
			}
			
		}
		return messages.toString();
	}

	/**
	 *  Copy entire directories.  I can't believe I have to write this.
	 * @param source source is the source directory, without a training slash
	 * @param dest destination is the destination directory, without a training slash
	 * @param wipe destructive copy.  If true, it will destroy destination directories before
	 * copying the files.  The files themselves are always overwritten.
	 * @return string containing errors or messages
	 */
	public static String copyDir(String source, String dest, boolean wipe) {

		String[] directory;
		String destinationfile = "";
		String sourcefile;
		File fsource = new File(source);
		File fdest = new File(dest);
		int size;
		StringBuffer messages = new StringBuffer();

		try {

			// get the source directory list
			directory = fsource.list();

			// create the destination directory.  If it already exists, wipe it.
			if (fdest.exists()) {
				if (wipe == true) {
					messages.append(Utils.wipeDir(dest));
					fdest.mkdirs();
					messages.append(
						"Make directory=" + dest + Constants.CRUDE_SEPERATOR);
				}
			} else {
				fdest.mkdirs();
				messages.append(
					"Make directory=" + dest + Constants.CRUDE_SEPERATOR);
			}

			// rove the directory list
			for (int i = 0; i < directory.length; i++) {
				try {
					destinationfile = dest + '/' + directory[i];
					sourcefile = source + '/' + directory[i];
					fsource = new File(sourcefile);
					if (fsource.isDirectory()) {
						messages.append(
							"Is directory="
								+ sourcefile
								+ Constants.CRUDE_SEPERATOR);
						messages.append(
							Utils.copyDir(sourcefile, destinationfile, wipe));
					} else {
						messages.append(
							Utils.copy(sourcefile, destinationfile));
					}
				} catch (Exception e) {
					messages.append(
						"Error on="
							+ destinationfile
							+ Constants.CRUDE_SEPERATOR);
				}
			}
			messages.append(
				"Done with directory= " + source + Constants.CRUDE_SEPERATOR);

		} catch (Exception e) {
			messages.append(
				"Serious error on="
					+ dest
					+ " error="
					+ e.getMessage()
					+ Constants.CRUDE_SEPERATOR);
		}
		return messages.toString();
	}

	/**
	 *  Wipe a directory and everything in it
	 * @param dest path to the directory
	 * @return string containing errors or messages
	 */
	public static String wipeDir(String dest) {

		String[] directory;
		File fsource = new File(dest);
		StringBuffer messages = new StringBuffer();
		String newdest;
		File newf;

		try {

			// get the source directory list
			directory = fsource.list();

			// rove the directory list
			for (int i = 0; i < directory.length; i++) {
				try {
					newdest = dest + '/' + directory[i];
					newf = new File(newdest);
					if (newf.isDirectory()) {
						messages.append(
							"Wipe directory="
								+ newdest
								+ Constants.CRUDE_SEPERATOR);
						messages.append(Utils.wipeDir(newdest));
					} else {
						newf.delete();
					}
				} catch (Exception e) {
					messages.append(
						"Error on wipe=" + dest + Constants.CRUDE_SEPERATOR);
				}
			}
			fsource.delete();
			messages.append(
				"Done wipe of directory= " + dest + Constants.CRUDE_SEPERATOR);

		} catch (Exception e) {
			messages.append(
				"Serious error on wipe of= "
					+ dest
					+ " error="
					+ e.getMessage()
					+ Constants.CRUDE_SEPERATOR);
		}
		return messages.toString();
	}
	
	/**
	 *  Read a file into a String.
	 * @param f File to read
	 * @return string if successful or otherwise null
	 */
	public static String loadFile2String(File   f) {

		StringBuffer	buffer = new StringBuffer();
		try {

			char[] buf = new char[1024];
			int sbuf;

			BufferedReader bis
			   = new BufferedReader(new FileReader(f));

			sbuf = bis.read(buf, 0, 1024);
			while (sbuf > 0) {
				buffer.append(buf, 0, sbuf);
				sbuf = bis.read(buf, 0, 1024);
			}
		} catch (Exception e) {
e.printStackTrace();
			return null;
		}
		return buffer.toString();
	}
	
	/**
	 * Get a packed, big-endian integer.  The array better have 4 bytes or you'll get an exception 
	 * @param buf arracy of bytes
	 * @return the integer
	 */	
	public static int packInteger(byte[] buf) {
		return ((buf[3] & 0xFF) << 0) +
		((buf[2] & 0xFF) << 8) +
		((buf[1] & 0xFF) << 16) +
		((buf[0] & 0xFF) << 24);
	}
	
	/**
	 * Unpack an integer into an array--big-endian.  The array better have 4 bytes or you'll get an exception 
	 * @param buf array of bytes.  better be at least 4 bytes
	 * @param offset offset into the array to start the unpack
	 * @param value the integer to unpack
	 */	
	public static void unpackInteger(byte[] buf, int offset, int value) {
		buf[offset + 3] = (byte) (value >>> 0);
		buf[offset + 2] = (byte) (value >>> 8);
		buf[offset + 1] = (byte) (value >>> 16);
		buf[offset + 0] = (byte) (value >>> 24);
	}	
	
	// Total kludge.  don't use
	public static String norfIt(int value) {
		if (value < 10) {
			return new String ("0" + value);
		} else if (value < 100) {
			return Integer.toString(value);
		} else if (value < 385) {
			StringBuffer zonk = new StringBuffer();
			zonk.append((char)value / 65);
			zonk.append((char)value % 65);
			return zonk.toString();
		} else {
			return "xx";
		}
	}
	
}