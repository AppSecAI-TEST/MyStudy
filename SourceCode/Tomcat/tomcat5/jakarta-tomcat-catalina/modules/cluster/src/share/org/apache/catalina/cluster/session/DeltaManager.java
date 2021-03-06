/*
 * Copyright 1999,2004 The Apache Software Foundation.
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


package org.apache.catalina.cluster.session;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import javax.servlet.ServletContext;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Session;
import org.apache.catalina.util.CustomObjectInputStream;
import org.apache.catalina.util.LifecycleSupport;

import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.cluster.ClusterManager;
import org.apache.catalina.cluster.SessionMessage;
import org.apache.catalina.cluster.Member;
import org.apache.catalina.cluster.CatalinaCluster;


/**
 * The DeltaManager manages replicated sessions by only
 * replicating the deltas in data. For applications written
 * to handle this, the DeltaManager is the optimal way of replicating data.
 *
 * This code is almost identical to StandardManager with a difference in
 * how it persists sessions and some modifications to it.
 *
 * <b>IMPLEMENTATION NOTE</b>:  Correct behavior of session storing and
 * reloading depends upon external calls to the <code>start()</code> and
 * <code>stop()</code> methods of this class at the correct times.
 *
 * @author Filip Hanik
 * @author Craig R. McClanahan
 * @author Jean-Francois Arcand
 * @version $Revision: 1.27 $ $Date: 2004/06/04 20:22:27 $
 */

