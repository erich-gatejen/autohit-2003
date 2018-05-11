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
package autohit.vm.process;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import autohit.common.Constants;
import autohit.vm.VMCore;
import autohit.vm.VMException;

/**
 * String processing routines.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i> <code>EPG - Rewrite - 13Sep03</code>
 */
public class StringProcessors {

	private final static int STATE_FREE = 0;
	private final static int STATE_VAR_ONE = 1;
	private final static int STATE_VAR_IN = 2;
	private final static int STATE_BUFFER_ONE = 3;
	private final static int STATE_BUFFER_IN = 4;
	private final static int STATE_UNIOBJ_ONE = 5;
	private final static int STATE_UNIOBJ_IN = 6;

	/**
	 * Process String (as a byte stream) to Core (as a byte stream). If there is a problem, you'll get a VMException.
	 * @param s
	 *            String to eval
	 * @param core
	 *            The Core to use
	 * @return The completed string
	 * @throws VMException
	 */
	public static void evalStreams2Core(InputStream in, OutputStream out, VMCore core)
			throws Exception {

		StringBuffer working = null;
		String result = null;
		String variable;
		Object thingthang;
		int state = STATE_FREE;

		BufferedInputStream bin = new BufferedInputStream(in);
		BufferedOutputStream bout = new BufferedOutputStream(out);
		int current = bin.read();
		while (current >= 0) {

			switch (state) {

				case STATE_FREE :
					switch (current) {
						case Constants.VARIABLE :
							state = STATE_VAR_ONE;
							break;
						case Constants.UNIOBJECT :
							state = STATE_UNIOBJ_ONE;
							break;
						case Constants.BUFFER :
							state = STATE_BUFFER_ONE;
							break;
						default :
							bout.write(current);
							break;
					}
					break;

				case STATE_VAR_ONE :
					switch (current) {
						case Constants.VARIABLE :
							// escaped
							bout.write(Constants.VARIABLE);
							state = STATE_FREE;
							break;
						case Constants.UNIOBJECT :
						case Constants.BUFFER :
						default :
							working = new StringBuffer();
							working.append((char) current);
							state = STATE_VAR_IN;
							break;
					}
					break;

				case STATE_VAR_IN :
					switch (current) {
						case Constants.VARIABLE :
							variable = working.toString();
							if (core.exists(variable)) {
								thingthang = core.fetch(variable);
								if (thingthang instanceof String) {
									bout.write( ((String)(thingthang)).getBytes() );
								} else {
									//	Blech! Something bad in this object
									throw new VMException(
											"Not an evaluatable type in variable.  name="
													+ working,
											VMException.CODE_VM_PROCESSOR_ERROR);
								}
							}
							state = STATE_FREE;
							break;
						case Constants.UNIOBJECT :
						case Constants.BUFFER :
						default :
							working.append((char) current);
							break;
					}
					break;

				case STATE_BUFFER_ONE :
					switch (current) {
						case Constants.BUFFER :
							bout.write(Constants.BUFFER);
							state = STATE_FREE;
							break;
						case Constants.VARIABLE :
						case Constants.UNIOBJECT :
						default :
							working = new StringBuffer();
							working.append((char) current);
							state = STATE_BUFFER_IN;
							break;
					}
					break;

				case STATE_BUFFER_IN :
					switch (current) {
						case Constants.BUFFER :
							variable = working.toString();
							if (core.exists(variable)) {
								thingthang = core.fetch(variable);
								if (thingthang instanceof StringBuffer) {
									bout.write( ((StringBuffer)(thingthang)).toString().getBytes() );
								} else {
									//	Blech! Something bad in this object
									throw new VMException(
											"Not an evaluatable type in buffer..  name="
													+ working,
											VMException.CODE_VM_PROCESSOR_ERROR);
								}
							}
							state = STATE_FREE;
							break;
						case Constants.UNIOBJECT :
						case Constants.VARIABLE :
						default :
							working.append((char) current);
							break;
					}
					break;

				case STATE_UNIOBJ_ONE :
					switch (current) {
						case Constants.UNIOBJECT :
							bout.write(Constants.UNIOBJECT);
							state = STATE_FREE;
							break;
						case Constants.VARIABLE :
						case Constants.BUFFER :
						default :
							//working.append((char)current);
							state = STATE_UNIOBJ_IN; // THIS WILL BE AN ERROR
							break;
					}
					break;

				case STATE_UNIOBJ_IN :
					switch (current) {

						case Constants.UNIOBJECT :
							throw new VMException(
									"UNIOBJECT not supported in SimVM",
									VMException.CODE_VM_PROCESSOR_FAULT);
						case Constants.VARIABLE :
						case Constants.BUFFER :
						default :
							// IGNORE, SINCE THIS WILL BE AN ERROR.
							break;
					}
					break;

				default :
					throw new VMException(
							"evalString2Core hit impossible state.  state="
									+ state,
							VMException.CODE_VM_SOFTWARE_DETECTED_FAULT);

			}
			current = bin.read();
		}

		//CHECK STATES
		switch (state) {
			case STATE_FREE :
				// HAPPY HAPPY
				break;
			case STATE_VAR_ONE :
			case STATE_VAR_IN :
				String vmessage;
				if (working.length() > 30) {
					vmessage = new String("Unbounded VARIABLE (oversize) '"
							+ working.toString().substring(30) + "'");
				} else {
					vmessage = new String("Unbounded VARIABLE '"
							+ working.toString() + "'");
				}
				throw new VMException(vmessage,
						VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);

			case STATE_BUFFER_ONE :
			case STATE_BUFFER_IN :
				String bmessage;
				if (working.length() > 30) {
					bmessage = new String("Unbounded BUFFER (oversize) '"
							+ working.toString().substring(30) + "'");
				} else {
					bmessage = new String("Unbounded BUFFER '"
							+ working.toString() + "'");
				}
				throw new VMException(bmessage,
						VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);

			case STATE_UNIOBJ_ONE :
			case STATE_UNIOBJ_IN :
				String umessage;
				if (working.length() > 30) {
					umessage = new String("Unbounded UNIOBJ (oversize) '"
							+ working.toString().substring(30) + "'");
				} else {
					umessage = new String("Unbounded UNIOBJ '"
							+ working.toString() + "'");
				}
				throw new VMException(umessage,
						VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);
			default :
				throw new VMException(
						"evalString2Core hit impossible state.  state=" + state,
						VMException.CODE_VM_SOFTWARE_DETECTED_FAULT);
		}
		bout.flush();
		
	}

