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
package org.apache.catalina.core;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.management.ObjectName;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletSecurityElement;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.apache.catalina.AccessLog;
import org.apache.catalina.Authenticator;
import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.ApplicationListener;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.CharsetMapper;
import org.apache.juli.logging.Log;
import org.apache.tomcat.JarScanner;
import org.apache.tomcat.util.http.mapper.Mapper;

/**
 * Minimal implementation for use in unit tests.
 */
public class TesterContext implements Context {

    @Override
    public Log getLogger() {
        return null;
    }

    @Override
    public ObjectName getObjectName() {
        return null;
    }

    @Override
    public Pipeline getPipeline() {
        return null;
    }

    @Override
    public Cluster getCluster() {
        return null;
    }

    @Override
    public void setCluster(Cluster cluster) {
        // NO-OP
    }

    @Override
    public int getBackgroundProcessorDelay() {
        return 0;
    }

    @Override
    public void setBackgroundProcessorDelay(int delay) {
        // NO-OP
    }

    @Override
    public String getName() {
        return "/test";
    }

    @Override
    public void setName(String name) {
        // NO-OP
    }

    @Override
    public Container getParent() {
        return null;
    }

    @Override
    public void setParent(Container container) {
        // NO-OP
    }

    @Override
    public ClassLoader getParentClassLoader() {
        return null;
    }

    @Override
    public void setParentClassLoader(ClassLoader parent) {
        // NO-OP
    }

    @Override
    public Realm getRealm() {
        return null;
    }

    @Override
    public void setRealm(Realm realm) {
        // NO-OP
    }

    @Override
    public void backgroundProcess() {
        // NO-OP
    }

    @Override
    public void addChild(Container child) {
        // NO-OP
    }

    @Override
    public void addContainerListener(ContainerListener listener) {
        // NO-OP
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // NO-OP
    }

    @Override
    public Container findChild(String name) {
        return null;
    }

    @Override
    public Container[] findChildren() {
        return null;
    }

    @Override
    public ContainerListener[] findContainerListeners() {
        return null;
    }

    @Override
    public void removeChild(Container child) {
        // NO-OP
    }

    @Override
    public void removeContainerListener(ContainerListener listener) {
        // NO-OP
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // NO-OP
    }

    @Override
    public void fireContainerEvent(String type, Object data) {
        // NO-OP
    }

    @Override
    public void logAccess(Request request, Response response, long time,
            boolean useDefault) {
        // NO-OP
    }

    @Override
    public AccessLog getAccessLog() {
        return null;
    }

    @Override
    public int getStartStopThreads() {
        return 0;
    }

    @Override
    public void setStartStopThreads(int startStopThreads) {
        // NO-OP
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        // NO-OP
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return null;
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        // NO-OP
    }

    @Override
    public void init() throws LifecycleException {
        // NO-OP
    }

    @Override
    public void start() throws LifecycleException {
        // NO-OP
    }

    @Override
    public void stop() throws LifecycleException {
        // NO-OP
    }

    @Override
    public void destroy() throws LifecycleException {
        // NO-OP
    }

    @Override
    public LifecycleState getState() {
        return null;
    }

    @Override
    public String getStateName() {
        return null;
    }

    @Override
    public boolean getAllowCasualMultipartParsing() {
        return false;
    }

    @Override
    public void setAllowCasualMultipartParsing(
            boolean allowCasualMultipartParsing) {
        // NO-OP
    }

    @Override
    public Object[] getApplicationEventListeners() {
        return null;
    }

    @Override
    public void setApplicationEventListeners(Object[] listeners) {
        // NO-OP
    }

    @Override
    public Object[] getApplicationLifecycleListeners() {
        return null;
    }

    @Override
    public void setApplicationLifecycleListeners(Object[] listeners) {
        // NO-OP
    }

    @Override
    public String getCharset(Locale locale) {
        return null;
    }

    @Override
    public URL getConfigFile() {
        return null;
    }

    @Override
    public void setConfigFile(URL configFile) {
        // NO-OP
    }

    @Override
    public boolean getConfigured() {
        return false;
    }

    @Override
    public void setConfigured(boolean configured) {
        // NO-OP
    }

    @Override
    public boolean getCookies() {
        return false;
    }

    @Override
    public void setCookies(boolean cookies) {
        // NO-OP
    }

    @Override
    public String getSessionCookieName() {
        return null;
    }

    @Override
    public void setSessionCookieName(String sessionCookieName) {
        // NO-OP
    }

    @Override
    public boolean getUseHttpOnly() {
        return false;
    }

