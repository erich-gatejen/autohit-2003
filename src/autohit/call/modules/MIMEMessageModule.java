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
package autohit.call.modules;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import autohit.call.CallException;
import autohit.common.Constants;
import autohit.universe.UniverseException;

/**
 * MIME message module.  It will form MIME messages using javamail 
 * functions.
 * <p>
 * It uses system properties for javax.mail Session.
 * <p>
 * If an encoding is set, strings will be converted to bytes using the
 * system default encoding and then encoded to the specified type.
 * If the encoding is specified and an object is read from the universe,
 * it will assume it is a byte stream and will encode it according to the
 * specified charset.
 * <p>
 * <code>
 * start() start a new message, with no multipart<br>
 * multipart() start a new multipart message<br>
 * setencoding(enc) set encoding to use for subsiquent operations.<br>
 * resetencoding() use the default encoding.<br>
 * from(address, optional{personal}) set FROM address<br>
 * to(address,  optional{personal}) add a TO address.  Additional calls add new recipients.<br>
 * cc(address,  optional{personal}) add a CC address.  Additional calls add new recipients.<br>
 * bcc(address, optional{personal}) add a BCC address.  Additional calls add new recipients.<br>
 * subject(string) set the subject line<br>
 * header(n,v) add a header name/value pair.<br>
 * addcontent(text) add content text to the non-multipart message<br>
 * addpart(text,contentid,description) add part from string using default encodings<br>
 * addpartenc(text, tenc, contentid, cenc, description, denc) add part using specified encodings<br>
 * addpartuni(uniobj,contentid,description, type) add part from universe object using default encodings<br>
 * addpartunienc(uniobj, tenc, contentid, cenc, description, denc, type) add part from universe object using specified encodings (uniobject currently ingnored)<br>
 * save() validate message and freeze send time.<br>
 * tostring() return the message as a string.  It must be a save()'d message.<br>
 * touni(uniobj) save the message to a universe object.  It must be a save()'d message.<br>
 * </code>
 * @author Erich P. Gatejen
 * @version 1.0
 * <i>Version History</i>
 * <code>EPG - Initial - 7Aug03</code>
 */
public class MIMEMessageModule extends Module {

	private final static String myNAME = "MIMEMessage";

	private final static String CONTENTTYPE_HEADERSTRING = "Context-Type";

	/**
	 * METHODS
	 */
	private final static String method_START = "start";
	private final static String method_MULTIPART = "multipart";
	private final static String method_SETENCODING = "setencoding";
	private final static String method_SETENCODING_1_ENC = "enc";
	private final static String method_RESETENCODING = "resetencoding";
	private final static String method_FROM = "from";
	private final static String method_FROM_1_ADDR = "address";
	private final static String method_FROM_2_PERSONAL = "personal";
	private final static String method_TO = "to";
	private final static String method_TO_1_ADDR = "address";
	private final static String method_TO_2_PERSONAL = "personal";
	private final static String method_CC = "cc";
	private final static String method_CC_1_ADDR = "address";
	private final static String method_CC_2_PERSONAL = "personal";
	private final static String method_BCC = "bcc";
	private final static String method_BCC_1_ADDR = "address";
	private final static String method_BCC_2_PERSONAL = "personal";
	private final static String method_SUBJECT = "subject";
	private final static String method_SUBJECT_1_STRING = "string";
	private final static String method_HEADER = "header";
	private final static String method_HEADER_1_NAME = "n";
	private final static String method_HEADER_2_VALUE = "v";
	private final static String method_ADDCONTENT = "addcontent";
	private final static String method_ADDCONTENT_1_TEXT = "text";
	private final static String method_ADDPART = "addpart";
	private final static String method_ADDPART_1_TEXT = "text";
	private final static String method_ADDPART_2_CONTENTID = "contentid";
	private final static String method_ADDPART_3_DESCRIPTION = "description";
	private final static String method_ADDPARTENC = "addpartenc";
	private final static String method_ADDPARTENC_1_TEXT = "text";
	private final static String method_ADDPARTENC_2_TENC = "tenc";
	private final static String method_ADDPARTENC_3_CONTENTID = "contentid";
	private final static String method_ADDPARTENC_4_CENC = "cenc";
	private final static String method_ADDPARTENC_5_DESCRIPTION = "description";
	private final static String method_ADDPARTENC_6_DENC = "denc";
	private final static String method_ADDPARTUNI = "addpartuni";
	private final static String method_ADDPARTUNI_1_UNIOBJ = "uniobj";
	private final static String method_ADDPARTUNI_2_CONTENTID = "contentid";
	private final static String method_ADDPARTUNI_3_DESCRIPTION = "description";
	private final static String method_ADDPARTUNI_4_TYPE = "type";
	private final static String method_ADDPARTUNIENC = "addpartunienc";
	private final static String method_ADDPARTUNIENC_1_UNIOBJ = "uniobj";
	private final static String method_ADDPARTUNIENC_2_TENC = "tenc";
	private final static String method_ADDPARTUNIENC_3_CONTENTID = "contentid";
	private final static String method_ADDPARTUNIENC_4_CENC = "cenc";
	private final static String method_ADDPARTUNIENC_5_DESCRIPTION =
		"description";
	private final static String method_ADDPARTUNIENC_6_DENC = "denc";
	private final static String method_ADDPARTUNIENC_7_TYPE = "type";
	private final static String method_SAVE = "save";
	private final static String method_TOSTRING = "tostring";
	private final static String method_TOUNI = "touni";
	private final static String method_TOUNI_1_UNIOBJ = "uniobj";

