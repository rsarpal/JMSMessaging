/*
	Package: JmsMQOperations
	Author : Rishu Sarpal
	Date : 17/11/2018
	Description: Split JmsMQOperations class into two classes JmsMQConnection and JmsMQMessagingSession
	JmsMQConnection - Open Connections to MQ.
	JmsMQMessagingSession- Opens JMSContext sessions and has functions for Put and Get meessages from MQ in Text and Byte formats

    Required IBM Jars: com.ibm.mq.allclient-9.0.4.0.jar;javax.jms-api-2.0.1.jar

	Compile :
	javac -d . JmsMQConnection.java -cp "com.ibm.mq.allclient-9.0.4.0.jar;javax.jms-api-2.0.1.jar"
	jar -cvf messaging.jar com/rsarpal/xmloperations/*.class com/rsarpal/JmsMQConnection/*.class com/rsarpal/JmsMQMessagingSession/*.class

	Constructor:
        JmsMQConnection(String host, int port,String channel, String manager, String user, String password, String queue)

    Methods:



		 JMS 2.0 methods:
		 1. public void connect()  -  sets MQ properties and the JMSContext and defines Destination objects.
		 2. public viod disconnect() - closes context.
		 2. public void createMessage(String sendXMLString) and public void createByteMessage(String sendXMLString) - These methods create the XML message which needs to
				be put on the queue. First method support text message , while the second is for ByteMessages.
		 3. public void jmsPutOnQ() and  public void jmsBytesPutOnQ() -  These methods puts the message created by the createMessage methods in step 2 on the Destination Q
		 4. MQ Get - Below functions Gets the message from queue and return them in String format.
			 - public String jmsReceiveByteFromQ() - Get byte message from queue and convert it into String object. This method keeps waiting for a message to appear on the Q, there is no timeout.
			 - public String jmsReceiveByteFromQ(int TIMEOUT_MS ) - Get byte message from queue and convert it into String object. This method will wait for or a message to appear on the Q as per defined timeout.
			 - public String jmsReceiveFromQ(int TIMEOUT_MS) - Get string message from queue and return String object.

		Common Methods:

		 1. public void addMessageProperty(String custProperty, String custValue) , addByteMessageProperty(String custProperty, String custValue) - These methods are used to
		    add custom message properties needed for placing the message on MQ.


*/
package com.rsarpal.JmsMQOperations;


import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.ibm.mq.headers.CCSID;
import com.ibm.mq.jms.MQDestination;

import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Destination;
import javax.jms.Session;
import javax.jms.Connection;
import javax.jms.Session;

public class JmsMQConnection {
	
	 // Create variables for the connection to MQ
    private  String HOST; // Host name or IP address
    private  String CHANNEL ;// = "DEV.APP.SVRCONN"; // Channel name
    private  String QMGR;   //  = "QM1"; // Queue manager name
    private  String APP_USER ; //= "APP"; // User name that application uses to connect to MQ
    private  String APP_PASSWORD ; // = "_APP_PASSWORD_"; // Password that the application uses to connect to MQ
    private  String QUEUE_NAME ; // = "InterchangeLoaderQ"; // Queue that the application uses to put and get messages to and from
	private  String APP_NAME; //dummy name of the app to recognise files
    private  int PORT; // Host name or IP address


    private JmsFactoryFactory ff; //IBM JMS 2.0
    private JmsConnectionFactory cf; //IBM JMS 2.0
    private Connection jmsConnection;
    private Session jmsSession;
	
	// JMS Variables
    public JMSContext context = null;
	public Destination destination = null;
	
	public JmsMQConnection(String host, int port,String channel, String manager, String user, String password, String queue, String appname){
        HOST = host;
        CHANNEL = channel;
        QMGR=manager;
        APP_USER=user;
        APP_PASSWORD=password;
        QUEUE_NAME=queue;
        PORT=port;
		APP_NAME=appname;
	}
	
	// set connection parameters for the queue
    public void connect(){

        try {
            // Create a connection factory
            ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
            cf = ff.createConnectionFactory();

            // Set the properties
            cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
            cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
            cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
            cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
            cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
            cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, APP_NAME);
            cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
            cf.setStringProperty(WMQConstants.USERID, APP_USER);
            cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
			
			//Create context and set destination queue
			context = cf.createContext();
			destination = context.createQueue("queue:///" + QUEUE_NAME);
			
			        		
			
        }catch (JMSException jmsex){
            System.out.println(jmsex);
        }catch (Exception e){
            System.out.println(e);
        }

    }

	
	 public void disconnect(){
		 
		 context.close();		 
	 }



}