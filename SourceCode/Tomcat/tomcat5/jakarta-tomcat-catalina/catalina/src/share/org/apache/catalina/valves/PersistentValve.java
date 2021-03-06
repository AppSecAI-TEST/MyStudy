/*
 * Copyright 1999-2001,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.catalina.valves;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Context;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.Store;
import org.apache.catalina.ValveContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.session.PersistentManager;
import org.apache.catalina.util.StringManager;


/**
 * Valve that implements the default basic behavior for the
 * <code>StandardHost</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>: To work correctly it requires a  PersistentManager.
 *
 * @author Jean-Frederic Clere
 * @version $Revision: 1.5 $ $Date: 2004/02/27 14:58:52 $
 */

public class PersistentValve
    extends ValveBase {


    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.valves.PersistentValve/1.0";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Select the appropriate child Context to process this request,
     * based on the specified request URI.  If no matching Context can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public void invoke(Request request, Response response,
                       ValveContext valveContext)
        throws IOException, ServletException {

        // Select the Context to be used for this Request
        StandardHost host = (StandardHost) getContainer();
        Context context = request.getContext();
        if (context == null) {
            ((HttpServletResponse) response.getResponse()).sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 sm.getString("standardHost.noContext"));
            return;
        }

        // Bind the context CL to the current thread
        Thread.currentThread().setContextClassLoader
            (context.getLoader().getClassLoader());

        // Update the session last access time for our session (if any)
        HttpServletRequest hreq = (HttpServletRequest) request.getRequest();
        String sessionId = hreq.getRequestedSessionId();
        Manager manager = context.getManager();
        if (sessionId != null && manager != null) {
            if (manager instanceof PersistentManager) {
                Store store = ((PersistentManager) manager).getStore();
                if (store != null) {
                    Session session = null;
                    try {
                        session = store.load(sessionId);
                    } catch (Exception e) {
                        log("deserializeError");
                    }
                    if (session != null) {
                        if (!session.isValid() ||
                            isSessionStale(session, System.currentTimeMillis())) {
                            log("session swapped in is invalid or expired");
                            session.expire();
                            store.remove(sessionId);
                        } else {
                            session.setManager(manager);
                            // session.setId(sessionId); Only if new ???
                            manager.add(session);
                            // ((StandardSession)session).activate();
                            session.access();
                        }
                    }
                }
            }
        }
        log("sessionId: " + sessionId);

        // Ask the next valve to process the request.
        valveContext.invokeNext(request, response);

        // Read the sessionid after the response.
        // HttpSession hsess = hreq.getSession(false);
        HttpSession hsess;
        try {
            hsess = hreq.getSession();
        } catch (Exception ex) {
            hsess = null;
        }
        String newsessionId = null;
        if (hsess!=null)
            newsessionId = hsess.getId();

        log("newsessionId: " + newsessionId);
        if (newsessionId!=null) {
            /* store the session in the store and remove it from the manager */
            if (manager instanceof PersistentManager) {
                Session session = manager.findSession(newsessionId);
                Store store = ((PersistentManager) manager).getStore();
                if (store != null && session!=null &&
                    session.isValid() &&
                    !isSessionStale(session, System.currentTimeMillis())) {
                    // ((StandardSession)session).passivate();
                    store.save(session);
                    ((PersistentManager) manager).removeSuper(session);
                    session.recycle();
                } else {
                    log("newsessionId store: " + store + " session: " +
                        session + " valid: " + session.isValid() +
                        " Staled: " +
                        isSessionStale(session, System.currentTimeMillis()));

                }
            } else {
                log("newsessionId Manager: " + manager);
            }
        }
    }

    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
 
        Logger logger = container.getLogger();
        if (logger != null)
            logger.log(this.toString() + ": " + message);
        else
            System.out.println(this.toString() + ": " + message);
 
    }

    /**
     * Indicate whether the session has been idle for longer
     * than its expiration date as of the supplied time.
     *
     * FIXME: Probably belongs in the Session class.
     */
    protected boolean isSessionStale(Session session, long timeNow) {
 
        int maxInactiveInterval = session.getMaxInactiveInterval();
        if (maxInactiveInterval >= 0) {
            int timeIdle = // Truncate, do not round up
                (int) ((timeNow - session.getLastAccessedTime()) / 1000L);
            if (timeIdle >= maxInactiveInterval)
                return true;
        }
 
        return false;
 
    }

}
