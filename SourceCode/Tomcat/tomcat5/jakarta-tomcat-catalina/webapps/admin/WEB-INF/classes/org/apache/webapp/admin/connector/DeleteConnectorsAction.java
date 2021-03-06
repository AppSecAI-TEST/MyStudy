/*
 * Copyright 2002,2004 The Apache Software Foundation.
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


package org.apache.webapp.admin.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ObjectInstance;
import javax.management.modelmbean.ModelMBean;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.TomcatTreeBuilder;
import org.apache.webapp.admin.TreeControl;
import org.apache.webapp.admin.TreeControlNode;


/**
 * The <code>Action</code> that completes <em>Delete Connectors</em>
 * transactions.
 *
 * @author Manveen Kaur
 * @version $Revision: 1.4 $ $Date: 2004/02/27 14:59:02 $
 */

public class DeleteConnectorsAction extends Action {


    /**
     * Signature for the <code>removeConnector</code> operation.
     */
    private String removeConnectorTypes[] =
    { "java.lang.String",      // Object name
    };


    /**
     * The MBeanServer we will be interacting with.
     */
    private MBeanServer mBServer = null;
    

    /**
     * The MessageResources we will be retrieving messages from.
     */
    private MessageResources resources = null;

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
    public ActionForward perform(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws IOException, ServletException {
        
        
        // Look up the components we will be using as needed
        HttpSession session = request.getSession();
        Locale locale = (Locale) session.getAttribute(Action.LOCALE_KEY);
        if (resources == null) {
            resources = getServlet().getResources();
        }

        // Acquire a reference to the MBeanServer containing our MBeans
        try {
            mBServer = ((ApplicationServlet) getServlet()).getServer();
        } catch (Throwable t) {
            throw new ServletException
            ("Cannot acquire MBeanServer reference", t);
        }
        
        // Delete the specified Connectors
        String connectors[]  = ((ConnectorsForm) form).getConnectors();
        String values[] = new String[1];
        String operation = "removeConnector";
        try {

            // Look up our tree control data structure
            TreeControl control = (TreeControl)
                session.getAttribute("treeControlTest");
                
            // Look up our MBeanFactory MBean
            ObjectName fname = null;
            String domain = null;
            TreeControlNode node = null;

            // Remove the specified connectors
            for (int i = 0; i < connectors.length; i++) {
                values[0] = connectors[i];
                if (control != null) {
                    control.selectNode(null);
                    node = control.findNode(connectors[i]);
                    domain = node.getDomain();
                    // Look up our MBeanFactory MBean
                    fname = TomcatTreeBuilder.getMBeanFactory();
                    mBServer.invoke(fname, operation,
                                values, removeConnectorTypes);
                    if (node != null) {
                        node.remove();
                    } else {
                        getServlet().log("Missing TreeControlNode for " +
                                         connectors[i]);
                    }
                } else {
                    getServlet().log("Missing TreeControl attribute");
                }
            }

        } catch (Exception e) {

            getServlet().log
                (resources.getMessage(locale, "users.error.invoke",
                                      operation), e);
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.invoke",
                                      operation));
            return (null);

        }

        // Report successful completion of this transaction
        return (mapping.findForward("Save Successful"));

    }
    
}
