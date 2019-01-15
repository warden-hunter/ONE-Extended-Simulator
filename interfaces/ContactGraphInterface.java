package interfaces;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import routing.CGR;
import util.Tuple;
import core.CBRConnection;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Neighbors;
import core.NetworkInterface;
import core.Settings;
import core.SimClock;
import core.SimError;

/**
 * A simple Network Interface that provides a constant bit-rate service, where
 * one transmission can be on at a time.
 */
public class ContactGraphInterface  extends NetworkInterface {
	
	//����
	/** router mode in the sim -setting id ({@value})*/
	public static final String USERSETTINGNAME_S = "userSetting";
	/** router mode in the sim -setting id ({@value})*/
	public static final String ROUTERMODENAME_S = "routerMode";
	public static final String DIJSKTRA_S = "dijsktra";
	public static final String SIMPLECONNECTIVITY_S = "simpleConnectivity";

	/**
	 * Reads the interface settings from the Settings file
	 */
	public ContactGraphInterface(Settings s)	{
		super(s);
	}
		
	/**
	 * Copy constructor
	 * @param ni the copied network interface object
	 */
	public ContactGraphInterface(ContactGraphInterface ni) {
		super(ni);
	}

	public NetworkInterface replicate()	{
		return new ContactGraphInterface(this);
	}

	/**
	 * Tries to connect this host to another host. The other host must be
	 * active and within range of this host for the connection to succeed. 
	 * @param anotherInterface The interface to connect to
	 */
	public void connect(NetworkInterface anotherInterface) {
		if (isScanning()  
				&& anotherInterface.getHost().isRadioActive() 
				&& isWithinRange(anotherInterface) 
				&& !isConnected(anotherInterface)
				&& (this != anotherInterface)) {
			// new contact within range
			// connection speed is the lower one of the two speeds 
			int conSpeed = anotherInterface.getTransmitSpeed();//�������˵����������ɽ�С��һ������
			if (conSpeed > this.transmitSpeed) {
				conSpeed = this.transmitSpeed; 
			}

			Connection con = new CBRConnection(this.host, this, 
					anotherInterface.getHost(), anotherInterface, conSpeed);
			connect(con,anotherInterface);//���������˫����host�ڵ㣬����������ɵ�����con���������б���
		}
	}

	/*��������*/
	public ConnectivityOptimizer predictionUpdate(){
		if (optimizer == null) {
			return null; /* nothing to do */
		}
		optimizer.updateLocation(this);
		return optimizer;
		
	}

