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


package org.apache.catalina.core;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Deployer;
import org.apache.catalina.Engine;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.startup.ContextRuleSet;
import org.apache.catalina.startup.ExpandWar;
import org.apache.catalina.startup.NamingRuleSet;
import org.apache.catalina.util.CatalinaDigester;
import org.apache.catalina.util.StringManager;
import org.apache.commons.digester.Digester;


/**
 * <p>Implementation of <b>Deployer</b> that is delegated to by the
 * <code>StandardHost</code> implementation class.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.23 $ $Date: 2004/04/12 22:05:15 $
 */

public class StandardHostDeployer implements Deployer {

    private static org.apache.commons.logging.Log log=
        org.apache.commons.logging.LogFactory.getLog( StandardHostDeployer.class );

    // ----------------------------------------------------------- Constructors

    public StandardHostDeployer() {
    }

    /**
     * Create a new StandardHostDeployer associated with the specified
     * StandardHost.
     *
     * @param host The StandardHost we are associated with
     */
    public StandardHostDeployer(StandardHost host) {

        super();
        this.host = host;

    }


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>ContextRuleSet</code> associated with our
     * <code>digester</code> instance.
     */
    private ContextRuleSet contextRuleSet = null;


     /**
     * The <code>Digester</code> instance to use for deploying web applications
     * to this <code>Host</code>.  <strong>WARNING</strong> - Usage of this
     * instance must be appropriately synchronized to prevent simultaneous
     * access by multiple threads.
     */
    private Digester digester = null;


    /**
     * The <code>StandardHost</code> instance we are associated with.
     */
    protected StandardHost host = null;


    /**
     * The <code>NamingRuleSet</code> associated with our
     * <code>digester</code> instance.
     */
    private NamingRuleSet namingRuleSet = null;


    /**
     * The document base which should replace the value specified in the
     * <code>Context</code> being added in the <code>addChild()</code> method,
     * or <code>null</code> if the original value should remain untouched.
     */
    private String overrideDocBase = null;


