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

package org.apache.catalina.cluster.deploy;

import org.apache.catalina.cluster.ClusterMessage;
import org.apache.catalina.cluster.Member;
import java.io.Serializable;
public class UndeployMessage implements ClusterMessage,Serializable {
    private Member address;
    private long timestamp;
    private String uniqueId;
    private String contextPath;
    private boolean undeploy;
    
    public UndeployMessage() {} //for serialization
    public UndeployMessage(Member address,
                           long timestamp,
                           String uniqueId,
                           String contextPath,
                           boolean undeploy) {
        this.address  = address;
        this.timestamp= timestamp;
        this.undeploy = undeploy;
        this.uniqueId = uniqueId;
        this.undeploy = undeploy;
        this.contextPath = contextPath;
    }

    public Member getAddress() {
        return address;
    }

    public void setAddress(Member address) {
        this.address = address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public boolean getUndeploy() {
        return undeploy;
    }

    public void setUndeploy(boolean undeploy) {
        this.undeploy = undeploy;
    }
}
