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


package org.apache.catalina.startup;


import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import org.apache.catalina.loader.StandardClassLoader;
import org.apache.tomcat.util.compat.JdkCompat;


/**
 * <p>Utility class for building class loaders for Catalina.  The factory
 * method requires the following parameters in order to build a new class
 * loader (with suitable defaults in all cases):</p>
 * <ul>
 * <li>A set of directories containing unpacked classes (and resources)
 *     that should be included in the class loader's
 *     repositories.</li>
 * <li>A set of directories containing classes and resources in JAR files.
 *     Each readable JAR file discovered in these directories will be
 *     added to the class loader's repositories.</li>
 * <li><code>ClassLoader</code> instance that should become the parent of
 *     the new class loader.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6.2.1 $ $Date: 2004/08/24 17:53:52 $
 */

public final class ClassLoaderFactory {


    // ------------------------------------------------------- Static Variables


    /**
     * Debugging detail level for processing the startup.
     */
    private static int debug = 0;


    /**
     * JDK compatibility support
     */
    private static final JdkCompat jdkCompat = JdkCompat.getJdkCompat();
    
    
    // ------------------------------------------------------ Static Properties


    /**
     * Return the debugging detail level.
     */
    public static int getDebug() {

        return (debug);

    }


    /**
     * Set the debugging detail level.
     *
     * @param newDebug The new debugging detail level
     */
    public static void setDebug(int newDebug) {

        debug = newDebug;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param unpacked Array of pathnames to unpacked directories that should
     *  be added to the repositories of the class loader, or <code>null</code> 
     * for no unpacked directories to be considered
     * @param packed Array of pathnames to directories containing JAR files
     *  that should be added to the repositories of the class loader, 
     * or <code>null</code> for no directories of JAR files to be considered
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(File unpacked[],
                                                File packed[],
                                                ClassLoader parent)
        throws Exception {
        return createClassLoader(unpacked, packed, null, parent);
    }


    /**
     * Create and return a new class loader, based on the configuration
     * defaults and the specified directory paths:
     *
     * @param unpacked Array of pathnames to unpacked directories that should
     *  be added to the repositories of the class loader, or <code>null</code> 
     * for no unpacked directories to be considered
     * @param packed Array of pathnames to directories containing JAR files
     *  that should be added to the repositories of the class loader, 
     * or <code>null</code> for no directories of JAR files to be considered
     * @param urls Array of URLs to remote repositories, designing either JAR 
     *  resources or uncompressed directories that should be added to 
     *  the repositories of the class loader, or <code>null</code> for no 
     *  directories of JAR files to be considered
     * @param parent Parent class loader for the new class loader, or
     *  <code>null</code> for the system class loader.
     *
     * @exception Exception if an error occurs constructing the class loader
     */
    public static ClassLoader createClassLoader(File unpacked[],
                                                File packed[],
                                                URL urls[],
                                                ClassLoader parent)
        throws Exception {

        if (debug >= 1)
            log("Creating new class loader");

        // Construct the "class path" for this class loader
        ArrayList list = new ArrayList();

        // Add unpacked directories
        if (unpacked != null) {
            for (int i = 0; i < unpacked.length; i++)  {
                File file = unpacked[i];
                if (!file.exists() || !file.canRead())
                    continue;
                if (debug >= 1)
                    log("  Including directory or JAR " 
                        + file.getAbsolutePath());
                URL url = new URL("file", null,
                                  file.getCanonicalPath() + File.separator);
                list.add(url.toString());
            }
        }

        // Add packed directory JAR files
        if (packed != null) {
            for (int i = 0; i < packed.length; i++) {
                File directory = packed[i];
                if (!directory.isDirectory() || !directory.exists() ||
                    !directory.canRead())
                    continue;
                String filenames[] = directory.list();
                for (int j = 0; j < filenames.length; j++) {
                    String filename = filenames[j].toLowerCase();
                    if (!filename.endsWith(".jar"))
                        continue;
                    File file = new File(directory, filenames[j]);
                    if (debug >= 1)
                        log("  Including jar file " + file.getAbsolutePath());
                    URL url = new URL("file", null,
                                      file.getCanonicalPath());
                    
                    if (ClassLoaderFactory.jdkCompat.isJava15()) {
                        if (url.toString().endsWith("xml-apis.jar") ||
                            url.toString().endsWith("xercesImpl.jar")) {
                            // Do not load xml-apis.jar & xercesImpl.jar
                            // if JDK 5.0 is used
                        } else {
                            list.add(url.toString());
                        }
                    } else {
                        list.add(url.toString());
                    }    
                }
            }
        }

        // Add URLs
        if (urls != null) {
            for (int i = 0; i < urls.length; i++) {
                list.add(urls[i].toString());
            }
        }

        // Construct the class loader itself
        String array[] = (String[]) list.toArray(new String[list.size()]);
        StandardClassLoader classLoader = null;
        if (parent == null)
            classLoader = new StandardClassLoader(array);
        else
            classLoader = new StandardClassLoader(array, parent);
        classLoader.setDelegate(true);
        return (classLoader);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Log a message for this class.
     *
     * @param message Message to be logged
     */
    private static void log(String message) {

        System.out.print("ClassLoaderFactory:  ");
        System.out.println(message);

    }


    /**
     * Log a message and exception for this class.
     *
     * @param message Message to be logged
     * @param exception Exception to be logged
     */
    private static void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }




}