	/**
	 * Process String to Core (as a character stream). If there is a problem, you'll get a VMException.
	 * 
	 * @param s
	 *            String to eval
	 * @param core
	 *            The Core to use
	 * @return The completed string
	 * @throws VMException
	 */
	public static String evalString2Core(String s, VMCore core)
			throws Exception {

		StringBuffer working = null;
		String result = null;
		String variable;
		Object thingthang;
		int state = STATE_FREE;

		StringReader rin = new StringReader(s);
		StringWriter rout = new StringWriter();
		int current = rin.read();
		while (current >= 0) {

			switch (state) {

				case STATE_FREE :
					switch (current) {
						case Constants.VARIABLE :
							state = STATE_VAR_ONE;
							break;
						case Constants.UNIOBJECT :
							state = STATE_UNIOBJ_ONE;
							break;
						case Constants.BUFFER :
							state = STATE_BUFFER_ONE;
							break;
						default :
							rout.write(current);
							break;
					}
					break;

				case STATE_VAR_ONE :
					switch (current) {
						case Constants.VARIABLE :
							// escaped
							rout.write(Constants.VARIABLE);
							state = STATE_FREE;
							break;
						case Constants.UNIOBJECT :
						case Constants.BUFFER :
						default :
							working = new StringBuffer();
							working.append((char) current);
							state = STATE_VAR_IN;
							break;
					}
					break;

				case STATE_VAR_IN :
					switch (current) {
						case Constants.VARIABLE :
							variable = working.toString();
							if (core.exists(variable)) {
								thingthang = core.fetch(variable);
								if (thingthang instanceof String) {
									rout.write((String) (thingthang));
								} else {
									//	Blech! Something bad in this object
									throw new VMException(
											"Not an evaluatable type in variable.  name="
													+ working,
											VMException.CODE_VM_PROCESSOR_ERROR);
								}
							}
							state = STATE_FREE;
							break;
						case Constants.UNIOBJECT :
						case Constants.BUFFER :
						default :
							working.append((char) current);
							break;
					}
					break;

				case STATE_BUFFER_ONE :
					switch (current) {
						case Constants.BUFFER :
							rout.write(Constants.BUFFER);
							state = STATE_FREE;
							break;
						case Constants.VARIABLE :
						case Constants.UNIOBJECT :
						default :
							working = new StringBuffer();
							working.append((char) current);
							state = STATE_BUFFER_IN;
							break;
					}
					break;

				case STATE_BUFFER_IN :
					switch (current) {
						case Constants.BUFFER :
							variable = working.toString();
							if (core.exists(variable)) {
								thingthang = core.fetch(variable);
								if (thingthang instanceof StringBuffer) {
									rout.write(((StringBuffer) (thingthang))
											.toString());
								} else {
									//	Blech! Something bad in this object
									throw new VMException(
											"Not an evaluatable type in buffer..  name="
													+ working,
											VMException.CODE_VM_PROCESSOR_ERROR);
								}
							}
							state = STATE_FREE;
							break;
						case Constants.UNIOBJECT :
						case Constants.VARIABLE :
						default :
							working.append((char) current);
							break;
					}
					break;

				case STATE_UNIOBJ_ONE :
					switch (current) {
						case Constants.UNIOBJECT :
							rout.write(Constants.UNIOBJECT);
							state = STATE_FREE;
							break;
						case Constants.VARIABLE :
						case Constants.BUFFER :
						default :
							//working.append((char)current);
							state = STATE_UNIOBJ_IN; // THIS WILL BE AN ERROR
							break;
					}
					break;

				case STATE_UNIOBJ_IN :
					switch (current) {

						case Constants.UNIOBJECT :
							throw new VMException(
									"UNIOBJECT not supported in SimVM",
									VMException.CODE_VM_PROCESSOR_FAULT);
						case Constants.VARIABLE :
						case Constants.BUFFER :
						default :
							// IGNORE, SINCE THIS WILL BE AN ERROR.
							break;
					}
					break;

				default :
					throw new VMException(
							"evalString2Core hit impossible state.  state="
									+ state,
							VMException.CODE_VM_SOFTWARE_DETECTED_FAULT);

			}
			current = rin.read();
		}

		//CHECK STATES
		switch (state) {
			case STATE_FREE :
				result = rout.toString();
				break;
			case STATE_VAR_ONE :
			case STATE_VAR_IN :
				String vmessage;
				if (working.length() > 30) {
					vmessage = new String("Unbounded VARIABLE (oversize) '"
							+ working.toString().substring(30) + "'");
				} else {
					vmessage = new String("Unbounded VARIABLE '"
							+ working.toString() + "'");
				}
				throw new VMException(vmessage,
						VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);

			case STATE_BUFFER_ONE :
			case STATE_BUFFER_IN :
				String bmessage;
				if (working.length() > 30) {
					bmessage = new String("Unbounded BUFFER (oversize) '"
							+ working.toString().substring(30) + "'");
				} else {
					bmessage = new String("Unbounded BUFFER '"
							+ working.toString() + "'");
				}
				throw new VMException(bmessage,
						VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);

			case STATE_UNIOBJ_ONE :
			case STATE_UNIOBJ_IN :
				String umessage;
				if (working.length() > 30) {
					umessage = new String("Unbounded UNIOBJ (oversize) '"
							+ working.toString().substring(30) + "'");
				} else {
					umessage = new String("Unbounded UNIOBJ '"
							+ working.toString() + "'");
				}
				throw new VMException(umessage,
						VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);
			default :
				throw new VMException(
						"evalString2Core hit impossible state.  state=" + state,
						VMException.CODE_VM_SOFTWARE_DETECTED_FAULT);
		}
		return result;

	}

