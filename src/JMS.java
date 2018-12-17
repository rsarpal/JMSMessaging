import com.rsarpal.jmsMessaging.JmsMessaging;

//java -cp messaging.jar;com.ibm.mq.allclient-9.0.4.0.jar;javax.jms-api-2.0.1.jar;. JMS

public class JMS {

	public static void main (String args[]){
	String xmlString ="hello test";
	//jmsMessaging(String host, int port,String channel, String manager, String user, String password, String queue)
	//jmsMessaging jms= new jmsMessaging("kehimqrp1.kehi.okobank.net", 1414,"CLI.OPOPP01K.OPP","OPOPP01K","K-OPP-JMS","Toukokuu2018!","InterchangeLoaderQ");
	//jmsMessaging jms= new jmsMessaging("kehimqrp1.kehi.okobank.net", 3140,"CLI.OPOPP01K.OPP","OPOPP01K","K-OPP-JMS","Toukokuu2018!","InterchangeLoaderQ");
		JmsMessaging jms= new JmsMessaging("localhost", 7676,"MyQueueConnectionFactory","CFQueue","admin","admin","MyQueueDest","MyQueueDest");
	System.out.println("after constructor");
	jms.connect();
	jms.createMessage(xmlString);
	System.out.println("after connect");
	jms.jmsPutOnQ();
	   
	System.out.println("after put");
		
	}
}