    @Override
    public void setUseHttpOnly(boolean useHttpOnly) {
        // NO-OP
    }

    @Override
    public String getSessionCookieDomain() {
        return null;
    }

    @Override
    public void setSessionCookieDomain(String sessionCookieDomain) {
        // NO-OP
    }

    @Override
    public String getSessionCookiePath() {
        return null;
    }

    @Override
    public void setSessionCookiePath(String sessionCookiePath) {
        // NO-OP
    }

    @Override
    public boolean getSessionCookiePathUsesTrailingSlash() {
        return false;
    }

    @Override
    public void setSessionCookiePathUsesTrailingSlash(
            boolean sessionCookiePathUsesTrailingSlash) {
        // NO-OP
    }

    @Override
    public boolean getCrossContext() {
        return false;
    }

    @Override
    public String getAltDDName() {
        return null;
    }

    @Override
    public void setAltDDName(String altDDName) {
        // NO-OP
    }

    @Override
    public void setCrossContext(boolean crossContext) {
        // NO-OP
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setDisplayName(String displayName) {
        // NO-OP
    }

    @Override
    public boolean getDistributable() {
        return false;
    }

    @Override
    public void setDistributable(boolean distributable) {
        // NO-OP
    }

    @Override
    public String getDocBase() {
        return null;
    }

    @Override
    public void setDocBase(String docBase) {
        // NO-OP
    }

    @Override
    public String getEncodedPath() {
        return null;
    }

    @Override
    public boolean getIgnoreAnnotations() {
        return false;
    }

    @Override
    public void setIgnoreAnnotations(boolean ignoreAnnotations) {
        // NO-OP
    }

    @Override
    public LoginConfig getLoginConfig() {
        return null;
    }

    @Override
    public void setLoginConfig(LoginConfig config) {
        // NO-OP
    }

    @Override
    public NamingResources getNamingResources() {
        return null;
    }

    @Override
    public void setNamingResources(NamingResources namingResources) {
        // NO-OP
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public void setPath(String path) {
        // NO-OP
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public void setPublicId(String publicId) {
        // NO-OP
    }

    @Override
    public boolean getReloadable() {
        return false;
    }

    @Override
    public void setReloadable(boolean reloadable) {
        // NO-OP
    }

    @Override
    public boolean getOverride() {
        return false;
    }

    @Override
    public void setOverride(boolean override) {
        // NO-OP
    }

    @Override
    public boolean getPrivileged() {
        return false;
    }

    @Override
    public void setPrivileged(boolean privileged) {
        // NO-OP
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public int getSessionTimeout() {
        return 0;
    }

    @Override
    public void setSessionTimeout(int timeout) {
        // NO-OP
    }

    @Override
    public boolean getSwallowAbortedUploads() {
        return false;
    }

    @Override
    public void setSwallowAbortedUploads(boolean swallowAbortedUploads) {
        // NO-OP
    }

    @Override
    public boolean getSwallowOutput() {
        return false;
    }

    @Override
    public void setSwallowOutput(boolean swallowOutput) {
        // NO-OP
    }

    @Override
    public String getWrapperClass() {
        return null;
    }

    @Override
    public void setWrapperClass(String wrapperClass) {
        // NO-OP
    }

    @Override
    public boolean getXmlNamespaceAware() {
        return false;
    }

    @Override
    public boolean getXmlValidation() {
        return false;
    }

    @Override
    public void setXmlValidation(boolean xmlValidation) {
        // NO-OP
    }

    @Override
    public void setXmlNamespaceAware(boolean xmlNamespaceAware) {
        // NO-OP
    }

    @Override
    public void setTldValidation(boolean tldValidation) {
        // NO-OP
    }

    @Override
    public boolean getTldValidation() {
        return false;
    }

    @Override
    public boolean getTldNamespaceAware() {
        return false;
    }

    @Override
    public void setTldNamespaceAware(boolean tldNamespaceAware) {
        // NO-OP
    }

    @Override
    public JarScanner getJarScanner() {
        return null;
    }

    @Override
    public void setJarScanner(JarScanner jarScanner) {
        // NO-OP
    }

    @Override
    public Authenticator getAuthenticator() {
        return null;
    }

    @Override
    public void setLogEffectiveWebXml(boolean logEffectiveWebXml) {
        // NO-OP
    }

    @Override
    public boolean getLogEffectiveWebXml() {
        return false;
    }

    @Override
    public void addApplicationListener(ApplicationListener listener) {
        // NO-OP
    }

    @Override
    public void addApplicationListener(String listener) {
        // NO-OP
    }

    @Override
    public void addApplicationParameter(ApplicationParameter parameter) {
        // NO-OP
    }

    @Override
    public void addConstraint(SecurityConstraint constraint) {
        // NO-OP
    }

    @Override
    public void addErrorPage(ErrorPage errorPage) {
        // NO-OP
    }

    @Override
    public void addFilterDef(FilterDef filterDef) {
        // NO-OP
    }

    @Override
    public void addFilterMap(FilterMap filterMap) {
        // NO-OP
    }

    @Override
    public void addFilterMapBefore(FilterMap filterMap) {
        // NO-OP
    }

    @Override
    public void addInstanceListener(String listener) {
        // NO-OP
    }

    @Override
    public void addLocaleEncodingMappingParameter(String locale, String encoding) {
        // NO-OP
    }

    @Override
    public void addMimeMapping(String extension, String mimeType) {
        // NO-OP
    }

    @Override
    public void addParameter(String name, String value) {
        // NO-OP
    }

    @Override
    public void addRoleMapping(String role, String link) {
        // NO-OP
    }

    @Override
    public void addSecurityRole(String role) {
        // NO-OP
    }

    @Override
    public void addServletMapping(String pattern, String name) {
        // NO-OP
    }

    @Override
    public void addServletMapping(String pattern, String name,
            boolean jspWildcard) {
        // NO-OP
    }

    @Override
    public void addWatchedResource(String name) {
        // NO-OP
    }

    @Override
    public void addWelcomeFile(String name) {
        // NO-OP
    }

    @Override
    public void addWrapperLifecycle(String listener) {
        // NO-OP
    }

    @Override
    public void addWrapperListener(String listener) {
        // NO-OP
    }

    @Override
    public Wrapper createWrapper() {
        return null;
    }

    @Override
    public String[] findApplicationListeners() {
        return null;
    }

    @Override
    public ApplicationParameter[] findApplicationParameters() {
        return null;
    }

    @Override
    public SecurityConstraint[] findConstraints() {
        return null;
    }

    @Override
    public ErrorPage findErrorPage(int errorCode) {
        return null;
    }

    @Override
    public ErrorPage findErrorPage(String exceptionType) {
        return null;
    }

    @Override
    public ErrorPage[] findErrorPages() {
        return null;
    }

    @Override
    public FilterDef findFilterDef(String filterName) {
        return null;
    }

    @Override
    public FilterDef[] findFilterDefs() {
        return null;
    }

    @Override
    public FilterMap[] findFilterMaps() {
        return null;
    }

    @Override
    public String[] findInstanceListeners() {
        return null;
    }

    @Override
    public String findMimeMapping(String extension) {
        return null;
    }

    @Override
    public String[] findMimeMappings() {
        return null;
    }

    @Override
    public String findParameter(String name) {
        return null;
    }

    @Override
    public String[] findParameters() {
        return null;
    }

    @Override
    public String findRoleMapping(String role) {
        return null;
    }

    @Override
    public boolean findSecurityRole(String role) {
        return false;
    }

    @Override
    public String[] findSecurityRoles() {
        return null;
    }

    @Override
    public String findServletMapping(String pattern) {
        return null;
    }

    @Override
    public String[] findServletMappings() {
        return null;
    }

    @Override
    public String findStatusPage(int status) {
        return null;
    }

    @Override
    public int[] findStatusPages() {
        return null;
    }

    @Override
    public String[] findWatchedResources() {
        return null;
    }

    @Override
    public boolean findWelcomeFile(String name) {
        return false;
    }

    @Override
    public String[] findWelcomeFiles() {
        return null;
    }

    @Override
    public String[] findWrapperLifecycles() {
        return null;
    }

    @Override
    public String[] findWrapperListeners() {
        return null;
    }

    @Override
    public boolean fireRequestInitEvent(ServletRequest request) {
        return false;
    }

    @Override
    public boolean fireRequestDestroyEvent(ServletRequest request) {
        return false;
    }

    @Override
    public void reload() {
        // NO-OP
    }

    @Override
    public void removeApplicationListener(String listener) {
        // NO-OP
    }

    @Override
    public void removeApplicationParameter(String name) {
        // NO-OP
    }

    @Override
    public void removeConstraint(SecurityConstraint constraint) {
        // NO-OP
    }

    @Override
    public void removeErrorPage(ErrorPage errorPage) {
        // NO-OP
    }

    @Override
    public void removeFilterDef(FilterDef filterDef) {
        // NO-OP
    }

    @Override
    public void removeFilterMap(FilterMap filterMap) {
        // NO-OP
    }

    @Override
    public void removeInstanceListener(String listener) {
        // NO-OP
    }

    @Override
    public void removeMimeMapping(String extension) {
        // NO-OP
    }

    @Override
    public void removeParameter(String name) {
        // NO-OP
    }

    @Override
    public void removeRoleMapping(String role) {
        // NO-OP
    }

    @Override
    public void removeSecurityRole(String role) {
        // NO-OP
    }

    @Override
    public void removeServletMapping(String pattern) {
        // NO-OP
    }

    @Override
    public void removeWatchedResource(String name) {
        // NO-OP
    }

    @Override
    public void removeWelcomeFile(String name) {
        // NO-OP
    }

    @Override
    public void removeWrapperLifecycle(String listener) {
        // NO-OP
    }

    @Override
    public void removeWrapperListener(String listener) {
        // NO-OP
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public void setEffectiveMajorVersion(int major) {
        // NO-OP
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public void setEffectiveMinorVersion(int minor) {
        // NO-OP
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        return null;
    }

    @Override
    public void addServletContainerInitializer(ServletContainerInitializer sci,
            Set<Class<?>> classes) {
        // NO-OP
    }

    @Override
    public boolean getPaused() {
        return false;
    }

    @Override
    public boolean isServlet22() {
        return false;
    }

    @Override
    public void setResourceOnlyServlets(String resourceOnlyServlets) {
        // NO-OP
    }

    @Override
    public String getResourceOnlyServlets() {
        return null;
    }

    @Override
    public boolean isResourceOnlyServlet(String servletName) {
        return false;
    }

    @Override
    public String getBaseName() {
        return null;
    }

    @Override
    public void setWebappVersion(String webappVersion) {
        // NO-OP
    }

    @Override
    public String getWebappVersion() {
        return null;
    }

    @Override
    public void setFireRequestListenersOnForwards(boolean enable) {
        // NO-OP
    }

    @Override
    public boolean getFireRequestListenersOnForwards() {
        return false;
    }

    @Override
    public void setPreemptiveAuthentication(boolean enable) {
        // NO-OP
    }

    @Override
    public boolean getPreemptiveAuthentication() {
        return false;
    }

    @Override
    public void setSendRedirectBody(boolean enable) {
        // NO-OP
    }

    @Override
    public boolean getSendRedirectBody() {
        return false;
    }

    @Override
    public Loader getLoader() {
        return null;
    }

    @Override
    public void setLoader(Loader loader) {
        // NO-OP
    }

    @Override
    public Manager getManager() {
        return null;
    }

    @Override
    public void setManager(Manager manager) {
        // NO-OP
    }

    @Override
    public void addPostConstructMethod(String clazz, String method) {
        // NO-OP
    }

    @Override
    public void addPreDestroyMethod(String clazz, String method) {
        // NO-OP
    }

    @Override
    public void removePostConstructMethod(String clazz) {
        // NO-OP
    }

    @Override
    public void removePreDestroyMethod(String clazz) {
        // NO-OP
    }

    @Override
    public String findPostConstructMethod(String clazz) {
        return null;
    }

    @Override
    public String findPreDestroyMethod(String clazz) {
        return null;
    }

    @Override
    public Map<String,String> findPostConstructMethods() {
        return null;
    }

    @Override
    public Map<String,String> findPreDestroyMethods() {
        return null;
    }

    @Override
    public String getInfo() {
        return null;
    }

    @Override
    @Deprecated
    public Object getMappingObject() {
        return null;
    }

    @Override
    public DirContext getResources() {
        return null;
    }

    @Override
    public void setResources(DirContext resources) {
        //NO-OP
    }

    @Override
    @Deprecated
    public void invoke(Request request, Response response) throws IOException,
            ServletException {
        // NO-OP
    }

    @Override
    @Deprecated
    public boolean getAvailable() {
        return false;
    }

    @Override
    @Deprecated
    public CharsetMapper getCharsetMapper() {
        return null;
    }

    @Override
    @Deprecated
    public void setCharsetMapper(CharsetMapper mapper) {
        // NO-OP
    }

    @Override
    public Mapper getMapper() {
        return null;
    }

    @Override
    public void addResourceJarUrl(URL url) {
    }

    @Override
    public Set<String> addServletSecurity(
            ApplicationServletRegistration registration,
            ServletSecurityElement servletSecurityElement) {
        return null;
    }
}