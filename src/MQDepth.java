
import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.MQException;


public class MQDepth {

 // Create variables for the connection to MQ

    private  String HOST; // Host name or IP address
    private  String CHANNEL ;// = "DEV.APP.SVRCONN"; // Channel name
    private  String QMGR;   //  = "QM1"; // Queue manager name
    private  String APP_USER ; //= "APP"; // User name that application uses to connect to MQ
    private  String APP_PASSWORD ; // = "_APP_PASSWORD_"; // Password that the application uses to connect to MQ
    private  String QUEUE_NAME ; // = "InterchangeLoaderQ"; // Queue that the application uses to put and get messages to and from
	private  String APP_NAME; //dummy name of the app to recognise files
    private  int PORT; // Host name or IP address
	private  MQQueueManager qmgr;
	
	public MQDepth(){
	}
	
	public void connectQManager(String host, int port,String channel, String manager, String user, String password, String appname){
        HOST = host;
        CHANNEL = channel;
        QMGR=manager;
        APP_USER=user;
        APP_PASSWORD=password;
        //QUEUE_NAME=queue;
        PORT=port;
		APP_NAME=appname;
		try {
		this.qmgr= createQueueManager();
		} catch (MQException me){
			me.printStackTrace();
		}
	}
	
	
	public int depthOf(String queueName) throws MQException {
        MQQueue queue = qmgr.accessQueue(queueName, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_INPUT_AS_Q_DEF, null, null, null);
        int depth= queue.getCurrentDepth();
        queue.close();
        
        return  depth;
    }
	
	@SuppressWarnings("unchecked")
    private MQQueueManager createQueueManager() throws MQException {
        MQEnvironment.channel = CHANNEL;
        MQEnvironment.port = PORT;
        MQEnvironment.hostname = HOST;
		MQEnvironment.userID=APP_USER;
		MQEnvironment.password=APP_PASSWORD;
		MQEnvironment.properties.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES);
        return new MQQueueManager(QMGR);
    }
	
	
	public void disconnect(){
		try {
			qmgr.close();
		} catch (MQException me){
			me.printStackTrace();
		}
	}
	
	
}