    /**
     * The config file which should replace the value set for the config file
     * of the <code>Context</code>being added in the <code>addChild()</code> 
     * method, or <code>null</code> if the original value should remain 
     * untouched.
     */
    private String overrideConfigFile = null;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    // -------------------------------------------------------- Depoyer Methods

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = (StandardHost)host;
    }

    /**
     * Return the name of the Container with which this Deployer is associated.
     */
    public String getName() {

        return (host.getName());

    }


    /**
     * Install a new web application, whose web application archive is at the
     * specified URL, into this container with the specified context path.
     * A context path of "" (the empty string) should be used for the root
     * application for this container.  Otherwise, the context path must
     * start with a slash.
     * <p>
     * If this application is successfully installed, a ContainerEvent of type
     * <code>PRE_INSTALL_EVENT</code> will be sent to registered listeners
     * before the associated Context is started, and a ContainerEvent of type
     * <code>INSTALL_EVENT</code> will be sent to all registered listeners
     * after the associated Context is started, with the newly created
     * <code>Context</code> as an argument.
     *
     * @param contextPath The context path to which this application should
     *  be installed (must be unique)
     * @param war A URL of type "jar:" that points to a WAR file, or type
     *  "file:" that points to an unpacked directory structure containing
     *  the web application to be installed
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalStateException if the specified context path
     *  is already attached to an existing web application
     * @exception IOException if an input/output error was encountered
     *  during installation
     */
    public synchronized void install(String contextPath, URL war)
        throws IOException {

        // Validate the format and state of our arguments
        if (contextPath == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathRequired"));
        if (!contextPath.equals("") && !contextPath.startsWith("/"))
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathFormat", contextPath));
        if (findDeployedApp(contextPath) != null)
            throw new IllegalStateException
                (sm.getString("standardHost.pathUsed", contextPath));
        if (war == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.warRequired"));

        // Calculate the document base for the new web application
        log.info(sm.getString("standardHost.installing",
                              contextPath, war.toString()));
        String url = war.toString();
        String docBase = null;
        boolean isWAR = false;
        if (url.startsWith("jar:")) {
            url = url.substring(4, url.length() - 2);
            if (!url.toLowerCase().endsWith(".war")) {
                throw new IllegalArgumentException
                    (sm.getString("standardHost.warURL", url));
            }
            isWAR = true;
        }
        if (url.startsWith("file://"))
            docBase = url.substring(7);
        else if (url.startsWith("file:"))
            docBase = url.substring(5);
        else
            throw new IllegalArgumentException
                (sm.getString("standardHost.warURL", url));

        // Determine if directory/war to install is in the host appBase
        boolean isAppBase = false;
        File appBase = new File(host.getAppBase());
        if (!appBase.isAbsolute())
            appBase = new File(System.getProperty("catalina.base"),
                            host.getAppBase());
        File contextFile = new File(docBase);
        File baseDir = contextFile.getParentFile();
        if (appBase.getCanonicalPath().equals(baseDir.getCanonicalPath())) {
            isAppBase = true;
        }

        // For security, if deployXML is false only allow directories
        // and war files from the hosts appBase
        if (!host.isDeployXML() && !isAppBase) {
            throw new IllegalArgumentException
                (sm.getString("standardHost.installBase", url));
        }

        // Make sure contextPath and directory/war names match when
        // installing from the host appBase
        if (isAppBase && host.getAutoDeploy()) {
            String filename = contextFile.getName();
            if (isWAR) {
                filename = filename.substring(0,filename.length()-4);
            }
            if (contextPath.length() == 0) {
                if (!filename.equals("ROOT")) {
                    throw new IllegalArgumentException
                        (sm.getString("standardHost.pathMatch", "/", "ROOT"));
                }
            } else if (!filename.equals(contextPath.substring(1))) {
                throw new IllegalArgumentException
                    (sm.getString("standardHost.pathMatch", contextPath, filename));
            }
        }

        // Expand war file if host wants wars unpacked
        if (isWAR && host.isUnpackWARs()) {
            if (contextPath.equals("")) {
                docBase = ExpandWar.expand(host, war, "/ROOT");
            } else {
                docBase = ExpandWar.expand(host, war, contextPath);
            }
        }

        // Install the new web application
        try {
            Class clazz = Class.forName(host.getContextClass());
            Context context = (Context) clazz.newInstance();
            context.setPath(contextPath);
            context.setDocBase(docBase);
            if (context instanceof Lifecycle) {
                clazz = Class.forName(host.getConfigClass());
                LifecycleListener listener =
                    (LifecycleListener) clazz.newInstance();
                ((Lifecycle) context).addLifecycleListener(listener);
            }
            host.fireContainerEvent(PRE_INSTALL_EVENT, context);
            host.addChild(context);
            host.fireContainerEvent(INSTALL_EVENT, context);
        } catch (ClassNotFoundException e) {
            log.info("", e);
        } catch (Exception e) {
            log.info("Error installing", e);
            throw new IOException(e.toString());
        }

    }


    /**
     * Install a new web application, whose web application archive is at the
     * specified URL, into this container with the specified context path.
     * A context path of "" (the empty string) should be used for the root
     * application for this container.  Otherwise, the context path must
     * start with a slash.
     * <p>
     * If this application is successfully installed, a ContainerEvent of type
     * <code>PRE_INSTALL_EVENT</code> will be sent to registered listeners
     * before the associated Context is started, and a ContainerEvent of type
     * <code>INSTALL_EVENT</code> will be sent to all registered listeners
     * after the associated Context is started, with the newly created
     * <code>Context</code> as an argument.
     *
     * @param contextPath The context path to which this application should
     *  be installed (must be unique)
     * @param war A URL of type "jar:" that points to a WAR file, or type
     *  "file:" that points to an unpacked directory structure containing
     *  the web application to be installed
     * @param configFile The path to a file to save the Context information.
     *  If configFile is null, the Context information is saved in server.xml;
     *  if it is NOT null, the Context information is saved in configFile.
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalStateException if the specified context path
     *  is already attached to an existing web application
     * @exception IOException if an input/output error was encountered
     *  during installation
     */
    public synchronized void install(String contextPath, URL war,
        String configFile) throws IOException {

        // Validate the format and state of our arguments
        if (contextPath == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathRequired"));
        if (!contextPath.equals("") && !contextPath.startsWith("/"))
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathFormat", contextPath));
        if (findDeployedApp(contextPath) != null)
            throw new IllegalStateException
                (sm.getString("standardHost.pathUsed", contextPath));
        if (war == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.warRequired"));

        // Calculate the document base for the new web application
        log.info(sm.getString("standardHost.installing",
                              contextPath, war.toString()));
        String url = war.toString();
        String docBase = null;
        boolean isWAR = false;
        if (url.startsWith("jar:")) {
            url = url.substring(4, url.length() - 2);
            if (!url.toLowerCase().endsWith(".war")) {
                throw new IllegalArgumentException
                    (sm.getString("standardHost.warURL", url));
            }
            isWAR = true;
        }
        if (url.startsWith("file://"))
            docBase = url.substring(7);
        else if (url.startsWith("file:"))
            docBase = url.substring(5);
        else
            throw new IllegalArgumentException
                (sm.getString("standardHost.warURL", url));

        // Expand war file if host wants wars unpacked
        if (isWAR && host.isUnpackWARs()) {
            if (contextPath.equals("")) {
                docBase = ExpandWar.expand(host, war, "/ROOT");
            } else {
                docBase = ExpandWar.expand(host, war, contextPath);
            }
        }

        // Install the new web application
        try {
            Class clazz = Class.forName(host.getContextClass());
            Context context = (Context) clazz.newInstance();
            context.setPath(contextPath);
            context.setDocBase(docBase);
            context.setConfigFile(configFile);
            if (context instanceof Lifecycle) {
                clazz = Class.forName(host.getConfigClass());
                LifecycleListener listener =
                    (LifecycleListener) clazz.newInstance();
                ((Lifecycle) context).addLifecycleListener(listener);
            }
            host.fireContainerEvent(PRE_INSTALL_EVENT, context);
            host.addChild(context);
            host.fireContainerEvent(INSTALL_EVENT, context);

            // save context info into configFile
            Engine engine = (Engine)host.getParent();
            StandardServer server = (StandardServer) engine.getService().getServer();
            //server.storeContext(context);
        } catch (Exception e) {
            log.error(sm.getString("standardHost.installError", contextPath),
                      e);
            throw new IOException(e.toString());
        }

    }


    /**
     * <p>Install a new web application, whose context configuration file
     * (consisting of a <code>&lt;Context&gt;</code> element) and (optional)
     * web application archive are at the specified URLs.</p>
     *
     * If this application is successfully installed, a ContainerEvent of type
     * <code>PRE_INSTALL_EVENT</code> will be sent to registered listeners
     * before the associated Context is started, and a ContainerEvent of type
     * <code>INSTALL_EVENT</code> will be sent to all registered listeners
     * after the associated Context is started, with the newly created
     * <code>Context</code> as an argument.
     *
     * @param config A URL that points to the context configuration descriptor
     *  to be used for configuring the new Context
     * @param war A URL of type "jar:" that points to a WAR file, or type
     *  "file:" that points to an unpacked directory structure containing
     *  the web application to be installed, or <code>null</code> to use
     *  the <code>docBase</code> attribute from the configuration descriptor
     *
     * @exception IllegalArgumentException if one of the specified URLs is
     *  null
     * @exception IllegalStateException if the context path specified in the
     *  context configuration file is already attached to an existing web
     *  application
     * @exception IOException if an input/output error was encountered
     *  during installation
     */
    public synchronized void install(URL config, URL war) throws IOException {

        // Validate the format and state of our arguments
        if (config == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.configRequired"));

        if (!host.isDeployXML())
            throw new IllegalArgumentException
                (sm.getString("standardHost.configNotAllowed"));

        log.info(sm.getString("standardHost.installingXML", config));

        // Calculate the document base for the new web application (if needed)
        String docBase = null; // Optional override for value in config file
        boolean isWAR = false;
        if (war != null) {
            String url = war.toString();
            log.info(sm.getString("standardHost.installingWAR", url));
            // Calculate the WAR file absolute pathname
            if (url.startsWith("jar:")) {
                url = url.substring(4, url.length() - 2);
                isWAR = true;
            }
            if (url.startsWith("file://"))
                docBase = url.substring(7);
            else if (url.startsWith("file:"))
                docBase = url.substring(5);
            else
                throw new IllegalArgumentException
                    (sm.getString("standardHost.warURL", url));

        }

        // Expand war file if host wants wars unpacked
        if (isWAR && host.isUnpackWARs()) {
            docBase = ExpandWar.expand(host, war);
        }

        // Install the new web application
        this.overrideDocBase = docBase;
        String configPath = config.toString();
        if (configPath.startsWith("file:")) {
            if (configPath.startsWith("file://")) {
                configPath = configPath.substring(7);
            } else {
                configPath = configPath.substring(5);
            }
            this.overrideConfigFile = (new File(configPath)).getAbsolutePath();
        } else {
            configPath = null;
        }

        InputStream stream = null;
        try {
            if (configPath == null) {
                stream = config.openStream();
            } else {
                stream = new FileInputStream(configPath);
            }
            Digester digester = createDigester();
            digester.setClassLoader(this.getClass().getClassLoader());
            digester.clear();
            digester.push(this);
            digester.parse(stream);
            stream.close();
            stream = null;
        } catch (Exception e) {
            host.log
                (sm.getString("standardHost.installError", docBase), e);
            throw new IOException(e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {
                    ;
                }
            }
            this.overrideDocBase = null;
            this.overrideConfigFile = null;
        }

    }


    /**
     * Installs a new web application from the web application archive at the
     * specified URL, which must contain a META-INF/context.xml context
     * configuration file (consisting of a <code>&lt;Context&gt;</code>
     * element).
     *
     * <p>The web application is installed at the path specified inside the 
     * embedded META-INF/context.xml. The docBase (if any) specified inside the
     * embedded META-INF/context.xml is overridden with the web application's
     * location.
     *
     * <p>If the installation succeeds, a ContainerEvent of type
     * <code>INSTALL_EVENT</code> is sent to all registered listeners,
     * with the newly created <code>Context</code> as its argument.
     *
     * @param war URL pointing to web application location (WAR-packaged or
     *        unpacked directory)
     *
     * @exception IllegalArgumentException if <code>war</code> is null, or if
     *            the deployment host does not support any context.xml
     *            configuration files
     * @exception IllegalStateException if the context path specified in the
     *            context configuration file is already in use by an existing
     *            web application
     * @exception IOException if an input/output error was encountered
     *            during installation
     */
    public synchronized void install(URL war) throws IOException {

        if (war == null) {
            throw new IllegalArgumentException
                (sm.getString("standardHost.warRequired"));
        }

        if (!host.isDeployXML()) {
            throw new IllegalArgumentException
                (sm.getString("standardHost.configNotAllowed"));
        }

        // Calculate the document base for the new web application (if needed)
        String docBase = null; // Optional override for value in config file
        boolean isWAR = false;
        String url = war.toString();
        log.info(sm.getString("standardHost.installingWAR", url));
        // Calculate the WAR file absolute pathname
        if (url.startsWith("jar:")) {
            url = url.substring("jar:".length(), url.length() - 2);
            isWAR = true;
        }
        if (url.startsWith("file://")) {
            docBase = url.substring("file://".length());
        } else if (url.startsWith("file:")) {
            docBase = url.substring("file:".length());
        } else {
            throw new IllegalArgumentException
                (sm.getString("standardHost.warURL", url));
        }

        // Expand war file if host wants wars unpacked
        if (isWAR && host.isUnpackWARs()) {
            docBase = ExpandWar.expand(host, war);
        }

        // Install the new web application
        this.overrideDocBase = docBase;

        InputStream stream = null;
        try {
            URL contextXml = new URL(war.toString() + "META-INF/context.xml");
            log.info(sm.getString("standardHost.installingXML", contextXml));
            stream = contextXml.openStream();
            Digester digester = createDigester();
            digester.setClassLoader(this.getClass().getClassLoader());
            digester.clear();
            digester.push(this);
            digester.parse(stream);
            stream.close();
            stream = null;
        } catch (Exception e) {
            host.log(sm.getString("standardHost.installError", docBase), e);
            throw new IOException(e.toString());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Throwable t) {
                    ;
                }
            }
            this.overrideDocBase = null;
        }

    }


    /**
     * Return the Context for the deployed application that is associated
     * with the specified context path (if any); otherwise return
     * <code>null</code>.
     *
     * @param contextPath The context path of the requested web application
     */
    public Context findDeployedApp(String contextPath) {

        return ((Context) host.findChild(contextPath));

    }


    /**
     * Return the context paths of all deployed web applications in this
     * Container.  If there are no deployed applications, a zero-length
     * array is returned.
     */
    public String[] findDeployedApps() {

        Container children[] = host.findChildren();
        String results[] = new String[children.length];
        for (int i = 0; i < children.length; i++)
            results[i] = children[i].getName();
        return (results);

    }


    /**
     * Remove an existing web application, attached to the specified context
     * path.  If this application is successfully removed, a
     * ContainerEvent of type <code>REMOVE_EVENT</code> will be sent to all
     * registered listeners, with the removed <code>Context</code> as
     * an argument.
     *
     * @param contextPath The context path of the application to be removed
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs during
     *  removal
     */
    public void remove(String contextPath) throws IOException {

        // Validate the format and state of our arguments
        if (contextPath == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathRequired"));
        if (!contextPath.equals("") && !contextPath.startsWith("/"))
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathFormat", contextPath));

        // Locate the context and associated work directory
        Context context = findDeployedApp(contextPath);
        if (context == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathMissing", contextPath));

        // Remove this web application
        log.info(sm.getString("standardHost.removing", contextPath));
        try {
            host.removeChild(context);
            host.fireContainerEvent(REMOVE_EVENT, context);
        } catch (Exception e) {
            log.error(sm.getString("standardHost.removeError", contextPath), e);
            throw new IOException(e.toString());
        }

    }


    /**
     * Remove an existing web application, attached to the specified context
     * path.  If this application is successfully removed, a
     * ContainerEvent of type <code>REMOVE_EVENT</code> will be sent to all
     * registered listeners, with the removed <code>Context</code> as
     * an argument. Deletes the web application war file and/or directory
     * if they exist in the Host's appBase.
     *
     * @param contextPath The context path of the application to be removed
     * @param undeploy boolean flag to remove web application from server
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs during
     *  removal
     */
    public void remove(String contextPath, boolean undeploy)
        throws IOException {

        // Validate the format and state of our arguments
        if (contextPath == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathRequired"));
        if (!contextPath.equals("") && !contextPath.startsWith("/"))
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathFormat", contextPath));

        // Locate the context and associated work directory
        Context context = findDeployedApp(contextPath);
        if (context == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathMissing", contextPath));

        // Remove this web application
        host.log(sm.getString("standardHost.removing", contextPath));
        try {
            // Get the work directory for the Context
            File workDir = 
                (File) context.getServletContext().getAttribute
                (Globals.WORK_DIR_ATTR);
            String configFile = context.getConfigFile();
            host.removeChild(context);

            if (undeploy) {
                // Remove the web application directory and/or war file if it
                // exists in the Host's appBase directory.

                // Determine if directory/war to remove is in the host appBase
                boolean isAppBase = false;
                File appBase = new File(host.getAppBase());
                if (!appBase.isAbsolute())
                    appBase = new File(System.getProperty("catalina.base"),
                                       host.getAppBase());
                File contextFile = new File(context.getDocBase());
                File baseDir = contextFile.getParentFile();
                if ((baseDir == null) 
                    || (appBase.getCanonicalPath().equals
                        (baseDir.getCanonicalPath()))) {
                    isAppBase = true;
                }

                boolean isWAR = false;
                if (contextFile.getName().toLowerCase().endsWith(".war")) {
                    isWAR = true;
                }
                // Only remove directory and/or war if they are located in the
                // Host's appBase autoDeploy is true
                if (isAppBase && host.getAutoDeploy()) {
                    String filename = contextFile.getName();
                    if (isWAR) {
                        filename = filename.substring(0,filename.length()-4);
                    }
                    if (contextPath.length() == 0 && filename.equals("ROOT") ||
                        filename.equals(contextPath.substring(1))) {
                        if (!isWAR) {
                            long contextLastModified = 
                                contextFile.lastModified();
                            if (contextFile.isDirectory()) {
                                ExpandWar.deleteDir(contextFile);
                            }
                            if (host.isUnpackWARs()) {
                                File contextWAR = 
                                    new File(context.getDocBase() + ".war");
                                if (contextWAR.exists()) {
                                    if (contextLastModified 
                                        > contextWAR.lastModified()) {
                                        contextWAR.delete();
                                    }
                                }
                            }
                        } else {
                            contextFile.delete();
                        }
                    }
                    if (host.isDeployXML() && (configFile != null)) {
                        File docBaseXml = new File(configFile);
                        docBaseXml.delete();
                    }
                }

                // Remove the work directory for the Context
                if (workDir == null &&
                    context instanceof StandardContext &&
                    ((StandardContext)context).getWorkDir() != null) {
                    workDir = new File(((StandardContext)context).getWorkPath());
                }
                if (workDir != null && workDir.exists()) {
                    ExpandWar.deleteDir(workDir);
                }
            }

            host.fireContainerEvent(REMOVE_EVENT, context);
        } catch (Exception e) {
            host.log(sm.getString("standardHost.removeError", contextPath), e);
            throw new IOException(e.toString());
        }

    }


    /**
     * Start an existing web application, attached to the specified context
     * path.  Only starts a web application if it is not running.
     *
     * @param contextPath The context path of the application to be started
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs during
     *  startup
     */
    public void start(String contextPath) throws IOException {

        // Validate the format and state of our arguments
        if (contextPath == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathRequired"));
        if (!contextPath.equals("") && !contextPath.startsWith("/"))
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathFormat", contextPath));
        Context context = findDeployedApp(contextPath);
        if (context == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathMissing", contextPath));
        log.info("standardHost.start " + contextPath);
        try {
            ((Lifecycle) context).start();
        } catch (LifecycleException e) {
            log.info("standardHost.start " + contextPath + ": ", e);
            throw new IllegalStateException
                ("standardHost.start " + contextPath + ": " + e);
        }
    }


    /**
     * Stop an existing web application, attached to the specified context
     * path.  Only stops a web application if it is running.
     *
     * @param contextPath The context path of the application to be stopped
     *
     * @exception IllegalArgumentException if the specified context path
     *  is malformed (it must be "" or start with a slash)
     * @exception IllegalArgumentException if the specified context path does
     *  not identify a currently installed web application
     * @exception IOException if an input/output error occurs while stopping
     *  the web application
     */
    public void stop(String contextPath) throws IOException {

        // Validate the format and state of our arguments
        if (contextPath == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathRequired"));
        if (!contextPath.equals("") && !contextPath.startsWith("/"))
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathFormat", contextPath));
        Context context = findDeployedApp(contextPath);
        if (context == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathMissing", contextPath));
        log.info("standardHost.stop " + contextPath);
        try {
            ((Lifecycle) context).stop();
        } catch (LifecycleException e) {
            log.error("standardHost.stop " + contextPath + ": ", e);
            throw new IllegalStateException
                ("standardHost.stop " + contextPath + ": " + e);
        }

    }


    // ------------------------------------------------------ Delegated Methods


    /**
     * Delegate a request to add a child Context to our associated Host.
     *
     * @param child The child Context to be added
     */
    public void addChild(Container child) {

        Context context = (Context) child;
        String contextPath = context.getPath();
        if (contextPath == null)
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathRequired"));
        else if (!contextPath.equals("") && !contextPath.startsWith("/"))
            throw new IllegalArgumentException
                (sm.getString("standardHost.pathFormat", contextPath));
        if (host.findChild(contextPath) != null)
            throw new IllegalStateException
                (sm.getString("standardHost.pathUsed", contextPath));
        if (this.overrideDocBase != null)
            context.setDocBase(this.overrideDocBase);
        if (this.overrideConfigFile != null)
            context.setConfigFile(this.overrideConfigFile);
        host.fireContainerEvent(PRE_INSTALL_EVENT, context);
        host.addChild(child);
        host.fireContainerEvent(INSTALL_EVENT, context);

    }


    /**
     * Delegate a request for the parent class loader to our associated Host.
     */
    public ClassLoader getParentClassLoader() {

        return (host.getParentClassLoader());

    }


    /**
     * Returns true if context.xml config files are supported.
     *
     * @return true of context.xml config files are supported, false otherwise
     */
    public boolean isDeployXML() {
        return (host.isDeployXML());
    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Create (if necessary) and return a Digester configured to process the
     * context configuration descriptor for an application.
     */
    protected Digester createDigester() {
        if (digester == null) {
            digester = new CatalinaDigester();
            digester.setValidating(false);
            contextRuleSet = new ContextRuleSet("");
            digester.addRuleSet(contextRuleSet);
            namingRuleSet = new NamingRuleSet("Context/");
            digester.addRuleSet(namingRuleSet);
        }
        return (digester);

    }


}
