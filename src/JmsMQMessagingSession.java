/*
	Package: JmsMQOperations
	Author : Rishu Sarpal
	Date : 17/11/2018
	Description: Split JmsMQOperations class into two classes JmsMQConnection and JmsMQMessagingSession
	JmsMQConnection - Open Connections to MQ.
	JmsMQMessagingSession- Opens JMSContext sessions and has functions for Put and Get meessages from MQ in Text and Byte formats

    Required IBM Jars: com.ibm.mq.allclient-9.0.4.0.jar;javax.jms-api-2.0.1.jar

	Compile :
	javac -d . JmsMQMessagingSession.java -cp "com.ibm.mq.allclient-9.0.4.0.jar;javax.jms-api-2.0.1.jar"
	jar -cvf messaging.jar com/rsarpal/xmloperations/*.class com/rsarpal/JmsMQConnection/*.class com/rsarpal/JmsMQMessagingSession/*.class

	Constructor:
        JmsMQMessagingSession(JMSContext context,Destination destination) -
        	Parameter to be passed from object of JmsMQConnection class. Creates sessions for a ConnectionFactory JMSContext

    Methods:

		 JMS 1.0 methods (for Jmeter):

		 2. public void createMessageConnectionF(String sendXMLString) and public void createByteMessageConnectionF(String sendXMLString) - These methods create the XML message which needs to
				be put on the queue. First method support text message , while the second is for ByteMessages.
		 3. public void jmsPutOnQConnectionF()  and public void jmsBytesPutOnQConnectionF() - These methods puts the message created by the createMessage methods in step 2 on the Destination Q
		 4. public String jmsReceiveByteFromQConnectionF()

		 JMS 2.0 methods:

		 1. public viod disconnect() - closes context.
		 2. public void initializeByteMessage() and public void initializeMessage() - this method initializes the ByteMessage or Message object.
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
import com.ibm.msg.client.wmq.WMQConstants;

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;
import javax.jms.BytesMessage;


import com.ibm.mq.headers.CCSID;
import com.ibm.mq.jms.MQDestination;

//Connection 
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;

public class JmsMQMessagingSession  {


    // JMS Variables
	private JMSContext sessionContext = null;
    private Destination destination = null;
    private JMSProducer producer = null;
    private JMSConsumer consumer = null;
    private TextMessage message= null;
	private BytesMessage byteMessage= null;
	 
	private int ccsid;
	private String codePage=null;
	

    public JmsMQMessagingSession(JMSContext context,Destination destination){
		try {
		sessionContext= context.createContext(JMSContext.AUTO_ACKNOWLEDGE);
		this.destination=destination;
		//set Bytemessage format
		ccsid = ((MQDestination)destination).getIntProperty(WMQConstants.WMQ_CCSID);
		codePage= CCSID.getCodepage(ccsid);
		 }catch (Exception e){
            System.out.println(e);
        }
		
	}
	
	public void disconnect(){
		 
		 sessionContext.close();		 
	 }
	
	public void initializeByteMessage(){
     
   		try {
		
			byteMessage = sessionContext.createBytesMessage();
			
		 
        }catch (Exception e){
            System.out.println(e);
        }
			
	}
	
	
	/***Functions defined which use JMS 2.0 JmsConnectionFactory***/
	
	
	
		//uses primary context for single sessions
	public void jmsBytesPutOnQ(String sendXMLString){
        try {
			producer = sessionContext.createProducer();
			byteMessage.writeBytes(sendXMLString.getBytes(codePage));
			producer.send(destination,byteMessage);
			
        }catch (Exception e){
            System.out.println(e);
        }

    }
	
	
    public String jmsReceiveFromQ(int TIMEOUT_MS){
        String receivedMessage="";

        try {

			//context = cf.createContext();
            //destination = context.createQueue("queue:///" + QUEUE_NAME);
			
            consumer = sessionContext.createConsumer(destination); // autoclosable
            receivedMessage = consumer.receiveBody(String.class, TIMEOUT_MS); // in ms or 15 seconds


        } catch (Exception e){
            System.out.println(e);
        }

        return receivedMessage;
    }
	
	// this will keep waiting for a message in the queue to appear for reading. No timeout set
	 public String jmsReceiveByteFromQ(){
       // byte[] receivedByteMessage=null;
	    BytesMessage receivedByteMessage;
		String receivedMessage="";

        try {
			
            consumer = sessionContext.createConsumer(destination); // autoclosable
            //receivedByteMessage = consumer.receiveBody(byte[].class, TIMEOUT_MS); // in ms or 15 seconds			
			receivedByteMessage = (BytesMessage)consumer.receive();
			
			
			//Byte to String
			int TEXT_LENGTH = new Long(receivedByteMessage.getBodyLength()).intValue();
			byte[] textBytes = new byte[TEXT_LENGTH];
			receivedByteMessage.readBytes(textBytes, TEXT_LENGTH);
			String codePage = receivedByteMessage.getStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET);
			receivedMessage = new String(textBytes, codePage);

        } catch (Exception e){
            System.out.println(e);
        }

        return receivedMessage;
    }
	
	// this will keep wait for a message in the queue to appear for reading until Timeout is reached
	 public String jmsReceiveByteFromQ(int TIMEOUT_MS ){
       // byte[] receivedByteMessage=null;
	    BytesMessage receivedByteMessage;
		String receivedMessage="";

        try {
			
            consumer = sessionContext.createConsumer(destination); // autoclosable
            //receivedByteMessage = consumer.receiveBody(byte[].class, TIMEOUT_MS); // in ms or 15 seconds			
			receivedByteMessage = (BytesMessage)consumer.receive(TIMEOUT_MS);
			
			if (receivedByteMessage!=null){
			
			//Byte to String
			int TEXT_LENGTH = new Long(receivedByteMessage.getBodyLength()).intValue();
			byte[] textBytes = new byte[TEXT_LENGTH];
			receivedByteMessage.readBytes(textBytes, TEXT_LENGTH);
			String codePage = receivedByteMessage.getStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET);
			receivedMessage = new String(textBytes, codePage);
			}
			
        } catch (Exception e){
            System.out.println(e);
        }

        return receivedMessage;
    }
	
	/**End**/
	
	//add custom Message property for String Message
	public void addMessageProperty(String custProperty, String custValue){
		 try {
		
			message.setStringProperty(custProperty,custValue);		
			 }catch (JMSException jmsex){
            System.out.println(jmsex);
        }
	}
	
	//add custom Message property for Byte Message
	public void addByteMessageProperty(String custProperty, String custValue){
		 try {
		
			byteMessage.setStringProperty(custProperty,custValue);		
			 }catch (JMSException jmsex){
            System.out.println(jmsex);
        }
	}

	
	
	
	

}