	/**
	 * Disconnects this host from another host.  The derived class should
	 * make the decision whether to disconnect or not
	 * @param con The connection to tear down
	 */
	public void disconnect(Connection con, 
			NetworkInterface anotherInterface) {
		super.disconnect(con, anotherInterface);
	}
	/**
	 * �Ƴ�ָ������con
	 * @param con
	 * @return
	 */
	public boolean removeConnection(Connection con){
		return this.connections.remove(con);
	}
	/*��������*/
	/**
	 * Updates the state of current connections (i.e. tears down connections
	 * that are out of range and creates new ones).
	 */
	public void update() {
		
		if (optimizer == null) {
			return; /* nothing to do */
		}
		
//		// First break the old ones
//		optimizer.updateLocation(this);
//		for (int i=0; i<this.connections.size(); ) {
//			Connection con = this.connections.get(i);
//			NetworkInterface anotherInterface = con.getOtherInterface(this);
//
//			// all connections should be up at this stage
//			assert con.isUp() : "Connection " + con + " was down!";
//
//			if (!isWithinRange(anotherInterface)) {//���½ڵ�λ�ú󣬼��֮ǰά���������Ƿ����Ϊ̫Զ���ϵ�
//				disconnect(con,anotherInterface);
//				connections.remove(i);
//				
//				//neighbors.removeNeighbor(con.getOtherNode(this.getHost()));//�ڶϵ����ӵ�ͬʱ�Ƴ����ھ��б�����ھӽڵ㣬����������
//			}
//			else {
//				i++;
//			}
//		}
		/**û����ǰ·��CGR�滮�����**/
		if (new Settings("Group").getSetting("router.CGR.type").contains("NoPreContactPlanDesign")){			
			// First break the old ones
			optimizer.updateLocation(this);
			for (int i=0; i<this.connections.size(); ) {
				Connection con = this.connections.get(i);
				NetworkInterface anotherInterface = con.getOtherInterface(this);

				// all connections should be up at this stage
				assert con.isUp() : "Connection " + con + " was down!";

				/**�����жϾ�ֱ������һ����Ϣ���͵�ʱ��**/
				
				if (this.interruptHostsList.contains(con.getOtherNode(this.getHost()))){
					if (con.isTransferring()){
//						System.out.println(((CBRConnection)con).getTransferDoneTime());
						//((CBRConnection)con).interruptConnection();
//						System.out.println(((CBRConnection)con).getTransferDoneTime());
//						throw new SimError("test");
					}										
				}					
				if (!isWithinRange(anotherInterface)) {				
					disconnect(con,anotherInterface);
					connections.remove(i);
				}		
				else {
					i++;
				}
			}
			return;
		}
		/**û����ǰ·��CGR�滮�����**/
		
		
		/**���ڶ�ȡ֮ǰԼ���õ�contactGraph�����н�������**/
//		double thisTime = SimClock.getTime();
//		/**��double���͵�ֵ���о�ȷ����**/
		BigDecimal timeNow = new BigDecimal(SimClock.getTime());  
		timeNow = timeNow.multiply(new BigDecimal(10));
//		thisTime = b.setScale(1, BigDecimal.ROUND_HALF_UP).doubleValue(); 
//		System.out.println(thisTime);
		//int index = (int)(SimClock.getTime() * 10);
		Tuple<DTNHost, DTNHost> connection = ((CGR)this.getHost().getRouter()).getContactGraph().get(this.getHost()).get(timeNow.intValue());
		//System.out.println("connection: "+connection + "time:  "+SimClock.getIntTime());
		/**��һʱ����ݽӴ�ͼԼ���õ����ӽڵ�**/
		DTNHost to;
		if (connection.getKey() == this.getHost())
			to = connection.getValue();
		else
			to = connection.getKey();

		if (!connections.isEmpty()){
			//if (connections.size() == 1)
			if (connections.get(0).getOtherNode(this.getHost()).equals(to))
				return;
		}
		else{
			if (!interruptHostsList.contains(to))
				connect(to.getInterface(1));
		}

		List<Connection> needToRemove = new ArrayList<Connection>();//��¼��Ҫ�Ƴ�����·
		
		for (int i=0; i<this.connections.size(); i++) {
			Connection con = this.connections.get(i);
			NetworkInterface anotherInterface = con.getOtherInterface(this);
	
			// all connections should be up at this stage
			assert con.isUp() : "Connection " + con + " was down!";
			/**���ڲ�����һʱ��Լ���õ���·��ȫ���Ͽ���ͬʱ��֤ͬһʱ����һ��������·**/
			if (anotherInterface != to.getInterface(1) && connections.size() > 1){//���½ڵ�λ�ú󣬼��֮ǰά���������Ƿ����Ϊ̫Զ���ϵ�
				disconnect(con,anotherInterface);
				needToRemove.add(connections.get(i));
		
			}
			if (interruptHostsList.contains(con.getOtherNode(this.getHost()))) {	//�����Ҫ�Ͽ����ӵĽڵ��б�������ȫ����·�жϵ����
				disconnect(con,anotherInterface);
				needToRemove.add(connections.get(i));
			}
			if (!isWithinRange(anotherInterface)) {//���½ڵ�λ�ú󣬼��֮ǰά���������Ƿ����Ϊ̫Զ���ϵ�
				disconnect(con,anotherInterface);
				if (!needToRemove.contains(connections.get(i)))
					needToRemove.add(connections.get(i));
			}
		}
		
		for (Connection c : needToRemove){
			connections.remove(c);//�ڴ˴��б����Ƴ�
		}
		
//		Settings s = new Settings(USERSETTINGNAME_S);
//		int mode = s.getInt(ROUTERMODENAME_S);//�������ļ��ж�ȡ·��ģʽ
//		switch(mode){
//		case 1:
//			// Then find new possible connections
//			Collection<NetworkInterface> interfaces =//�������optimizer.getNearInterfaces(this)����ȡ�ھӽڵ��ˣ��������ӵĽ���ȫ������world��java���н���
//				optimizer.getNearInterfaces(this);
//			for (NetworkInterface i : interfaces) {
//				connect(i);
//				//neighbors.addNeighbor(i.getHost());
//			}
//			break;
//		case 2 :
//			break;
//		/*case 3://�ִ�ģʽ
//			Collection<NetworkInterface> interfaces_ =//�������optimizer.getNearInterfaces(this)����ȡ�ھӽڵ��ˣ��������ӵĽ���ȫ������world��java���н���
//				optimizer.getNearInterfaces(this, clusterHosts, hostsOfGEO);
//			for (NetworkInterface i : interfaces_) {
//				connect(i);
//			}
//			break;*/
//		}
//
//		//System.out.println(this.getHost()+"  interface  "+SimClock.getTime()+" this time  "+this.connections);
//		//this.getHost().getNeighbors().updateNeighbors(this.getHost(), this.connections);//�����ھӽڵ����ݿ�
		
		//clearInterruptHostsList();//�����Ҫ�Ͽ����ӵĽڵ��б�
	}
	/**
	 * ���ڴ�������Ϣ�ĺ�����ʹ�ã�����λ��λ��DTNHost��createNewMessage(Message m)������
	 * @param msg
	 */
	public void CGRConstruct(Message msg, HashMap<DTNHost, List<Tuple<Integer, Boolean>>> routerTable){
		if (msg.getProperty("routerPath") != null)
			return;
		List<Tuple<Integer, Boolean>> path = null;
		if (((CGR)this.getHost().getRouter()).hasRouterTableUpdated() == true){
			path = routerTable.get(msg.getTo());
		}
		else{
			path = ((CGR)this.getHost().getRouter()).PathSearch(msg, null);
			//System.out.println("path search: "+path);
		}			
		if (path != null){
			List<Tuple<Integer, Boolean>> routerPath = new ArrayList<Tuple<Integer, Boolean>>();
			routerPath.add(new Tuple<Integer, Boolean>(this.getHost().getAddress(), false));//ע��˳��
			routerPath.addAll(path);
			msg.updateProperty("routerPath", routerPath);
		}
			
		//System.out.println(path);
	}
	