public class DeltaManager
    extends ManagerBase
    implements Lifecycle, PropertyChangeListener, ClusterManager {

    // ---------------------------------------------------- Security Classes


    public static org.apache.commons.logging.Log log =
        org.apache.commons.logging.LogFactory.getLog( DeltaManager.class );

    // ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information about this implementation.
     */
    private static final String info = "DeltaManager/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The maximum number of active Sessions allowed, or -1 for no limit.
     */
    private int maxActiveSessions = -1;


    /**
     * The descriptive name of this Manager implementation (for logging).
     */
    protected static String managerName = "DeltaManager";
    
    protected String name = null;


    /**
     * Path name of the disk file in which active sessions are saved
     * when we stop, and from which these sessions are loaded when we start.
     * A <code>null</code> value indicates that no persistence is desired.
     * If this pathname is relative, it will be resolved against the
     * temporary working directory provided by our context, available via
     * the <code>javax.servlet.context.tempdir</code> context attribute.
     */
    private String pathname = "SESSIONS.ser";


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    int rejectedSessions=0;
    int expiredSessions=0;
    long processingTime=0;

    private CatalinaCluster cluster = null;
    private boolean stateTransferred;
    private boolean useDirtyFlag;
    private boolean expireSessionsOnShutdown;
    private boolean printToScreen;
    // ------------------------------------------------------------- Constructor
    public DeltaManager() {
        super();
    }

    // ------------------------------------------------------------- Properties


    /**
     * Set the Container with which this Manager has been associated.  If
     * it is a Context (the usual case), listen for changes to the session
     * timeout property.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        // De-register from the old Container (if any)
        if ((this.container != null) && (this.container instanceof Context))
            ((Context) this.container).removePropertyChangeListener(this);

        // Default processing provided by our superclass
        super.setContainer(container);

        // Register with the new Container (if any)
        if ((this.container != null) && (this.container instanceof Context)) {
            setMaxInactiveInterval
                ( ((Context) this.container).getSessionTimeout()*60 );
            ((Context) this.container).addPropertyChangeListener(this);
        }

    }


    /**
     * Return descriptive information about this Manager implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (this.info);

    }


    /**
     * Return the maximum number of active Sessions allowed, or -1 for
     * no limit.
     */
    public int getMaxActiveSessions() {

        return (this.maxActiveSessions);

    }

    /** Number of session creations that failed due to maxActiveSessions
     *
     * @return The count
     */
    public int getRejectedSessions() {
        return rejectedSessions;
    }

    public void setRejectedSessions(int rejectedSessions) {
        this.rejectedSessions = rejectedSessions;
    }

    /** Number of sessions that expired.
     *
     * @return The count
     */
    public int getExpiredSessions() {
        return expiredSessions;
    }

    public void setExpiredSessions(int expiredSessions) {
        this.expiredSessions = expiredSessions;
    }

    public long getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(long processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Set the maximum number of actives Sessions allowed, or -1 for
     * no limit.
     *
     * @param max The new maximum number of sessions
     */
    public void setMaxActiveSessions(int max) {

        int oldMaxActiveSessions = this.maxActiveSessions;
        this.maxActiveSessions = max;
        support.firePropertyChange("maxActiveSessions",
                                   new Integer(oldMaxActiveSessions),
                                   new Integer(this.maxActiveSessions));

    }


    /**
     * Return the descriptive short name of this Manager implementation.
     */
    public String getName() {

        return (name);

    }


    /**
     * Return the session persistence pathname, if any.
     */
    public String getPathname() {

        return (this.pathname);

    }


    /**
     * Set the session persistence pathname to the specified value.  If no
     * persistence support is desired, set the pathname to <code>null</code>.
     *
     * @param pathname New session persistence pathname
     */
    public void setPathname(String pathname) {

        String oldPathname = this.pathname;
        this.pathname = pathname;
        support.firePropertyChange("pathname", oldPathname, this.pathname);

    }


    // --------------------------------------------------------- Public Methods

    /**
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     *
     * Construct and return a new session object, based on the default
     * settings specified by this Manager's properties.  The session
     * id will be assigned by this method, and available via the getId()
     * method of the returned session.  If a new session cannot be created
     * for any reason, return <code>null</code>.
     *
     * @exception IllegalStateException if a new session cannot be
     *  instantiated for any reason
     */
    public Session createSession() {
        return createSession(true);
    }

    public Session createSession(boolean distribute) {

      if ((maxActiveSessions >= 0) &&
          (sessions.size() >= maxActiveSessions)) {
            rejectedSessions++;
            throw new IllegalStateException
                (sm.getString("standardManager.createSession.ise"));
        }

      // Recycle or create a Session instance
      DeltaSession session = getNewDeltaSession();
      String sessionId = generateSessionId();

      String jvmRoute = getJvmRoute();
      // @todo Move appending of jvmRoute generateSessionId()???
      if (jvmRoute != null) {
        sessionId += '.' + jvmRoute;
      }
      synchronized (sessions) {
        while (sessions.get(sessionId) != null) { // Guarantee uniqueness
          duplicates++;
          sessionId = generateSessionId();
          // @todo Move appending of jvmRoute generateSessionId()???
          if (jvmRoute != null) {
            sessionId += '.' + jvmRoute;
          }
        }
      }

      session.setNew(true);
      session.setValid(true);
      session.setCreationTime(System.currentTimeMillis());
      session.setMaxInactiveInterval(this.maxInactiveInterval);
      session.setId(sessionId);
      session.resetDeltaRequest();
      // Initialize the properties of the new session and return it
      
      sessionCounter++;
      
      
      if ( distribute ) {
          SessionMessage msg = new SessionMessageImpl(
              getName(),
              SessionMessage.EVT_SESSION_CREATED,
              null,
              sessionId,
              sessionId+System.currentTimeMillis());
          cluster.send(msg);
          session.resetDeltaRequest();
      }
      log.debug("Created a DeltaSession with Id["+session.getId()+"] Total count="+sessions.size());
      
      return (session);

    }



    /**
     * Get new session class to be used in the doLoad() method.
     */
    protected DeltaSession getNewDeltaSession() {
        return new DeltaSession(this);
    }


    private DeltaRequest loadDeltaRequest(DeltaSession session, byte[] data) throws
        ClassNotFoundException, IOException {
        ByteArrayInputStream fis = null;
        ReplicationStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        //fix to be able to run the DeltaManager
        //stand alone without a container.
        //use the Threads context class loader
        if ( container != null ) 
            loader = container.getLoader();
        if ( loader != null ) 
            classLoader = loader.getClassLoader();
        else
            classLoader = Thread.currentThread().getContextClassLoader();
        //end fix
        fis = new ByteArrayInputStream(data);
        ois = new ReplicationStream(fis,classLoader);
        session.getDeltaRequest().readExternal(ois);
        ois.close();
        return session.getDeltaRequest();
    }
    
    private byte[] unloadDeltaRequest(DeltaRequest deltaRequest) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        deltaRequest.writeExternal(oos);
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }
    /**
     * Load any currently active sessions that were previously unloaded
     * to the appropriate persistence mechanism, if any.  If persistence is not
     * supported, this method returns without doing anything.
     *
     * @exception ClassNotFoundException if a serialized class cannot be
     *  found during the reload
     * @exception IOException if an input/output error occurs
     */
    private void doLoad(byte[] data) throws ClassNotFoundException, IOException {

        // Initialize our internal data structures
        //sessions.clear(); //should not do this
        // Open an input stream to the specified pathname, if any
        ByteArrayInputStream fis = null;
        ObjectInputStream ois = null;
        Loader loader = null;
        ClassLoader classLoader = null;
        try {
            fis = new ByteArrayInputStream(data);
            BufferedInputStream bis = new BufferedInputStream(fis);
            if (container != null)
                loader = container.getLoader();
            if (loader != null)
                classLoader = loader.getClassLoader();
            if (classLoader != null) {
                ois = new CustomObjectInputStream(bis, classLoader);
            } else {
                ois = new ObjectInputStream(bis);
            }
        } catch (IOException e) {
            log.error(sm.getString("standardManager.loading.ioe", e), e);
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException f) {
                    ;
                }
                ois = null;
            }
            throw e;
        }

        // Load the previously unloaded active sessions
        synchronized (sessions) {
            try {
                Integer count = (Integer) ois.readObject();
                int n = count.intValue();
                for (int i = 0; i < n; i++) {
                    DeltaSession session = getNewDeltaSession();
                    session.readObjectData(ois);
                    session.setManager(this);
                    session.setValid(true);
                    session.setPrimarySession(false);
                    //in case the nodes in the cluster are out of 
                    //time synch, this will make sure that we have the
                    //correct timestamp, isValid returns true, cause accessCount=1
                    session.access();
                    //make sure that the session gets ready to expire if needed
                    session.setAccessCount(0);
                    sessions.put(session.getId(), session);
                }
            } catch (ClassNotFoundException e) {
              log.error(sm.getString("standardManager.loading.cnfe", e), e);
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException f) {
                        ;
                    }
                    ois = null;
                }
                throw e;
            } catch (IOException e) {
              log.error(sm.getString("standardManager.loading.ioe", e), e);
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException f) {
                        ;
                    }
                    ois = null;
                }
                throw e;
            } finally {
                // Close the input stream
                try {
                    if (ois != null)
                        ois.close();
                } catch (IOException f) {
                    // ignored
                }

            }
        }

    }





    /**
     * Save any currently active sessions in the appropriate persistence
     * mechanism, if any.  If persistence is not supported, this method
     * returns without doing anything.
     *
     * @exception IOException if an input/output error occurs
     */
    private byte[] doUnload() throws IOException {


        // Open an output stream to the specified pathname, if any
        ByteArrayOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(new BufferedOutputStream(fos));
        } catch (IOException e) {
            log.error(sm.getString("standardManager.unloading.ioe", e), e);
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException f) {
                    ;
                }
                oos = null;
            }
            throw e;
        }

        // Write the number of active sessions, followed by the details
        ArrayList list = new ArrayList();
        synchronized (sessions) {
            try {
                oos.writeObject(new Integer(sessions.size()));
                Iterator elements = sessions.values().iterator();
                while (elements.hasNext()) {
                    DeltaSession session =
                        (DeltaSession) elements.next();
                    list.add(session);
                    session.writeObjectData(oos);
                }
                oos.flush();
                oos.close();
                oos = null;
            } catch (IOException e) {
                log.error(sm.getString("standardManager.unloading.ioe", e), e);
                if (oos != null) {
                    try {
                        oos.close();
                    } catch (IOException f) {
                        ;
                    }
                    oos = null;
                }
                throw e;
            }
        }

        // Flush and close the output stream
        return fos.toByteArray();
    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }

    /**
     * Prepare for the beginning of active use of the public methods of this
     * component.  This method should be called after <code>configure()</code>,
     * and before any of the public methods of the component are utilized.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that prevents this component from being used
     */
    public void start() throws LifecycleException {
        if( ! initialized )
            init();

        // Validate and update our current component state
        if (started) {
            return;
        }
        started = true;
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        

        // Force initialization of the random number generator
        String dummy = generateSessionId();

        // Load unloaded sessions, if any
        try {
            //the channel is already running
            log.info("Starting clustering manager...:"+getName());
            if ( cluster == null ) {
                log.error("Starting... no cluster associated with this context:"+getName());
                return;
            }

            if (cluster.getMembers().length > 0) {
                Member mbr = cluster.getMembers()[0];
                SessionMessage msg =
                    new SessionMessageImpl(this.getName(),
                                       SessionMessage.EVT_GET_ALL_SESSIONS,
                                       null,
                                       "GET-ALL",
                                       "GET-ALL-"+getName());
                //just to make sure the other server has the context started
//                long timetowait = 20000-mbr.getMemberAliveTime();
//                if ( timetowait > 0 ) {
//                    log.info("The other server has not been around more than 20 seconds, will sleep for "+timetowait+" ms. in order to let it startup");
//                    try { Thread.currentThread().sleep(timetowait); } catch ( Exception x ) {}
//                }//end if
                
                //request session state
                cluster.send(msg, mbr);
                log.warn("Manager["+getName()+"], requesting session state from "+mbr+
                         ". This operation will timeout if no session state has been received within "+
                         "60 seconds");
                long reqStart = System.currentTimeMillis();
                long reqNow = 0;
                boolean isTimeout=false;
                do {
                    try {
                        Thread.currentThread().sleep(100);
                    }catch ( Exception sleep) {}
                    reqNow = System.currentTimeMillis();
                    isTimeout=((reqNow-reqStart)>(1000*60));
                } while ( (!getStateTransferred()) && (!isTimeout));
                if ( isTimeout || (!getStateTransferred()) ) {
                    log.error("Manager["+getName()+"], No session state received, timing out.");
                }else {
                    log.info("Manager["+getName()+"], session state received in "+(reqNow-reqStart)+" ms.");
                }
            } else {
                log.info("Manager["+getName()+"], skipping state transfer. No members active in cluster group.");
            }//end if

        } catch (Throwable t) {
            log.error(sm.getString("standardManager.managerLoad"), t);
        }

    }


    /**
     * Gracefully terminate the active use of the public methods of this
     * component.  This method should be the last one called on a given
     * instance of this component.
     *
     * @exception LifecycleException if this component detects a fatal error
     *  that needs to be reported
     */
    public void stop() throws LifecycleException {

        if (log.isDebugEnabled())
            log.debug("Stopping");

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("standardManager.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Expire all active sessions
        if ( this.getExpireSessionsOnShutdown() ) {
            log.info("Expiring sessions upon shutdown");
            Session sessions[] = findSessions();
            for (int i = 0; i < sessions.length; i++) {
                DeltaSession session = (DeltaSession) sessions[i];
                if (!session.isValid())
                    continue;
                try {
                    session.expire();
                }
                catch (Throwable t) {
                    ;
                } //catch
            } //for
        }//end if

        // Require a new random number generator if we are restarted
        this.random = null;

        if( initialized ) {
            destroy();
        }
        getCluster().removeManager(getName());
    }


    // ----------------------------------------- PropertyChangeListener Methods


    /**
     * Process property change events from our associated Context.
     *
     * @param event The property change event that has occurred
     */
    public void propertyChange(PropertyChangeEvent event) {

        // Validate the source of this event
        if (!(event.getSource() instanceof Context))
            return;
        Context context = (Context) event.getSource();

        // Process a relevant property change
        if (event.getPropertyName().equals("sessionTimeout")) {
            try {
                setMaxInactiveInterval
                    ( ((Integer) event.getNewValue()).intValue()*60 );
            } catch (NumberFormatException e) {
                log.error(sm.getString("standardManager.sessionTimeout",
                                 event.getNewValue().toString()));
            }
        }

    }

    // -------------------------------------------------------- Replication Methods

    /**
        * A message was received from another node, this
        * is the callback method to implement if you are interested in
        * receiving replication messages.
        * @param msg - the message received.
        */
       public void messageDataReceived(SessionMessage msg) {
           messageReceived(msg, msg.getAddress()!=null?(Member)msg.getAddress():null);
       }

       /**
        * When the request has been completed, the replication valve
        * will notify the manager, and the manager will decide whether
        * any replication is needed or not.
        * If there is a need for replication, the manager will
        * create a session message and that will be replicated.
        * The cluster determines where it gets sent.
        * @param sessionId - the sessionId that just completed.
        * @return a SessionMessage to be sent,
        */
       public SessionMessage requestCompleted(String sessionId) {
           try {
               DeltaSession session = (DeltaSession) findSession(sessionId);
               DeltaRequest deltaRequest = session.getDeltaRequest();
               SessionMessage msg = null;
               if (deltaRequest.getSize() > 0) {
   
                   byte[] data = unloadDeltaRequest(deltaRequest);
                   msg = new SessionMessageImpl(name, SessionMessage.EVT_SESSION_DELTA,
                                            data, sessionId,
                                            sessionId+System.currentTimeMillis());
                   session.resetDeltaRequest();
               } else if ( !session.isPrimarySession() ) {
                   msg = new SessionMessageImpl(getName(),
                                         SessionMessage.EVT_SESSION_ACCESSED,
                                         null,
                                         sessionId,
                                         sessionId+System.currentTimeMillis());
               }
               session.setPrimarySession(true);
               //check to see if we need to send out an access message
               if ( (msg == null) ) {
                   long replDelta = System.currentTimeMillis() - session.getLastTimeReplicated();
                   if ( replDelta > (getMaxInactiveInterval()*1000) ) {
                       msg = new SessionMessageImpl(getName(),
                                             SessionMessage.EVT_SESSION_ACCESSED,
                                             null,
                                             sessionId,
                                             sessionId+System.currentTimeMillis());
                   }
                   
               }
               
               //update last replicated time
               if ( msg != null ) session.setLastTimeReplicated(System.currentTimeMillis());
               return msg;
           }
           catch (IOException x) {
               log.error("Unable to serialize delta request", x);
               return null;
           }
   
       }
       
       protected void sessionExpired(String id) {
           SessionMessage msg = new SessionMessageImpl(getName(), 
                                                   SessionMessage.EVT_SESSION_EXPIRED,
                                                   null,
                                                   id,
                                                   id+"-EXPIRED-MSG");
           cluster.send(msg);
       }
   
       /**
        * When the manager expires session not tied to a request.
        * The cluster will periodically ask for a list of sessions
        * that should expire and that should be sent across the wire.
        * @return
        */
       public String[] getInvalidatedSessions() {
           return new String[0];
       }


       /**
        * This method is called by the received thread when a SessionMessage has
        * been received from one of the other nodes in the cluster.
        * @param msg - the message received
        * @param sender - the sender of the message, this is used if we receive a
        *                 EVT_GET_ALL_SESSION message, so that we only reply to
        *                 the requesting node
        */
       protected void messageReceived(SessionMessage msg, Member sender) {
           try {
               log.debug("Manager ("+name+") Received SessionMessage of type=" + msg.getEventTypeString()+" from "+sender);
               switch (msg.getEventType()) {
                   case SessionMessage.EVT_GET_ALL_SESSIONS: {
                       //get a list of all the session from this manager
                       log.debug("Manager ("+name+") unloading sessions");
                       byte[] data = doUnload();
                       log.debug("Manager ("+name+") unloading sessions complete");
                       SessionMessage newmsg = new SessionMessageImpl(name,
                           SessionMessage.EVT_ALL_SESSION_DATA,
                           data, "SESSION-STATE","SESSION-STATE-"+getName());
                       cluster.send(newmsg, sender);
                       break;
                   }
                   case SessionMessage.EVT_ALL_SESSION_DATA: {
                       log.debug("Manager ("+name+") received session state data.");
                       byte[] data = msg.getSession();
                       doLoad(data);
                       log.debug("Manager ("+name+") state deserialized.");
                       stateTransferred = true;
                       break;
                   }
                   case SessionMessage.EVT_SESSION_CREATED: {
                       DeltaSession session = (DeltaSession)createSession(false);
                       session.setId(msg.getSessionID());
                       session.setNew(false);
                       session.setPrimarySession(false);
                       session.resetDeltaRequest();
                       break;
                   }
                   case SessionMessage.EVT_SESSION_EXPIRED: {
                       DeltaSession session = (DeltaSession)findSession(msg.getSessionID());
                       if (session != null) {
                           session.expire(true,false);
                       } //end if
                       break;
                   }
                   case SessionMessage.EVT_SESSION_ACCESSED: {
                       DeltaSession session = (DeltaSession)findSession(msg.getSessionID());
                       if (session != null) {
                           session.access();
                           session.setPrimarySession(false);
                           session.endAccess();
                       }
                       break;
                   }
                   case SessionMessage.EVT_SESSION_DELTA : {
                       byte[] delta = msg.getSession();
                       DeltaSession session = (DeltaSession)findSession(msg.getSessionID());
                       if (session != null) {
                           DeltaRequest dreq = loadDeltaRequest(session, delta);
                           dreq.execute(session);
                           session.setPrimarySession(false);
                       }
                       
                       break;
                   }
                   default: {
                       //we didn't recognize the message type, do nothing
                       break;
                   }
               } //switch
           }
           catch (Exception x) {
               log.error("Unable to receive message through TCP channel", x);
           }
       }



    // -------------------------------------------------------- Private Methods

    public void backgroundProcess() {
        log.debug("DeltaManager.backgroundProcess invoked at "+System.currentTimeMillis());
        processExpires();
    }
    /**
     * Invalidate all sessions that have expired.
     */
    public void processExpires() {
        long timeNow = System.currentTimeMillis();
        Session sessions[] = findSessions();

        for (int i = 0; i < sessions.length; i++) {
            DeltaSession session = (DeltaSession) sessions[i];
            if (!session.isValid()) {
                try {
                    expiredSessions++;
                } catch (Throwable t) {
                    log.error(sm.getString
                              ("standardManager.expireException"), t);
                }
            }
        }
        long timeEnd = System.currentTimeMillis();
        processingTime += ( timeEnd - timeNow );
    }

    public boolean getStateTransferred() {
        return stateTransferred;
    }

    public void setStateTransferred(boolean stateTransferred) {
        this.stateTransferred = stateTransferred;
    }

    public CatalinaCluster getCluster() {
        return cluster;
    }

    public void setCluster(CatalinaCluster cluster) {
        this.cluster = cluster;
    }

    public void load() {

    }

    public void unload() {

    }
    public boolean getUseDirtyFlag() {
        return useDirtyFlag;
    }
    public void setUseDirtyFlag(boolean useDirtyFlag) {
        this.useDirtyFlag = useDirtyFlag;
    }
    public boolean getExpireSessionsOnShutdown() {
        return expireSessionsOnShutdown;
    }
    public void setExpireSessionsOnShutdown(boolean expireSessionsOnShutdown) {
        this.expireSessionsOnShutdown = expireSessionsOnShutdown;
    }
    public boolean getPrintToScreen() {
        return printToScreen;
    }
    public void setPrintToScreen(boolean printToScreen) {
        this.printToScreen = printToScreen;
    }
    public void setName(String name) {
        this.name = name;
    }


}
