/*
	Package: JmsMQOperations
	Author : Rishu Sarpal
	Date : 01/08/2018
	Description: Open Connections to MQ. Put and Get meessages from MQ in Text and Byte formats

    Required IBM Jars: com.ibm.mq.allclient-9.0.4.0.jar;javax.jms-api-2.0.1.jar
			
	Compile :
	javac -d . jmsMessaging.java -cp "com.ibm.mq.allclient-9.0.4.0.jar;javax.jms-api-2.0.1.jar"
	jar -cvf messaging.jar com/rsarpal/xmloperations/*.class com/rsarpal/JmsMQOperations/*.class

	Constructor:
        JmsMQOperations(String host, int port,String channel, String manager, String user, String password, String queue)

    Methods:

		 JMS 1.0 methods (for Jmeter):
		 1. public void connectOldConnectionFactory()  -  sets MQ properties and the connection and session objects.
		 2. public void createMessageConnectionF(String sendXMLString) and public void createByteMessageConnectionF(String sendXMLString) - These methods create the XML message which needs to
				be put on the queue. First method support text message , while the second is for ByteMessages.
		 3. public void jmsPutOnQConnectionF()  and public void jmsBytesPutOnQConnectionF() - These methods puts the message created by the createMessage methods in step 2 on the Destination Q
		 4. public String jmsReceiveByteFromQConnectionF()

		 JMS 2.0 methods:
		 1. public void connect()  -  sets MQ properties and the JMSContext and defines Destination objects.
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

import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;
import javax.jms.BytesMessage;

// JMS 2.0 support
import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import com.ibm.mq.headers.CCSID;
import com.ibm.mq.jms.MQDestination;

//Connection JMS 1.0 backward support
import javax.jms.Connection;
import javax.jms.Session;
import javax.jms.MessageProducer;
import javax.jms.MessageConsumer;

public class JmsMQOperations {


    // Create variables for the connection to MQ
    private  String HOST; // Host name or IP address
    private  String CHANNEL ;// = "DEV.APP.SVRCONN"; // Channel name
    private  String QMGR;   //  = "QM1"; // Queue manager name
    private  String APP_USER ; //= "APP"; // User name that application uses to connect to MQ
    private  String APP_PASSWORD ; // = "_APP_PASSWORD_"; // Password that the application uses to connect to MQ
    private  String QUEUE_NAME ; // = "InterchangeLoaderQ"; // Queue that the application uses to put and get messages to and from
    private  int PORT; // Host name or IP address
    private  String APP_NAME; //dummy name of the app to recognise files


    private JmsFactoryFactory ff; //JMS 2.0
    private JmsConnectionFactory cf; //JMS 2.0
	private Connection connection; //JMS 1.0 backward
	private Session session; //JMS 1.0 backward
	


    // JMS Variables
    private JMSContext context = null;
    private Destination destination = null;
    private JMSProducer producer = null;
    private JMSConsumer consumer = null;
    private TextMessage message= null;
	private BytesMessage byteMessage= null;




    public JmsMQOperations(String host, int port,String channel, String manager, String user, String password, String queue, String appname){
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
    public void connectOldConnectionFactory(){

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
			
			connection = cf.createConnection(); 
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			destination = session.createQueue(QUEUE_NAME);
			
        }catch (JMSException jmsex){
            System.out.println(jmsex);
        }catch (Exception e){
            System.out.println(e);
        }

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
	
	/***Functions defined which use JMS 1.0 ConnectionFactory, Backward added for JMETER***/
	
	//Create message connection using old JMS 1.0 ConnectionFactory , Added for Jmeter
	public void createMessageConnectionF(String sendXMLString){
		 try {
			 // Create JMS objects
						
            message = session.createTextMessage(sendXMLString);
						 
		}catch (Exception e){
            System.out.println(e);
        }
	}
	
	//Create Byte message connection using old JMS 1.0 ConnectionFactory , Added for Jmeter 
	public void createByteMessageConnectionF(String sendXMLString){
		 try {
			 // Create JMS objects
			
			byteMessage = session.createBytesMessage();
			
			int ccsid = ((MQDestination)destination).getIntProperty(WMQConstants.WMQ_CCSID);
			String codePage = CCSID.getCodepage(ccsid);
			byteMessage.writeBytes(sendXMLString.getBytes(codePage));
					
			 
		}catch (Exception e){
            System.out.println(e);
        }
	}
	
	//Put string message on the Q which supports JMS 1.0 ConnectionFactory
	 public void jmsPutOnQConnectionF(){
        try (MessageProducer mproducer = session.createProducer(destination)){

            mproducer.send(message);
			
            //System.out.println("Sent message:\n" + message);
        }catch (Exception e){
            System.out.println(e);
        }

    }

	//Put byte message on the Q which supports JMS 1.0 ConnectionFactory
	 public void jmsBytesPutOnQConnectionF(){
        try (MessageProducer mproducer = session.createProducer(destination)){

            mproducer.send(byteMessage);
			
            //System.out.println("Sent message:\n" + message);
        }catch (Exception e){
            System.out.println(e);
        }

    }
	
	public String jmsReceiveByteFromQConnectionF(){
       // byte[] receivedByteMessage=null;
	    BytesMessage receivedByteMessage;
		String receivedMessage="";

        try (MessageConsumer msgconsumer = session.createConsumer(destination)){
			

            //receivedByteMessage = consumer.receiveBody(byte[].class, TIMEOUT_MS); // in ms or 15 seconds			
			receivedByteMessage = (BytesMessage)msgconsumer.receive();
			
			System.out.println("" + receivedByteMessage);
			
			
			//Byte to String
			int TEXT_LENGTH = new Long(receivedByteMessage.getBodyLength()).intValue();
			System.out.println(" len" + TEXT_LENGTH);
			byte[] textBytes = new byte[TEXT_LENGTH];
			receivedByteMessage.readBytes(textBytes, TEXT_LENGTH);
			String codePage = receivedByteMessage.getStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET);
			receivedMessage = new String(textBytes, codePage);
			
			//System.out.println(" msg-" + receivedMessage);

        } catch (Exception e){
            System.out.println(e);
        }

        return receivedMessage;
    }
	
	/**End**/
	
	/***Functions defined which use JMS 2.0 JmsConnectionFactory***/
	
	//Create message connection using new JMS 2.0 JmsConnectionFactory  which supports context
	public void createMessage(String sendXMLString){
		 try {
			
            message = context.createTextMessage(sendXMLString);			
			 
		}catch (Exception e){
            System.out.println(e);
        }
	}
	
	//Create Byte message using new JMS 2.0 JMSContext and JMXConnectorFactory 
	public void createByteMessage(String sendXMLString){
		 try {
			//context = cf.createContext();
            //destination = context.createQueue("queue:///" + QUEUE_NAME);
			
			byteMessage = context.createBytesMessage();
			
			int ccsid = ((MQDestination)destination).getIntProperty(WMQConstants.WMQ_CCSID);
			String codePage = CCSID.getCodepage(ccsid);
			byteMessage.writeBytes(sendXMLString.getBytes(codePage));		
			 
		}catch (Exception e){
            System.out.println(e);
        }
	}
	
	//Put string message on the Q which supports JMS 2.0 JmsConnectionFactory
    public void jmsPutOnQ(){
        try {
            producer = context.createProducer();
            producer.send(destination, message);
            //System.out.println("Sent message:\n" + message);
        }catch (Exception e){
            System.out.println(e);
            System.out.println(e);
        }

    }
	
	//Put byte message on the Q which supports JMS 2.0 JMSConnectionFactory
	 public void jmsBytesPutOnQ(){
        try {
           			
			producer= context.createProducer();
            producer.send(destination,byteMessage);
			
        }catch (Exception e){
            System.out.println(e);
        }

    }
	
	
    public String jmsReceiveFromQ(int TIMEOUT_MS){
        String receivedMessage="";

        try (JMSConsumer recvConsumer = context.createConsumer(destination)){ // autoclosable

			//context = cf.createContext();
            //destination = context.createQueue("queue:///" + QUEUE_NAME);
			

            receivedMessage = recvConsumer.receiveBody(String.class, TIMEOUT_MS); // in ms or 15 seconds


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

		try (JMSConsumer recvConsumer = context.createConsumer(destination)){ // autoclosable

        //try {consumer = context.createConsumer(destination); // autoclosable
            //receivedByteMessage = consumer.receiveBody(byte[].class, TIMEOUT_MS); // in ms or 15 seconds			
			receivedByteMessage = (BytesMessage)recvConsumer.receive();
			
			
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

		try (JMSConsumer recvConsumer = context.createConsumer(destination)){ // autoclosable
        //try {consumer = context.createConsumer(destination); // autoclosable
            //receivedByteMessage = consumer.receiveBody(byte[].class, TIMEOUT_MS); // in ms or 15 seconds			
			receivedByteMessage = (BytesMessage)recvConsumer.receive(TIMEOUT_MS);
			
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