	/**��Ҫ�Ͽ����ӵĽڵ��б�**/
	private List<DTNHost> interruptHostsList = new ArrayList<DTNHost>();
	/**
	 * ������Ҫ�Ͽ����ӵĽڵ��б�
	 * @param ih
	 * @return
	 */
	public boolean setInterruptHost(DTNHost ih){
		return interruptHostsList.add(ih);
	}
	/**
	 * ������Ҫ�ж���·���б�
	 * @return
	 */
	public List<DTNHost> getInterruptHostsList(){
		return this.interruptHostsList;
	}
	/**
	 * �����Ҫ�Ͽ����ӵĽڵ��б�
	 */
	public void clearInterruptHostsList(){
		this.interruptHostsList.clear();
	}
	
	/** 
	 * Creates a connection to another host. This method does not do any checks
	 * on whether the other node is in range or active 
	 * @param anotherInterface The interface to create the connection to
	 */
	public void createConnection(NetworkInterface anotherInterface) {
		if (!isConnected(anotherInterface) && (this != anotherInterface)) {    			
			// connection speed is the lower one of the two speeds 
			int conSpeed = anotherInterface.getTransmitSpeed();
			if (conSpeed > this.transmitSpeed) {
				conSpeed = this.transmitSpeed; 
			}

			Connection con = new CBRConnection(this.host, this, 
					anotherInterface.getHost(), anotherInterface, conSpeed);
			connect(con,anotherInterface);
		}
	}

	/**
	 * Returns a string representation of the object.
	 * @return a string representation of the object.
	 */
	public String toString() {
		return "SatelliteLaserInterface " + super.toString();
	}

}