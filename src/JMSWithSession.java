import com.rsarpal.JmsMQOperations.JmsMQMessagingSession;
import com.rsarpal.JmsMQOperations.JmsMQConnection;

public class JMSWithSession {
    JmsMQMessagingSession[] jmsContextSession;

    public static void main(String[] args){
        String xmlString ="hello test";

        JmsMQConnection jmsConn=new JmsMQConnection("localhost", 7676,"MyQueueConnectionFactory","CFQueue","admin","admin","MyQueueDest","MyQueueDest");
        jmsConn.connect();

        jmsContextSession= new JmsMQMessagingSession[10];

        for (int i=0; i<10;i++){
            jmsContextSession[i]=new jmsContextSession(jmsConn.context,jmsConn.destination); //create multiple sessions for a connection context
            jmsContextSession[i].initializeByteMessage();
            jmsContextSession[i].addByteMessageProperty("property1","value1");
            jmsContextSession[i].jmsBytesPutOnQ(xmlString);
            jmsContextSession[i].disconnect();
        }



    }




}
