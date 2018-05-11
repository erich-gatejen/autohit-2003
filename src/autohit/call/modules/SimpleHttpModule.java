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

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;

import autohit.call.CallException;
import autohit.common.AutohitProperties;
import autohit.common.Constants;
import autohit.common.EasySSLProtocolSocketFactory;

/**
 * Simple http module. There is a client/per session at this time. The property
 * "wire" sets if the very noisy HttpClient wire logging is turned on or not.
 * the most recent instantiation of this module will set the HttpClient property
 * for all instances.
 * 
 * @author Erich P. Gatejen
 * @version 1.0 <i>Version History </i> <code>EPG - Initial - 22Jun03<br>
 * EPG - Set wire property - 2Sep03</code>
 *  
 */
public class SimpleHttpModule extends Module {

    private final static String myNAME = "SimpleHttp";

    /**
     * METHODS
     */
    private final static String method_SESSION = "start";
    private final static String method_SESSION_1_ADDRESS = "address";
    private final static String method_SESSION_2_PORT = "port";
    private final static String method_SESSIONHTTPS = "starthttps";
    private final static String method_SESSIONHTTPS_1_ADDRESS = "address";
    private final static String method_SESSIONHTTPS_2_PORT = "port";
    private final static String method_GET = "get";
    private final static String method_GET_1_URL = "url";
    private final static String method_DONE = "done";
    private final static String method_CREDENTIALS = "set_credentials";
    private final static String method_CREDENTIALS_1_UID = "uid";
    private final static String method_CREDENTIALS_2_PASS = "password";
    private final static String method_POST = "post";
    private final static String method_POST_1_URL = method_GET_1_URL;
    private final static String method_POST_2_TABLE = "table";
    private final static String method_TIMEOUT = "timeout";
    private final static String method_TIMEOUT_1_MILLIS = "millis";

    private final static int DEFAULT_TIMEOUT = 10000; // 10 seconds
    private final static int DEFAULT_HTTP = 80;
    private final static int DEFAULT_HTTPS = 443;    
    
    private HttpClient httpClient;
    private Credentials creds;
    boolean started;

    /**
     * Constructor
     */
    public SimpleHttpModule() {

    }

    // IMPLEMENTORS

    /**
     * Execute a named method. You must implement this method. You can call any
     * of the helpers for data and services. The returned object better be a
     * string (for now).
     * 
     * @param name
     *            name of the method
     * @see autohit.common.NOPair
     * @throws CallException
     */
    public Object execute_chain(String name) throws CallException {

        Object response = null;
        String param1;
        String param2;
        Object thingie;
        int port = DEFAULT_HTTP;

        if (name.equals(method_SESSION)) {

            param1 = (String) getParam(method_SESSION_1_ADDRESS);
            if (param1 == null) { throw buildException("Serious FAULT while creating session with start method.  Required 'address' parameter not provided",
                    CallException.CODE_MODULE_FAULT); }

            // port is optional
            param2 = (String) getParam(method_SESSION_2_PORT);
            if (param2 != null) {
                try {
                    port = Integer.parseInt(param2);
                } catch (Exception e) {
                    error("Bad port parameter for start method.  Defaulting to " + DEFAULT_HTTP + "  Errored parameter=" + param2);
                    port = DEFAULT_HTTP;
                }
            }

            // Do it
            this.start(param1, port);
            response = Constants.EMPTY_LEFT;

        } else if (name.equals(method_SESSIONHTTPS)) {

            param1 = (String) getParam(method_SESSIONHTTPS_1_ADDRESS);
            if (param1 == null) { throw buildException("Serious FAULT while creating session with start method.  Required 'address' parameter not provided",
                    CallException.CODE_MODULE_FAULT); }

            // port is optional
            param2 = (String) getParam(method_SESSIONHTTPS_2_PORT);
            if (param2 != null) {
                try {
                    port = Integer.parseInt(param2);
                } catch (Exception e) {
                    error("Bad port parameter for start method.  Defaulting to " + DEFAULT_HTTPS + "  Errored parameter=" + param2);
                    port = DEFAULT_HTTP;
                }
            }

            // Do it
            this.starthttps(param1, port);
            response = Constants.EMPTY_LEFT;

        } else if (name.equals(method_TIMEOUT)) {
            if (started == false) { throw buildException("module:SimpleHttp:Tried to set timeout when a session wasn't started.",
                    CallException.CODE_MODULE_FAULT); }
            param1 = (String) getParam(method_TIMEOUT_1_MILLIS);
            if (param1 == null) {
                error("Missing 'millis' parameter for timeout method.");
            } else {
                try {
                    httpClient.setConnectionTimeout(Integer.parseInt(param1));
                } catch (Exception e) {
                    error("Paramater 'millis' for timeout method is malformed.");
                }
            }

        } else if (name.equals(method_GET)) {
            param1 = (String) getParam(method_GET_1_URL);
            if (param1 == null) {
                error("Missing 'url' parameter for get method.  Aborting get.");
            } else {
                response = this.get(param1);
            }

        } else if (name.equals(method_POST)) {
            param1 = (String) getParam(method_POST_1_URL);
            param2 = (String) getParam(method_POST_2_TABLE);
            if ((param1 == null) || (param2 == null)) {
                error("Missing parameter for post method.  Aborting post.");
            } else {
                thingie = this.getPersist(param2);
                if (thingie instanceof Hashtable) {
                    response = this.post(param1, (Hashtable) thingie);
                } else {
                    throw buildException("Serious FAULT in method POST.  Expected " + param2
                            + " to be a TABLE, but it isn't.  Faulting to prevent runaway execution.", CallException.CODE_MODULE_FAULT);
                }
            }

        } else if (name.equals(method_DONE)) {
            this.done();
            response = Constants.EMPTY_LEFT;

        } else if (name.equals(method_CREDENTIALS)) {
            param1 = (String) getParam(method_CREDENTIALS_1_UID);
            param2 = (String) getParam(method_CREDENTIALS_2_PASS);

            if ((param1 == null) || (param2 == null)) { throw buildException(
                    "Serious FAULT while setting credentials.  One or both of the required 'uid' and 'password' not provided", CallException.CODE_MODULE_FAULT); }
            this.set_credentials(param1, param2);
            response = Constants.EMPTY_LEFT;

        } else {
            error("Not a provided method.  method=" + name);
            response = Constants.EMPTY_LEFT;
        }
        return response;
    }

