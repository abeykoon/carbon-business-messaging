/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.andes.core.internal.registry;

import org.wso2.andes.management.common.mbeans.QueueManagementInformation;
import org.wso2.carbon.andes.core.QueueManagerException;
import org.wso2.carbon.andes.core.internal.util.QueueManagementConstants;
import org.wso2.carbon.andes.core.types.Message;
import org.wso2.carbon.andes.core.types.Queue;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The following class contains the MBeans invoking services related to queue resources.
 */
public class QueueManagementBeans {

    public static QueueManagementBeans self;
    public static final String DIRECT_EXCHANGE = "amq.direct";
    public static final String DEFAULT_EXCHANGE = "<<default>>";


    /**
     * Gets the active queue managing instance.
     * @return A queue managing instance.
     */
    public static QueueManagementBeans getInstance() {
        if (self == null) {
            self = new QueueManagementBeans();
        }
        return self;
    }

    /**
     * Invoke service bean to creates a new queue.
     *
     * @param queueName The queue name.
     * @param userName The user name of the queue owner
     *
     * @throws QueueManagerException
     */
    public void createQueue(String queueName, String userName) throws QueueManagerException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {

            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=VirtualHost.VirtualHostManager,VirtualHost=\"carbon\"");
            String operationName = "createNewQueue";

            Object[] parameters = new Object[]{queueName, userName, true};
            String[] signature = new String[]{String.class.getName(), String.class.getName(),
                    boolean.class.getName()};

            mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);

            ObjectName bindingMBeanObjectName =
                    new ObjectName("org.wso2.andes:type=VirtualHost.Exchange,VirtualHost=\"carbon\",name=\"" +
                            DIRECT_EXCHANGE + "\",ExchangeType=direct");
            String bindingOperationName = "createNewBinding";

            Object[] bindingParams = new Object[]{queueName, queueName};
            String[] bpSignatures = new String[]{String.class.getName(), String.class.getName()};

            mBeanServer.invoke(
                    bindingMBeanObjectName,
                    bindingOperationName,
                    bindingParams,
                    bpSignatures);

        } catch (MalformedObjectNameException | InstanceNotFoundException | ReflectionException e) {
            throw new QueueManagerException("Cannot create Queue : " + queueName, e);
        } catch (MBeanException e) {
            throw new QueueManagerException(e.getCause().getMessage(), e);
        }
    }

    /**
     *
     * @return
     * @throws QueueManagerException
     */
    public List<Queue> getAllQueueInformation() throws QueueManagerException {
        List<Queue> queueList = new ArrayList<>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {

            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");
            Object result = mBeanServer.getAttribute(objectName,
                    QueueManagementConstants.QUEUE_INFORMATION_MBEAN_ATTRIBUTE);
            if (result != null) {
                CompositeData[] queueInformationList = (CompositeData[]) result;
                for (CompositeData queueData : queueInformationList) {
                    Queue queue = new Queue();
                    queue.setQueueName((String) queueData.get(QueueManagementInformation.QUEUE_NAME));
                    queue.setQueueOwningNode((String) queueData.get(QueueManagementInformation.QUEUE_MASTER_NODE));
                    queue.setPendingMessageCount((int) queueData.get(QueueManagementInformation
                            .REMAINING_MESSAGE_COUNT));
                    queue.setTotalReceivedMessageCount((long)queueData.get(QueueManagementInformation
                            .TOTAL_RECEIVED_MESSAGE_COUNT));
                    queue.setTotalAckedMessageCount((long)queueData.get(QueueManagementInformation
                            .TOTAL_ACKED_Message_COUNT));
                    queueList.add(queue);
                }
            }
        } catch (MalformedObjectNameException | ReflectionException | MBeanException | InstanceNotFoundException e) {
            throw new QueueManagerException("Cannot access mBean operations for message counts:", e);
        } catch (AttributeNotFoundException e) {
            throw new QueueManagerException("Cannot access mBean operations for message counts. Attribute not found"
                    + QueueManagementConstants.QUEUE_INFORMATION_MBEAN_ATTRIBUTE, e);
        }
        return queueList;
    }




    /**
     * Get DLC queue registered in broker by given name (tenant information included)
     *
     * @param DLCQueueName name of the queue
     * @return Queue Bean
     * @throws QueueManagerException
     */
    public Queue getDLCQueue(String DLCQueueName) throws QueueManagerException {
        Queue DLCQueue = null;
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");

            String operationName = QueueManagementConstants.DLC_QUEUE_INFO_MBEAN_OPERATION;
            Object[] parameters = new Object[]{DLCQueueName};
            String[] signature = new String[]{String.class.getName()};

            Object result = mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);

            if (result != null) {
                Map<String, Long> queueCountMap = (Map<String, Long>) result;
                for (Map.Entry<String, Long> entry : queueCountMap.entrySet()) {
                    Queue queue = new Queue();
                    queue.setQueueName(entry.getKey());
                    queue.setPendingMessageCount(entry.getValue());
                    DLCQueue =  queue;
                }
            }
        } catch (MalformedObjectNameException | ReflectionException | MBeanException | InstanceNotFoundException e) {
            throw new QueueManagerException("Cannot access mBean operations for message counts:", e);
        }

        return DLCQueue;
    }

    /**
     * Invoke service bean to get the pending message count for a queue.
     *
     * @param queueName  The destination name.
     * @param msgPattern The value can be either "queue" or "topic".
     * @return The number of messages.
     * @throws QueueManagerException
     */
    public long getPendingMessageCount(String queueName, String msgPattern) throws QueueManagerException {
        long messageCount = 0;
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");

            String operationName = "getMessageCount";
            Object[] parameters = new Object[]{queueName, msgPattern};
            String[] signature = new String[]{String.class.getName(), String.class.getName()};
            Object result = mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);
            if (result != null) {
                messageCount = (Long) result;
            }
            return messageCount;

        } catch (MalformedObjectNameException | ReflectionException | MBeanException | InstanceNotFoundException e) {
            throw new QueueManagerException("Error while invoking mBean operations for message count:" + queueName, e);
        }
    }

    /**
     * Deletes a queue.
     * @param queueName Queue name to delete.
     * @throws QueueManagerException
     */
    public void deleteQueue(String queueName) throws QueueManagerException {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=VirtualHost.VirtualHostManager,VirtualHost=\"carbon\"");
            String operationName = "deleteQueue";

            Object[] parameters = new Object[]{queueName};
            String[] signature = new String[]{String.class.getName()};

            mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);

        } catch (MalformedObjectNameException | InstanceNotFoundException e) {
            throw new QueueManagerException("Cannot delete Queue : " + queueName, e);
        } catch (JMException e) {
            throw new QueueManagerException(e.getCause().getMessage(), e);
        }
    }

    /**
     * Invoke service bean for permanently deleting messages from the Dead Letter Channel.
     *
     * @param messageIDs          Browser message Id / External message Id list to be deleted
     * @param destinationQueueName Dead Letter Queue name for the respective tenant
     * @throws QueueManagerException
     */
    public void deleteMessagesFromDeadLetterQueue(long[] messageIDs, String destinationQueueName) throws
            QueueManagerException {
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");

            String operationName = "deleteMessagesFromDeadLetterQueue";
            Object[] parameters = new Object[]{messageIDs, destinationQueueName};
            String[] signature = new String[]{long[].class.getName(), String.class.getName()};
            mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);
        } catch (MalformedObjectNameException | ReflectionException | MBeanException | InstanceNotFoundException e) {
            throw new QueueManagerException("Error deleting messages from Dead Letter Queue : " +
                    destinationQueueName, e);
        }
    }

    /**
     * Invoke service bean for restoring messages from Dead Letter Channel to their original destinations.
     *
     * @param messageIDs          Browser message Id / External message Id list to be deleted
     * @param destinationQueueName Dead Letter Queue name for the respective tenant
     * @return unavailable message count
     * @throws QueueManagerException
     */
    public long restoreMessagesFromDeadLetterQueue(long[] messageIDs, String destinationQueueName) throws
            QueueManagerException {
        long unavailableMessageCount = 0L;
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");

            String operationName = "restoreMessagesFromDeadLetterQueue";
            Object[] parameters = new Object[]{messageIDs, destinationQueueName};
            String[] signature = new String[]{long[].class.getName(), String.class.getName()};
            Object result = mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);
            if (result != null) {
                unavailableMessageCount = (Long) result;
            }
            return unavailableMessageCount;
        } catch (MalformedObjectNameException | ReflectionException | MBeanException | InstanceNotFoundException e) {
            throw new QueueManagerException("Error restoring messages from Dead Letter Queue : " +
                    destinationQueueName, e);
        }
    }

    /**
     * Invoke service bean for restoring messages from Dead Letter Channel to a given destination.
     *
     * @param messageIDs          Browser message Id / External message Id list to be deleted
     * @param newDestinationQueueName         The new destination for the messages in the same tenant
     * @param destinationQueueName Dead Letter Queue name for the respective tenant
     * @return unavailable message count
     * @throws QueueManagerException
     */
    public long restoreMessagesFromDeadLetterQueueWithDifferentDestination(long[] messageIDs,
            String newDestinationQueueName, String destinationQueueName) throws QueueManagerException {
        long unavailableMessageCount = 0L;
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");

            String operationName = "restoreMessagesFromDeadLetterQueue";
            Object[] parameters = new Object[]{messageIDs, newDestinationQueueName, destinationQueueName};
            String[] signature = new String[]{long[].class.getName(), String.class.getName(), String.class.getName()};
            Object result = mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);
            if (null != result) {
                unavailableMessageCount = (Long) result;
            }
            return unavailableMessageCount;
        } catch (MalformedObjectNameException | ReflectionException | MBeanException | InstanceNotFoundException e) {
            throw new QueueManagerException("Error restoring messages from Dead Letter Queue : " +
                    destinationQueueName + " to " + newDestinationQueueName, e);
        }
    }

    /**
     * Invoke service bean to delete all messages of a queue.
     *
     * @param queueName The name of the queue.
     * @param userName  The username of the queue owner.
     * @throws QueueManagerException
     */
    public void purgeMessagesFromQueue(String queueName,
                                       String userName) throws QueueManagerException {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName bindingMBeanObjectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");
            String bindingOperationName = "deleteAllMessagesInQueue";

            Object[] bindingParams = new Object[]{queueName,userName};
            String[] bpSignatures = new String[]{String.class.getName(),String.class.getName()};

            mBeanServer.invoke(
                    bindingMBeanObjectName,
                    bindingOperationName,
                    bindingParams,
                    bpSignatures);

        } catch (MalformedObjectNameException | InstanceNotFoundException | ReflectionException e) {
            throw new QueueManagerException("Cannot purge Queue : " + queueName, e);
        } catch (MBeanException e) {
            throw new QueueManagerException(e.getCause().getMessage(), e);
        }
    }

    /**
     * Invoke service bean to check whether a queue exists.
     *
     * @param queueName The queue name.
     * @return True if queue exits, else false.
     * @throws QueueManagerException
     */
    public static boolean queueExists(String queueName) throws QueueManagerException {
        try {
            boolean status = false;
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");
            String operationName = "isQueueExists";
            Object[] parameters = new Object[]{queueName};
            String[] signature = new String[]{String.class.getName()};
            Object result = mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);
            if (result != null) {
                status = (Boolean) result;
            }

            return status;
        } catch (JMException e) {
            throw new QueueManagerException("Error checking if queue " + queueName + " exists.", e);
        }
    }

    /**
     * Invoke service bean to retrieve browse messages list
     *
     * @param queueName name of queue to browse
     * @param nextMessageIdToRead next start message id to get message list
     * @param maxMessageCount number of message count per page
     *
     * @return list of {@link org.wso2.carbon.andes.core.types.Message}
     */
    public List<Message> browseQueue (String queueName, long nextMessageIdToRead, int maxMessageCount)
            throws QueueManagerException {
        List<Message> browseMessageList = new ArrayList<>();
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");
            String operationName = "browseQueue";
            Object[] parameters = new Object[]{queueName, nextMessageIdToRead, maxMessageCount};
            String[] signature = new String[]{String.class.getName(), long.class.getName(), int.class.getName()};
            Object result = mBeanServer.invoke(
                    objectName,
                    operationName,
                    parameters,
                    signature);
            if (result != null) {
                CompositeData[] messageDataList = (CompositeData[]) result;
                for (CompositeData messageData : messageDataList) {
                    Message message = new Message();
                    message.setMsgProperties((String) messageData.get(QueueManagementInformation.JMS_PROPERTIES));
                    message.setContentType((String) messageData.get(QueueManagementInformation.CONTENT_TYPE));
                    message.setMessageContent((String[]) messageData.get(QueueManagementInformation.CONTENT));
                    message.setJMSMessageId((String) messageData.get(QueueManagementInformation.JMS_MESSAGE_ID));
                    message.setJMSReDelivered((Boolean) messageData.get(QueueManagementInformation.JMS_REDELIVERED));
                    message.setJMSTimeStamp((Long) messageData.get(QueueManagementInformation.TIME_STAMP));
                    message.setDlcMsgDestination((String) messageData.get(QueueManagementInformation.MSG_DESTINATION));
                    message.setAndesMsgMetadataId((Long) messageData.get(QueueManagementInformation.ANDES_MSG_METADATA_ID));
                    browseMessageList.add(message);
                }
            }
        } catch (InstanceNotFoundException | MBeanException | ReflectionException | MalformedObjectNameException e) {
            throw new QueueManagerException("Cannot browse queue : " + queueName, e);
        }
        return browseMessageList;
    }

    /**
     * Invoke service bean to retrieve names of all durable queues
     *
     * @return Set of queue names
     * @throws QueueManagerException
     */
    public Set<String> getNamesOfAllDurableQueues() throws QueueManagerException {
        Set<String> namesOfDurableQueues = new HashSet<>();
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        try {
            ObjectName objectName =
                    new ObjectName("org.wso2.andes:type=QueueManagementInformation,name=QueueManagementInformation");
            Object result = mBeanServer.getAttribute(objectName,
                    QueueManagementConstants.DURABLE_QUEUE_NAMES__MBEAN_ATTRIBUTE);
            if (result != null) {
                namesOfDurableQueues = (Set<String>) result;
            }
        } catch (MalformedObjectNameException | ReflectionException | MBeanException | InstanceNotFoundException e) {
            throw new QueueManagerException("Cannot access mBean operations for qet all queue names:", e);
        } catch (AttributeNotFoundException e) {
            throw new QueueManagerException("Incompatible attributes for operation to retrieve all queue names", e);
        }
        return namesOfDurableQueues;
    }

}