	private MimeMessage msg;
	private Multipart mp;
	private Session defaultSession;
	private boolean valid;

	// If null, then dont re-encode the strings.
	private String encoding;

	/**
	 * Constructor
	 */
	public MIMEMessageModule() {

	}

	// IMPLEMENTORS

	/**
	 * Execute a named method.  You must implement this method.
	 * You can call any of the helpers for data and services.
	 * The returned object better be a string (for now).
	 * @param name name of the method
	 * @see autohit.common.NOPair
	 * @throws CallException
	 */
	public Object execute_chain(String name) throws CallException {

		Object response = Constants.EMPTY_LEFT;
		Object thingie;

		if (name.equals(method_START)) {
			this.start();

		} else if (name.equals(method_MULTIPART)) {
			this.multipart();

		} else if (name.equals(method_SETENCODING)) {
			this.setencoding(this.required(method_SETENCODING_1_ENC, name));

		} else if (name.equals(method_RESETENCODING)) {
			this.resetencoding();

		} else if (name.equals(method_FROM)) {
			String param1 = this.required(method_FROM_1_ADDR, name);
			String param2 = this.optional(method_FROM_2_PERSONAL);
			if (param2 == null)
				param2 = param1;
			this.from(param1, param2);

		} else if (name.equals(method_TO)) {
			String param1 = this.required(method_TO_1_ADDR, name);
			String param2 = this.optional(method_TO_2_PERSONAL);
			if (param2 == null)
				param2 = param1;
			this.recipient(param1, param2, Message.RecipientType.TO);

		} else if (name.equals(method_CC)) {
			String param1 = this.required(method_CC_1_ADDR, name);
			String param2 = this.optional(method_CC_2_PERSONAL);
			if (param2 == null)
				param2 = param1;
			this.recipient(param1, param2, Message.RecipientType.CC);

		} else if (name.equals(method_BCC)) {
			String param1 = this.required(method_BCC_1_ADDR, name);
			String param2 = this.optional(method_BCC_2_PERSONAL);
			if (param2 == null)
				param2 = param1;
			this.recipient(param1, param2, Message.RecipientType.BCC);

		} else if (name.equals(method_SUBJECT)) {
			this.subject(this.required(method_SUBJECT_1_STRING, name));

		} else if (name.equals(method_HEADER)) {
			this.header(
				this.required(method_HEADER_1_NAME, name),
				this.required(method_HEADER_2_VALUE, name));

		} else if (name.equals(method_ADDCONTENT)) {
			this.addcontent(this.required(method_ADDCONTENT_1_TEXT, name));

		} else if (name.equals(method_ADDPART)) {
			this.addpart(
				this.required(method_ADDPART_1_TEXT, name),
				this.required(method_ADDPART_2_CONTENTID, name),
				this.required(method_ADDPART_3_DESCRIPTION, name));

		} else if (name.equals(method_ADDPARTENC)) {
			this.addpartenc(
				this.required(method_ADDPARTENC_1_TEXT, name),
				this.required(method_ADDPARTENC_2_TENC, name),
				this.required(method_ADDPARTENC_3_CONTENTID, name),
				this.required(method_ADDPARTENC_4_CENC, name),
				this.required(method_ADDPARTENC_5_DESCRIPTION, name),
				this.required(method_ADDPARTENC_6_DENC, name));

		} else if (name.equals(method_ADDPARTUNI)) {
			this.addpartuni(
				this.required(method_ADDPARTUNI_1_UNIOBJ, name),
				this.required(method_ADDPARTUNI_2_CONTENTID, name),
				this.required(method_ADDPARTUNI_3_DESCRIPTION, name),
				this.required(method_ADDPARTUNI_4_TYPE, name));

		} else if (name.equals(method_ADDPARTUNIENC)) {
			this.addpartunienc(
				this.required(method_ADDPARTUNIENC_1_UNIOBJ, name),
				this.required(method_ADDPARTUNIENC_2_TENC, name),
				this.required(method_ADDPARTUNIENC_3_CONTENTID, name),
				this.required(method_ADDPARTUNIENC_4_CENC, name),
				this.required(method_ADDPARTUNIENC_5_DESCRIPTION, name),
				this.required(method_ADDPARTUNIENC_6_DENC, name),
				this.required(method_ADDPARTUNIENC_7_TYPE, name));

		} else if (name.equals(method_SAVE)) {
			this.save();

		} else if (name.equals(method_TOSTRING)) {
			response = this.mtostring();

		} else if (name.equals(method_TOUNI)) {
			this.touni(this.required(method_TOUNI_1_UNIOBJ, name));

		} else {
			error("Not a provided method.  method=" + name);
		}
		return response;
	}