    /**
     * Allow the subclass a chance to initialize. At a minium, an implementor
     * should create an empty method.
     * 
     * @throws CallException
     * @return the name
     */
    protected String instantiation_chain() throws CallException {

        // Do we turn off that noisy HTTPClient logging. OFF by default
        try {
            String p = visSC.getInvokerProperties().getString(AutohitProperties.SYSTEM_WIRE_DEBUG);
            if ((p != null) && (p.equals("true"))) {
                System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
            } else {
                System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "error");
            }
        } catch (Exception e) {
            // Just not that important.
        }

        // Allocate a client
        started = false;
        return myNAME;
    }

    /**
     * Allow the subclass a chance to cleanup on free. At a minium, an
     * implementor should create an empty method.
     * 
     * @throws CallException
     */
    protected void free_chain() throws CallException {
        // just in case....
        httpClient = null;
    }

    // PRIVATE IMPLEMENTATIONS

    /**
     * Start method for an HTTP session. It will set the target address for the client, as well as
     * clearing any state.
     * 
     * @param addr
     *            the address. Do not include protocol, but you may add port
     *            (ie. "www.goatland.com:80").
     * @throws CallException
     */
    private void start(String addr, int port) throws CallException {

        try {
            httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
            creds = null;
            HttpState initialState = new HttpState();
            initialState.setCookiePolicy(CookiePolicy.COMPATIBILITY);
            httpClient.setState(initialState);
            httpClient.setConnectionTimeout(DEFAULT_TIMEOUT);
            httpClient.getHostConfiguration().setHost(addr, port, "http");

        } catch (Exception ex) {
            throw new CallException("Serious fault while creating session with start method.  Session is not valid.  error=" + ex.getMessage(),
                    CallException.CODE_MODULE_FAULT, ex);
        }

        // NO CODE AFTER THIS!
        started = true;
    }

    /**
     * Start method for an HTTPS session. It will set the target address for the client, as well as
     * clearing any state.
     * 
     * @param addr
     *            the address. Do not include protocol, but you may add port
     *            (ie. "www.goatland.com:443").
     * @throws CallException
     */
    private void starthttps(String addr, int port) throws CallException {

        try {
            // buidl protocol
            Protocol myhttps = new Protocol("https", new EasySSLProtocolSocketFactory(), port);

            httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
            creds = null;
            HttpState initialState = new HttpState();
            initialState.setCookiePolicy(CookiePolicy.COMPATIBILITY);
            httpClient.setState(initialState);
            httpClient.setConnectionTimeout(DEFAULT_TIMEOUT);
            httpClient.getHostConfiguration().setHost(addr, port, myhttps);

        } catch (Exception ex) {
            throw new CallException("Serious fault while creating session with start method.  Session is not valid.  error=" + ex.getMessage(),
                    CallException.CODE_MODULE_FAULT, ex);
        }

        // NO CODE AFTER THIS!
        started = true;
    }

    
    /**
     * Done method. Dispose of state and everything.
     * 
     * @throws CallException
     */
    private void done() throws CallException {

        // NO CODE BEFORE THIS!
        started = false;
        httpClient = null;
    }

    /**
     * Start method. It will set the target address for the client, as well as
     * clearing any state.
     * 
     * @param url
     *            the Url path, not to include protocol, address, and port (ie.
     *            "/goats/index.html").
     * @return the data from the page as a String
     * @throws CallException
     */
    private String get(String url) throws CallException {

        if (started == false) { throw buildException("module:SimpleHttp:Tried to get when a session wasn't started.", CallException.CODE_MODULE_FAULT); }

        String result = null;

        // Construct our method.
        HttpMethod method = new GetMethod(url);
        method.setFollowRedirects(true);
        method.setStrictMode(false);

        //execute the method
        try {
            // Do it
            debug("(get)get=" + url);
            httpClient.executeMethod(method);

            // Process result
            result = method.getResponseBodyAsString();
            log("(get)" + method.getStatusLine().toString() + " size=" + result.length());

        } catch (HttpException he) {
            // Bad but not fatal
            error("(get)Error on connect to url " + url + ".  Error=" + he.getMessage());
        } catch (IOException ioe) {
            // Fatal
            throw buildException("(get)Unable to connect.  Session is invalid.  message=" + ioe.getMessage(), CallException.CODE_MODULE_FAULT, ioe);
        } finally {
            try {
                method.releaseConnection();
                method.recycle();
            } catch (Exception e) {
                // Already FUBAR
            }
        }
        return result;
    }

    /**
     * Post method. It will set the target address for the client, as well as
     * clearing any state.
     * 
     * @param url
     *            the Url path, not to include protocol, address, and port (ie.
     *            "/goats/index.html").
     * @param nv
     *            set of name/value pairs for the post. it can be empty.
     * @return the data from the page as a String
     * @throws CallException
     */
    private String post(String url, Hashtable nv) throws CallException {

        if (started == false) { throw buildException("Tried to post when a session wasn't started.", CallException.CODE_MODULE_FAULT); }

        String result = null;
        String name;
        Object value;

        // Construct our method.
        PostMethod method = new PostMethod(url);
        method.setFollowRedirects(true);
        method.setStrictMode(false);

        //build the rest of the method
        try {
            // Construct the headers
            Enumeration eNV = nv.keys();
            while (eNV.hasMoreElements()) {
                name = (String) eNV.nextElement();
                value = nv.get(name);
                if (value instanceof String) {
                    // Only take it if it is a string
                    method.addParameter(name, (String) value);
                    debug("ADD POST - name=" + name + " value=" + (String) value);
                }
            }
            //DEBUG
            debug("DUMP POST-------------------------------");
            debug(method.toString());
            debug("DUMP POST-------------------------------");

            // Do it
            debug("(post)post=" + url);
            httpClient.executeMethod(method);

            // Process result
            result = method.getResponseBodyAsString();
            log("(post)" + method.getStatusLine().toString() + " size=" + result.length());

        } catch (HttpException he) {
            // Bad but not fatal
            error("(post)Error on connect to url " + url + ".  Error=" + he.getMessage());
        } catch (IOException ioe) {
            // Fatal
            throw buildException("(post)Unable to connect.  Session is invalid.", CallException.CODE_MODULE_FAULT, ioe);

        } catch (Exception ex) {
            // Fatal
            throw buildException("(post)Serious general error.", CallException.CODE_MODULE_FAULT, ex);
        } finally {
            try {
                method.releaseConnection();
                method.recycle();
            } catch (Exception e) {
                // Already FUBAR
            }
        }
        return result;
    }

    /**
     * Set credentials method. It will throw an exception if a session isn't
     * started.
     * 
     * @param uid
     *            User id
     * @param pass
     *            Password
     * @throws CallException
     */
    private void set_credentials(String uid, String pass) throws CallException {

        if (started) {
            creds = new UsernamePasswordCredentials(uid, pass);
            httpClient.getState().setCredentials(null, null, creds);

        } else {
            throw new CallException("Tried to set credentials before a session is started.", CallException.CODE_MODULE_REPORTED_ERROR);
        }
    }

}