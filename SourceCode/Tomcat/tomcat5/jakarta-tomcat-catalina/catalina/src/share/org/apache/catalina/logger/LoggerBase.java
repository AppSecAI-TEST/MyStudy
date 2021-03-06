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


package org.apache.catalina.logger;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.modeler.Registry;


/**
 * Convenience base class for <b>Logger</b> implementations.  The only
 * method that must be implemented is <code>log(String msg)</code>, plus
 * any property setting and lifecycle methods required for configuration.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.9 $ $Date: 2004/05/26 15:48:29 $
 */

public class LoggerBase
    implements Lifecycle, Logger, MBeanRegistration 
 {
    private static Log log = LogFactory.getLog(LoggerBase.class);
    
    // ----------------------------------------------------- Instance Variables


    /**
     * The Container with which this Logger has been associated.
     */
    protected Container container = null;


    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    
    /**
     * The descriptive information about this implementation.
     */
    protected static final String info =
        "org.apache.catalina.logger.LoggerBase/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The verbosity level for above which log messages may be filtered.
     */
    protected int verbosity = ERROR;


    // ------------------------------------------------------------- Properties


    /**
     * Return the Container with which this Logger has been associated.
     */
    public Container getContainer() {

        return (container);

    }


    /**
     * Set the Container with which this Logger has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (this.debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return descriptive information about this Logger implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     */
    public int getVerbosity() {

        return (this.verbosity);

    }


    /**
     * Set the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     *
     * @param verbosity The new verbosity level
     */
    public void setVerbosity(int verbosity) {

        this.verbosity = verbosity;

    }


    /**
     * Set the verbosity level of this logger.  Messages logged with a
     * higher verbosity than this level will be silently ignored.
     *
     * @param verbosity The new verbosity level, as a string
     */
    public void setVerbosityLevel(String verbosity) {

        if ("FATAL".equalsIgnoreCase(verbosity))
            this.verbosity = FATAL;
        else if ("ERROR".equalsIgnoreCase(verbosity))
            this.verbosity = ERROR;
        else if ("WARNING".equalsIgnoreCase(verbosity))
            this.verbosity = WARNING;
        else if ("INFORMATION".equalsIgnoreCase(verbosity))
            this.verbosity = INFORMATION;
        else if ("DEBUG".equalsIgnoreCase(verbosity))
            this.verbosity = DEBUG;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        support.addPropertyChangeListener(listener);

    }


    /**
     * Writes the specified message to a servlet log file, usually an event
     * log.  The name and type of the servlet log is specific to the
     * servlet container.  This message will be logged unconditionally.
     *
     * @param msg A <code>String</code> specifying the message to be
     *  written to the log file
     */
    public void log(String msg) {
        log.debug(msg);
    }


    /**
     * Writes the specified exception, and message, to a servlet log file.
     * The implementation of this method should call
     * <code>log(msg, exception)</code> instead.  This method is deprecated
     * in the ServletContext interface, but not deprecated here to avoid
     * many useless compiler warnings.  This message will be logged
     * unconditionally.
     *
     * @param exception An <code>Exception</code> to be reported
     * @param msg The associated message string
     */
    public void log(Exception exception, String msg) {

        log(msg, exception);

    }


    /**
     * Writes an explanatory message and a stack trace for a given
     * <code>Throwable</code> exception to the servlet log file.  The name
     * and type of the servlet log file is specific to the servlet container,
     * usually an event log.  This message will be logged unconditionally.
     *
     * @param msg A <code>String</code> that describes the error or
     *  exception
     * @param throwable The <code>Throwable</code> error or exception
     */
    public void log(String msg, Throwable throwable) {

        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println(msg);
        throwable.printStackTrace(writer);
        Throwable rootCause = null;
        if (throwable instanceof LifecycleException)
            rootCause = ((LifecycleException) throwable).getThrowable();
        else if (throwable instanceof ServletException)
            rootCause = ((ServletException) throwable).getRootCause();
        if (rootCause != null) {
            writer.println("----- Root Cause -----");
            rootCause.printStackTrace(writer);
        }
        log(buf.toString());

    }


    /**
     * Writes the specified message to the servlet log file, usually an event
     * log, if the logger is set to a verbosity level equal to or higher than
     * the specified value for this message.
     *
     * @param message A <code>String</code> specifying the message to be
     *  written to the log file
     * @param verbosity Verbosity level of this message
     */
    public void log(String message, int verbosity) {

        if (this.verbosity >= verbosity)
            log(message);

    }


    /**
     * Writes the specified message and exception to the servlet log file,
     * usually an event log, if the logger is set to a verbosity level equal
     * to or higher than the specified value for this message.
     *
     * @param message A <code>String</code> that describes the error or
     *  exception
     * @param throwable The <code>Throwable</code> error or exception
     * @param verbosity Verbosity level of this message
     */
    public void log(String message, Throwable throwable, int verbosity) {

        if (this.verbosity >= verbosity)
            log(message, throwable);

    }


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        support.removePropertyChangeListener(listener);

    }
                                                                               
    protected String domain;
    protected String host;
    protected String path;
    protected ObjectName oname;
    protected ObjectName controller;
    protected MBeanServer mserver;

    public ObjectName getController() {
        return controller;
    }

    public void setController(ObjectName controller) {
        this.controller = controller;
    }

    public ObjectName getObjectName() {
        return oname;
    }
          
    public String getDomain() {
        return domain;
    }
    
    public ObjectName preRegister(MBeanServer server,
                                  ObjectName name) throws Exception {
        oname=name;
        mserver=server;
        // FIXME null pointer exception 
        if (name == null) {
            return null;
        }
        domain=name.getDomain();
        host=name.getKeyProperty("host");
        path=name.getKeyProperty("path");
        if( container== null ) {
            // Register with the parent
            try {
                ObjectName cname=null;
                if( host == null ) {
                    // global
                    cname=new ObjectName(domain +":type=Engine");
                } else if( path==null ) {
                    cname=new ObjectName(domain +
                            ":type=Host,host=" + host);
                } else {
                    cname=new ObjectName(domain +":j2eeType=WebModule,name=//" +
                            host + "/" + path);
                }
                log.debug("Register with " + cname);
                mserver.invoke(cname, "setLogger", new Object[] {this},
                        new String[] {"org.apache.catalina.Logger"});
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        } 
                
        return name;
    }

    public void postRegister(Boolean registrationDone) {
    }

    public void preDeregister() throws Exception {
    }

    public void postDeregister() {
    }

    public void init() {
        
    }
    
    public void destroy() {
        
    }
    
    public ObjectName createObjectName() {
        // register
        try {
            StandardEngine engine=null;            
            String suffix="";
            if( container instanceof StandardEngine ) {
                engine=(StandardEngine)container;                
            } else if( container instanceof StandardHost ) {
                engine=(StandardEngine)container.getParent();
                suffix=",host=" + container.getName();
            } else if( container instanceof StandardContext ) {
                String path = ((StandardContext)container).getPath();
                // add "/" to avoid MalformedObjectName Exception
                if (path.equals("")) {
                    path = "/";
                }
                engine=(StandardEngine)container.getParent().getParent();
                suffix= ",path=" + path + ",host=" + 
                        container.getParent().getName();
            } else {
                log.error("Unknown container " + container );
            }
            if( engine != null ) {
                oname=new ObjectName(engine.getDomain()+ ":type=Logger" + suffix);
            } else {
                log.error("Null engine !! " + container);
            }
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        return oname;
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
     * @param listener The listener to add
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
                                                                
        // register this logger
        if ( getObjectName()==null ) {   
            ObjectName oname = createObjectName();   
            try {   
                Registry.getRegistry(null, null).registerComponent(this, oname, null); 
                log.debug( "registering logger " + oname );   
            } catch( Exception ex ) {   
                log.error( "Can't register logger " + oname, ex);   
            }      
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

        // unregister this logger
        if ( getObjectName()!=null ) {   
            ObjectName oname = createObjectName();   
            try {   
                Registry.getRegistry(null, null).unregisterComponent(oname); 
                log.info( "unregistering logger " + oname );   
            } catch( Exception ex ) {   
                log.error( "Can't unregister logger " + oname, ex);   
            }      
        }  
    }
  
}