	/**
	 * Allow the subclass a chance to initialize.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 * @return the name
	 */
	protected String instantiation_chain() throws CallException {
		valid = false;
		msg = null;
		mp = null;
		defaultSession = Session.getDefaultInstance(System.getProperties());
		return myNAME;
	}

	/**
	 * Allow the subclass a chance to cleanup on free.  At a minium, an 
	 * implementor should create an empty method.
	 * @throws CallException
	 */
	protected void free_chain() throws CallException {
		// just in case....
	}

	// PRIVATE IMPLEMENTATIONS

	/**
	 * Start a non-multipart session.
	 * @throws CallException
	 */
	private void start() throws CallException {
		try {
			valid = false;
			encoding = null;
			mp = null;
			msg = new MimeMessage(defaultSession);
		} catch (Exception e) {
			this.fault(
				"Could not instantiate a javax MimeMessage.  e="
					+ e.getMessage());
		}
	}

	/**
	 * Start a multipart session.
	 * @throws CallException
	 */
	private void multipart() throws CallException {
		try {
			valid = false;
			encoding = null;
			mp = new MimeMultipart();
			msg = new MimeMessage(defaultSession);
		} catch (Exception e) {
			this.fault(
				"Could not instantiate a javax MimeMessage.  e="
					+ e.getMessage());
		}
	}

	/**
	 * Reset the encoding.  Will use system default.
	 */
	private void resetencoding() throws CallException {
		encoding = null;
	}

	/**
	 * Set a new default encoding method. It mmust be a valid 
	 * @param enc Encoding name
	 * @throws CallException if the charset is not supported by the system
	 */
	private void setencoding(String enc) throws CallException {
		if (Charset.isSupported(enc)) {
			encoding = enc;
		} else {
			this.fault(
				"Specified character encoding (Charset) is not supported in this system.  enc="
					+ enc);
		}
	}

	/**
	 * Set FROM 
	 * @param address the address
	 * @param pname the personal name
	 * @throws CallException if there is a problem
	 */
	private void from(String address, String pname) throws CallException {
		InternetAddress ineta = buildaddy(address, pname);
		try {
			msg.setFrom(ineta);
		} catch (Exception ex) {
			this.fault("Could not set FROM address.  address=" + address, ex);
		}
	}

	/**
	 * Add recipient
	 * @param address the address
	 * @param pname the personal name
	 * @throws CallException if there is a problem
	 */
	private void recipient(
		String address,
		String pname,
		Message.RecipientType type)
		throws CallException {
		InternetAddress ineta = buildaddy(address, pname);
		try {
			msg.addRecipient(type, ineta);
		} catch (Exception ex) {
			this.fault("Could not add address.  address=" + address, ex);
		}
	}

	/**
	 * Set SUBJECT 
	 * @param text to set as the subject
	 * @throws CallException if there is a problem
	 */
	private void subject(String text) throws CallException {
		try {
			if (encoding == null) {
				msg.setSubject(text);
			} else {
				msg.setSubject(text, encoding);
			}
		} catch (Exception ex) {
			this.fault("Could not set SUBJECT address.  subject=" + text, ex);
		}
	}

	/**
	 * Add header
	 * @param name name of header item
	 * @param value value of the item.
	 * @throws CallException if there is a problem
	 */
	private void header(String name, String value) throws CallException {
		try {
			if (encoding == null) {
				msg.addHeader(name, value);
			} else {
				msg.addHeader(
					name,
					MimeUtility.encodeText(value, encoding, null));
			}
		} catch (Exception ex) {
			this.fault(
				"Could not set SUBJECT address.  name="
					+ name
					+ ".  message="
					+ ex.getMessage(),
				ex);
		}
	}