	/**
	 * Process String to Core. If there is a problem, you'll get a VMException.
	 * OLD VERSION
	 * 
	 * @param s
	 *            String to eval
	 * @param core
	 *            The Core to use
	 * @return The completed string
	 * @throws VMException
	 */
	public static String evalString2CoreOLD(String s, VMCore core)
			throws Exception {

		StringBuffer buff = new StringBuffer();
		String result = null;
		String working;
		Object thingthang;
		int end = s.length();
		int otherside;

		try {

			for (int i = 0; i < end; i++) {

				switch (s.charAt(i)) {

					case Constants.VARIABLE :
						otherside = s.indexOf(Constants.VARIABLE, i + 1);
						if ((otherside < 0) || (otherside < i)) {
							throw new VMException(
									"Unbounded VARIABLE",
									VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);
						}
						if (otherside == (i + 1)) {
							//escaped char
							buff.append(Constants.VARIABLE);
							i++; //skip escaped
						} else {
							working = s.substring(i + 1, otherside);
							i = i + working.length() + 1;

							if (core.exists(working)) {
								thingthang = core.fetch(working);
								if (thingthang instanceof String) {
									buff.append((String) thingthang);
								} else {
									//	Blech! Something bad in this object
									throw new VMException(
											"Not an evaluatable type in variable.  name="
													+ working,
											VMException.CODE_VM_PROCESSOR_ERROR);
								}
							} //else {
							// The variable doesn't exist. FAULT.
							//	throw new Exception(
							//		"Variable does not exist. name="
							//			+ working);
							//}
						}
						break;

					case Constants.UNIOBJECT :
						if ((i + 1 < end)
								&& (s.charAt(i + 1) == Constants.UNIOBJECT)) {
							//escaped char
							buff.append(Constants.UNIOBJECT);
							i++; //skip escaped
						} else {
							throw new VMException(
									"UNIOBJECT not supported in SimVM",
									VMException.CODE_VM_PROCESSOR_FAULT);
						}
						break;

					case Constants.BUFFER :
						otherside = s.indexOf(Constants.BUFFER, i + 1);
						if ((otherside < 0) || (otherside < i)) {
							throw new VMException(
									"Unbounded BUFFER name",
									VMException.CODE_VM_PROCESSOR_ERROR_UNBOUNDED);
						}
						if (otherside == (i + 1)) {
							//escaped char
							buff.append(Constants.BUFFER);
							i++; //skip escaped
						} else {
							working = s.substring(i + 1, otherside);
							i = i + working.length() + 1;

							if (core.exists(working)) {
								thingthang = core.fetch(working);
								if (thingthang instanceof StringBuffer) {
									buff.append(((StringBuffer) thingthang)
											.toString());
								} else {
									//	Blech! Something bad in this object
									throw new VMException(
											"Not an evaluatable type in buffer.  name="
													+ working,
											VMException.CODE_VM_PROCESSOR_ERROR_NOTFOUND);
								}
							} else {
								// The variable doesn't exist. FAULT.
								throw new VMException(
										"Buffer does not exist.  name="
												+ working,
										VMException.CODE_VM_PROCESSOR_ERROR_NOTFOUND);
							}
						}
						break;

					default :
						buff.append(s.charAt(i));
				}
			}

			// return it
			result = buff.toString();

		} catch (VMException ev) {
			throw ev;
		} catch (Exception ee) {
			throw new VMException("Exception while evaluating.  message="
					+ ee.getMessage(),
					VMException.CODE_VM_PROCESSOR_ERROR_NOTFOUND, ee);
		}
		return result;

	}

}