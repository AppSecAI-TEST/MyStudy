/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

package org.apache.webapp.admin.realm;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import java.util.List;
import java.util.Enumeration;

import org.apache.webapp.admin.ApplicationServlet;
import org.apache.webapp.admin.LabelValueBean;

/**
 * Form bean for the memory realm page.
 *
 * @author Manveen Kaur
 * @version $Revision: 1.3 $ $Date: 2004/02/27 14:59:03 $
 */

public final class MemoryRealmForm extends RealmForm {
    
    // ----------------------------------------------------- Instance Variables
        
    /**
     * The text for the path Name.
     */
    private String pathName = null;
       
    // ------------------------------------------------------------- Properties
        
    /**
     * Return the path Name.
     */
    public String getPathName() {
        
        return this.pathName;
        
    }
    
    /**
     * Set the path Name.
     */
    public void setPathName(String pathName) {
        
        this.pathName = pathName;
        
    }
        
    // --------------------------------------------------------- Public Methods
    
    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
   
        super.reset(mapping, request);
        this.pathName = null;
        
    }
    
   /**
     * Render this object as a String.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer("UserDatabaseRealmForm[adminAction=");
        sb.append(getAdminAction());
        sb.append(",debugLvl=");
        sb.append(getDebugLvl());
        sb.append(",pathname=");
        sb.append(pathName);
        sb.append("',objectName='");
        sb.append(getObjectName());
        sb.append("',realmType=");
        sb.append(getRealmType());
        sb.append("]");
        return (sb.toString());

    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    
    public ActionErrors validate(ActionMapping mapping,
    HttpServletRequest request) {
        
        ActionErrors errors = new ActionErrors();

        String submit = request.getParameter("submit");
        
        // front end validation when save is clicked.
        //if (submit != null) {
            if ((pathName == null) || (pathName.length()<1)) {
                errors.add("pathName",
                new ActionError("error.pathName.required"));
            }
        //}        
        return errors;
    }
}