	/**
	 * Add content.  Only use this is start() used.  It will be text/plain.
	 * @param text to add
	 * @throws CallException if there is a problem
	 */
	private void addcontent(String text) throws CallException {

		if (mp != null) {
			this.fault("You may not addcontent(text) to a multipart message.");
		}

		try {
			if (encoding == null) {
				msg.setText(text);
			} else {
				msg.setText(text, encoding);
			}
		} catch (Exception ex) {
			this.fault(
				"Could not set content text.  message=" + ex.getMessage(),
				ex);
		}
	}

	/**
	 * Add a part to a multipart.  All fields are subject to the encodings.
	 * If the text is pre-encoded, there should be no problems.  However,
	 * if there are, you might want to use Universe Objects instead.
	 * @param text is the text to add as a body part
	 * @param cid is the Content ID
	 * @param desc is the content description.
	 * @throws CallException if there is a problem
	 */
	private void addpart(String text, String cid, String desc)
		throws CallException {

		if (mp == null) {
			this.fault("You may not addpart(text) to a non-multipart message.");
		}
		try {

			MimeBodyPart mbp = new MimeBodyPart();

			if (encoding == null) {
				mbp.setText(text);
				mbp.setDescription(desc);
				mbp.setContentID(cid);
			} else {
				mbp.setText(text, encoding);
				mbp.setDescription(desc, encoding);
				mbp.setContentID(MimeUtility.encodeText(cid, encoding, null));
			}

			mp.addBodyPart(mbp);

		} catch (Exception ex) {
			this.fault(
				"Could not addpart(text).  message=" + ex.getMessage(),
				ex);
		}
	}

	/**
	 * Add a part using specified encodings.
	 * If the text is pre-encoded, there should be no problems.  However,
	 * if there are, you might want to use Universe Objects instead.
	 * @param text is the text to add as a body part
	 * @param tenc text encoding
	 * @param cid is the Content ID
	 * @param cenc Content ID encoding
	 * @param desc is the Content Cescription.
	 * @param denc Content Description encoding.
	 * @throws CallException if there is a problem
	 */
	private void addpartenc(
		String text,
		String tenc,
		String cid,
		String cenc,
		String desc,
		String denc)
		throws CallException {

		if (mp == null) {
			this.fault(
				"You may not addpartenc(...) to a non-multipart message.");
		}
		try {

			MimeBodyPart mbp = new MimeBodyPart();

			mbp.setText(text, tenc);
			mbp.setDescription(desc, cenc);
			mbp.setContentID(MimeUtility.encodeText(cid, denc, null));

			mp.addBodyPart(mbp);

		} catch (Exception ex) {
			this.fault(
				"Could not addpartenc(...).  message=" + ex.getMessage(),
				ex);
		}
	}

	/**
	 * Add a part to a multipart from a universe object.  All fields are subject to the encodings.
	 * If the text is pre-encoded, there should be no problems.  However,
	 * if there are, you might want to use Universe Objects instead.
	 * @param uniobj is the text to add as a body part
	 * @param cid is the Content ID
	 * @param desc is the content description.
	 * @param type Content Type.  If empty string, it will get it from the data source.
	 * @throws CallException if there is a problem
	 */
	private void addpartuni(
		String uniobj,
		String cid,
		String desc,
		String type)
		throws CallException {

		if (mp == null) {
			this.fault(
				"You may not addpartuni(uniobj) to a non-multipart message.");
		}
		try {

			// Do actual body
			MimeBodyPart mbp = new MimeBodyPart();
			DataSource ds = visUniverse.getDataSource(uniobj);
			mbp.setDataHandler(new DataHandler(ds));

			if (encoding == null) {
				mbp.setDescription(desc);
				mbp.setContentID(cid);
			} else {
				mbp.setDescription(desc, encoding);
				mbp.setContentID(MimeUtility.encodeText(cid, encoding, null));
			}
			if (type.length() > 0) {
				mbp.setHeader(CONTENTTYPE_HEADERSTRING, type);
			} else {
				mbp.setHeader(CONTENTTYPE_HEADERSTRING, ds.getContentType());
			}

			mp.addBodyPart(mbp);

		} catch (UniverseException uex) {
			this.fault(
				"Could not addpartuni(...).  Could not get the Universe object.  code="
					+ uex.numeric
					+ "  message="
					+ uex.getMessage(),
				uex);
		} catch (Exception ex) {
			this.fault(
				"Could not addpartuni(...).  message=" + ex.getMessage(),
				ex);
		}
	}

