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

package org.apache.catalina.cluster;

/**
 * The common interface used by all cluster manager.
 * This is so that we can have a more pluggable way
 * of swapping session managers for different algorithms.
 *
 * @author Filip Hanik

 */

import org.apache.catalina.Manager;


public interface ClusterManager extends Manager {

   /**
    * A message was received from another node, this
    * is the callback method to implement if you are interested in
    * receiving replication messages.
    * @param msg - the message received.
    */
   public void messageDataReceived(SessionMessage msg);

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
   public SessionMessage requestCompleted(String sessionId);

   /**
    * When the manager expires session not tied to a request.
    * The cluster will periodically ask for a list of sessions
    * that should expire and that should be sent across the wire.
    * @return String[] The invalidated sessions
    */
   public String[] getInvalidatedSessions();
   
   /**
    * Return the name of the manager, typically the context name such as /replicator
    * @return String
    */
   public String getName();
   
   public void setName(String name);
   
   public void setExpireSessionsOnShutdown(boolean expireSessionsOnShutdown);
   
   public void setUseDirtyFlag(boolean useDirtyFlag);
   
   public void setCluster(CatalinaCluster cluster);

}
