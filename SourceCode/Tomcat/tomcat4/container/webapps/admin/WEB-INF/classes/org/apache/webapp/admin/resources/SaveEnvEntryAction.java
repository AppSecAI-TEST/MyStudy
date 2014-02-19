/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.webapp.admin.resources;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.struts.util.MessageResources;
import org.apache.webapp.admin.ApplicationServlet;


/**
 * <p>Implementation of <strong>Action</strong> that saves a new or
 * updated Env Entry.</p>
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
 * @since 4.1
 */

public final class SaveEnvEntryAction extends Action {


    // ----------------------------------------------------- Instance Variables

    /**
     * The MessageResources we will be retrieving messages from.
     */
    private MessageResources resources = null;


    /**
     * The MBeanServer we will be interacting with.
     */
    private MBeanServer mserver = null;


    // --------------------------------------------------------- Public Methods


    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     *
     * @param mapping The ActionMapping used to select this instance
     * @param actionForm The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException, ServletException {

        // Look up the components we will be using as needed
        if (mserver == null) {
            mserver = ((ApplicationServlet) getServlet()).getServer();
        }
        if (resources == null) {
            resources = getResources(request);
        }
        HttpSession session = request.getSession();
        Locale locale = (Locale) session.getAttribute(Globals.LOCALE_KEY);

        // Has this transaction been cancelled?
        if (isCancelled(request)) {
            return (mapping.findForward("List EnvEntries Setup"));
        }

        // Check the transaction token
        if (!isTokenValid(request)) {
            response.sendError
                (HttpServletResponse.SC_BAD_REQUEST,
                 resources.getMessage(locale, "users.error.token"));
            return (null);
        }

        // Perform any extra validation that is required
        EnvEntryForm envEntryForm = (EnvEntryForm) form;
        String objectName = envEntryForm.getObjectName();

        // Perform an "Add Entry" transaction
        if (objectName == null) {

            String signature[] = new String[3];
            signature[0] = "java.lang.String";
            signature[1] = "java.lang.String";
            signature[2] = "java.lang.String";

            Object params[] = new Object[3];
            params[0] = envEntryForm.getName();
            params[1] = envEntryForm.getEntryType();
            params[2] = envEntryForm.getValue();
            
            String resourcetype = envEntryForm.getResourcetype();
            String path = envEntryForm.getPath();
            String host = envEntryForm.getHost();
            String service = envEntryForm.getService();
            
            ObjectName oname = null;

            try {

                if (resourcetype!=null) {
                    // Construct the MBean Name for the naming source
                    if (resourcetype.equals("Global")) {
                        oname = 
                            new ObjectName(ResourceUtils.NAMINGRESOURCES_TYPE +
                            ResourceUtils.GLOBAL_TYPE);
                    } else if (resourcetype.equals("Context")) {            
                        oname = 
                            new ObjectName (ResourceUtils.NAMINGRESOURCES_TYPE + 
                            ResourceUtils.CONTEXT_TYPE + ",path=" + path + 
                            ",host=" + host + ",service=" + service);
                    } else if (resourcetype.equals("DefaultContext")) {
                        if (host.length() > 0) {
                            oname = 
                                new ObjectName(ResourceUtils.NAMINGRESOURCES_TYPE +
                                ResourceUtils.HOST_DEFAULTCONTEXT_TYPE + ",host=" + 
                                host + ",service=" + service);
                        } else {
                            oname = 
                                new ObjectName(ResourceUtils.NAMINGRESOURCES_TYPE +
                                ResourceUtils.SERVICE_DEFAULTCONTEXT_TYPE + ",service=" + 
                                service);
                        }
                    }
                }
                // Create the new object and associated MBean
                objectName = (String) mserver.invoke(oname, "addEnvironment",
                                                     params, signature);
                                                                     
            } catch (Exception e) {

                getServlet().log
                    (resources.getMessage(locale, "users.error.invoke",
                                          "addEnvironment"), e);
                response.sendError
                    (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     resources.getMessage(locale, "users.error.invoke",
                                          "addEnvironment"));
                return (null);
            }

        }

        // Perform an "Update Environment Entry" transaction
        String attribute = null;
        try {
            
            // Construct an object name for this object
            ObjectName oname = new ObjectName(objectName);

            // Update the specified env entry
            attribute = "override";
            mserver.setAttribute
                (oname,
                 new Attribute(attribute, new Boolean(envEntryForm.getOverride())));
            attribute = "description";
            mserver.setAttribute
                (oname,
                 new Attribute(attribute, envEntryForm.getDescription()));
            attribute = "type";
            mserver.setAttribute
                (oname,
                 new Attribute(attribute, envEntryForm.getEntryType()));
            attribute = "value";
            mserver.setAttribute
                (oname,
                 new Attribute(attribute, envEntryForm.getValue()));

        } catch (Exception e) {

            getServlet().log
                (resources.getMessage(locale, "users.error.set.attribute",
                                      attribute), e);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.set.attribute",
                                      attribute));
            return (null);

        }
        
        // Proceed to the list entries screen
        return (mapping.findForward("EnvEntries List Setup"));

    }


}
