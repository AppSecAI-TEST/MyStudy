/*
 * Copyright 2001-2002,2004 The Apache Software Foundation.
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


package org.apache.webapp.admin.defaultcontext;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.Set;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.QueryExp;
import javax.management.Query;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.JMException;
import org.apache.struts.util.MessageResources;

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.TomcatTreeBuilder;

/**
 * The <code>Action</code> that sets up <em>Delete DefaultContexts</em> transactions.
 *
 * @author Amy Roh
 * @version $Revision: 1.3 $ $Date: 2004/02/27 14:59:02 $
 */

public class DeleteDefaultContextAction extends Action {
    

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
        

        // Acquire the resources that we need
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
        
        // object name that forms the basis of the search pattern
        // to get list of all available default contexts
        String patternObject = null;
        
        // Set up a form bean containing the currently selected
        // objects to be deleted
        DefaultContextsForm defaultContextsForm = new DefaultContextsForm();
        String select = request.getParameter("select");
        if (select != null) {
            String defaultContexts[] = new String[1];
            defaultContexts[0] = select;
            defaultContextsForm.setDefaultContexts(defaultContexts);
            patternObject = select;
        }        
        request.setAttribute("defaultContextsForm", defaultContextsForm);
                
        // get the parent host object name
        String parent = request.getParameter("parent");
        if (parent != null) {                
            patternObject = parent;
        } 
        
        // FIXME delete DefaultContext not supported yet
        // Accumulate a list of all available contexts
        ArrayList list = new ArrayList();
        try {
            ObjectName poname = new ObjectName(patternObject);
            String host = poname.getKeyProperty("host");
            String pattern = null;
            if (host != null) {
                pattern = poname.getDomain()+ 
                          TomcatTreeBuilder.DEFAULTCONTEXT_TYPE +
                          TomcatTreeBuilder.WILDCARD +  
                          ",host=" + poname.getKeyProperty("host"); 
            } else {
                pattern = poname.getDomain()+ 
                          TomcatTreeBuilder.DEFAULTCONTEXT_TYPE; 
            }   
            // get all available contexts only for this host
            Iterator items =
                mBServer.queryNames(new ObjectName(pattern), null).iterator();
            while (items.hasNext()) {
                list.add(items.next().toString());
            }
        } catch (Exception e) {
            getServlet().log
                (resources.getMessage(locale, "users.error.select"));
            response.sendError
                (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 resources.getMessage(locale, "users.error.select"));
            return (null);
        }
        Collections.sort(list);
        request.setAttribute("contextsList", list);
        
        // Forward to the list display page
        return (mapping.findForward("DefaultContexts"));

    }

}