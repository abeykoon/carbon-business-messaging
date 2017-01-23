/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.andes.admin.internal;


import java.util.Calendar;

public class Queue {

    private String queueName;

    private String queueOwningNode;

    private long pendingMessageCount;

    private long totalReceivedMessageCount;

    private long totalAckedMessageCount;

    private long queueDepth;

    private Calendar createdTime;

    private Calendar updatedTime;

    private String createdFrom;

    public Queue() {
    }

    public Queue(org.wso2.carbon.andes.core.types.Queue queue){
        this.queueName = queue.getQueueName();
        this.queueOwningNode = queue.getQueueOwningNode();
        this.pendingMessageCount = queue.getPendingMessageCount();
        this.totalReceivedMessageCount = queue.getTotalReceivedMessageCount();
        this.totalAckedMessageCount = queue.getTotalAckedMessageCount();
        this.queueDepth = queue.getQueueDepth();
        this.createdTime = queue.getCreatedTime();
        this.updatedTime = queue.getUpdatedTime();
        this.createdFrom = queue.getCreatedFrom();
    }

    public Queue(String queueName) {
        this.queueName = queueName;
    }
     
    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getQueueOwningNode() {
        return queueOwningNode;
    }

    public void setQueueOwningNode(String queueOwningNode) {
        this.queueOwningNode = queueOwningNode;
    }

    public long getTotalReceivedMessageCount() {
        return totalReceivedMessageCount;
    }

    public void setTotalReceivedMessageCount(long totalReceivedMessageCount) {
        this.totalReceivedMessageCount = totalReceivedMessageCount;
    }

    public long getTotalAckedMessageCount() {
        return totalAckedMessageCount;
    }

    public void setTotalAckedMessageCount(long totalAckedMessageCount) {
        this.totalAckedMessageCount = totalAckedMessageCount;
    }

    public long getQueueDepth() {
        return queueDepth;
    }

    public void setQueueDepth(long queueDepth) {
        this.queueDepth = queueDepth;
    }

    public long getPendingMessageCount() {
        return pendingMessageCount;
    }

    public void setPendingMessageCount(long pendingMessageCount) {
        this.pendingMessageCount = pendingMessageCount;
    }

    public Calendar getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Calendar createdTime) {
        this.createdTime = createdTime;
    }

    public Calendar getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Calendar updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getCreatedFrom() {
        return createdFrom;
    }

    public void setCreatedFrom(String createdFrom) {
        this.createdFrom = createdFrom;
    }
}
