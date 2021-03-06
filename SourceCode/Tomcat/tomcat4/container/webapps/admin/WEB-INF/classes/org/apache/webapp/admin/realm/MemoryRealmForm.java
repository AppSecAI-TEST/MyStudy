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

package org.apache.webapp.admin.realm;

import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;

/**
 * Form bean for the memory realm page.
 *
 * @author Manveen Kaur
 * @version $Revision: 466595 $ $Date: 2006-10-21 23:24:41 +0100 (Sat, 21 Oct 2006) $
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
        if (submit != null) {
            if ((pathName == null) || (pathName.length()<1)) {
                errors.add("pathName",
                new ActionMessage("error.pathName.required"));
            }
        }        
        return errors;
    }
}