	/**
	 * Add a part to a multipart from a universe object.  All fields are subject to the encodings.
	 * If the text is pre-encoded, there should be no problems.  However,
	 * if there are, you might want to use Universe Objects instead.
	 * @param uniobj is the universe object to add as a body part
	 * @param tenc Encoding (Currently ignored)
	 * @param cid is the Content ID
	 * @param cenc Content ID encoding
	 * @param desc is the Content Cescription.
	 * @param denc Content Description encoding.
	 * @param type Content Type.  If empty string, it will get it from the data source.
	 * @throws CallException if there is a problem
	 */
	private void addpartunienc(
		String uniobj,
		String tenc,
		String cid,
		String cenc,
		String desc,
		String denc,
		String type)
		throws CallException {

		if (mp == null) {
			this.fault(
				"You may not addpartunienc(uniobj) to a non-multipart message.");
		}
		try {

			// Do actual body
			MimeBodyPart mbp = new MimeBodyPart();
			DataSource ds = visUniverse.getDataSource(uniobj);
			mbp.setDataHandler(new DataHandler(ds));

			mbp.setDescription(desc, cenc);
			mbp.setContentID(MimeUtility.encodeText(cid, denc, null));

			if (type.length() > 0) {
				mbp.setHeader(CONTENTTYPE_HEADERSTRING, type);
			} else {
				mbp.setHeader(CONTENTTYPE_HEADERSTRING, ds.getContentType());
			}

			mp.addBodyPart(mbp);

		} catch (UniverseException uex) {
			this.fault(
				"Could not addpartunienc(...).  Could not get the Universe object.  code="
					+ uex.numeric
					+ "  message="
					+ uex.getMessage(),
				uex);
		} catch (Exception ex) {
			this.fault(
				"Could not addpartunienc(...).  message=" + ex.getMessage(),
				ex);
		}
	}

	/**
	 * Validate message and freeze send time.
	 * @throws CallException
	 */
	private void save() throws CallException {

		try {

			if (mp != null)
				msg.setContent(mp);
			msg.setSentDate(new Date());
			//msg.saveChanges();
			valid = true;
		} catch (Exception ex) {
			this.done();
			this.fault(
				"Could not validate message!  Session will be dumped.  message="
					+ ex.getMessage(),
				ex);
		}
	}

	/**
	 * Return the message as a string.  It must be a save()'d message.
	 * The string will be subject to the local system encoding.
	 * @throws CallException
	 * TODO there has to be a better way that writing to a ByteArray and then toString
	 */
	private String mtostring() throws CallException {
		String response = null;

		if (!valid)
			this.fault(
				"Message is not valid.  You must save() it before mtostring().  message=");

		try {

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			msg.writeTo(bos);
			bos.flush();
			response = bos.toString();

		} catch (Exception ex) {
			this.fault(
				"Could not put message in a string.  message="
					+ ex.getMessage(),
				ex);
		}
		return response;
	}

	/**
	 * Save the message to a universe object.  It must be a save()'d message.
	 * The data will not be encoded at all!
	 * @throws CallException
	 */
	private String touni(String un) throws CallException {
		String response = null;

		if (!valid)
			this.fault(
				"Message is not valid.  You must save() it before mtostring().  message=");

		try {

			OutputStream ois = visUniverse.putStream(un);
			if (ois == null)
				throw new Exception("Universe returned a null stream.  This should never happen.");
			msg.writeTo(ois);

		} catch (UniverseException uex) {
			this.fault(
				"Could not put the message in the Universe due to a Universe problem.  code="
					+ uex.numeric
					+ "  message="
					+ uex.getMessage(),
				uex);

		} catch (Exception ex) {
			this.fault(
				"Could not put message in the Universe.  message="
					+ ex.getMessage(),
				ex);
		}
		return response;
	}

	/**
	 * Done method.  Dispose of state and everything.
	 * @throws CallException
	 */
	private void done() throws CallException {
		encoding = null;
		mp = null;
		msg = null;
		valid = false;
	}

	// HELPERS 

	/**
	 * HELPER
	 * @param address the address
	 * @param pname the personal name
	 * @throws CallException if there is a problem
	 */
	private InternetAddress buildaddy(String address, String pname)
		throws CallException {
		InternetAddress ineta = null;
		try {
			if (encoding == null) {
				ineta = new InternetAddress(address, pname);
			} else {
				ineta = new InternetAddress(address, pname, encoding);
			}
		} catch (Exception ex) {
			this.fault(
				"Could not form and encode address.  address=" + address,
				ex);
		}
		return ineta;
	}

}
