/* 
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details. 
 */
package input;

import java.util.Random;

import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.World;

/**
 * External event for creating a message.
 */
public class MessageCreateEvent extends MessageEvent {
	private int size;
	private int responseSize;
	/**------------------------------   �� MessageCreateEvent ���ӵĲ���       --------------------------------*/
	
	private String fileID; 			// �������ļ���ID��
	public final static String SelectLabel = "PacketType";
	/** user setting in the sim -setting id ({@value})*/
	public static final String USERSETTINGNAME_S = "userSetting";
	/** user setting in the sim Cache */
	public static final String EnableCache_s = "EnableCache";
    /** number of files */
	public static final String nrofFile_s = "nrofFile";
	/** namespace for host group settings ({@value})*/
	public static final String GROUP_NS = "Group";
	/** retransmission time of message */
	private static final String RETRANS_TIME = "reTransTime";
	/**------------------------------   �� MessageCreateEvent ���ӵĲ���       --------------------------------*/
	
	/**
	 * Creates a message creation event with a optional response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param size Size of the message
	 * @param responseSize Size of the requested response message or 0 if
	 * no response is requested
	 * @param time Time, when the message is created
	 */
	public MessageCreateEvent(int from, int to, String id, int size,
			int responseSize, double time) {
		super(from,to, id, time);
		this.size = size;
		this.responseSize = responseSize;
	}

	@Override
	public String toString() {
		return super.toString() + " [" + fromAddr + "->" + toAddr + "] " +
		"size:" + size + " CREATE";
	}
	
	/**------------------------------   �� MessageCreateEvent ���ӵĺ�������       --------------------------------*/
	
	/** �й����ļ�����޸ĵĲ���       */
	public String RandomGetFileID() {
		Settings ss = new Settings(GROUP_NS);					// ÿһ����������һ�����ö��󣬾���Ŀ��ܺ������ռ��й�
		int nrofFile = ss.getInt(nrofFile_s);						// default�趨���ļ���Ŀ
		Random random = new Random();
		int id =random.nextInt(nrofFile);
		return "filename" +id;										// return filename;
	}
	/**
	 * Creates the message this event represents. 
	 */
	@Override
	public void processEvent(World world) {
		Settings setting = new Settings(USERSETTINGNAME_S);		//��ȡ���ã��ж��Ƿ���Ҫ�ִ�
		String cacheEnable = setting.getSetting(EnableCache_s); // decide whether to enable the cache function
		
//		if (cacheEnable.indexOf("true") >= 0) {
//	        this.fileID = RandomGetFileID();
//	        
//	        DTNHost from = world.getNodeByAddress(this.fromAddr);
//			this.toAddr = from.getFiles().get(this.fileID);							// �޸�
//			DTNHost to = world.getNodeByAddress(this.toAddr);
//
//			this.responseSize = to.getFileBuffer().get(this.fileID).getSize();		// responseSize�趨�����ļ��Ĵ�С��
//			
//			Message m = new Message(from, to, this.id, this.size);
//			m.setResponseSize(this.responseSize);
//			m.setFilename(this.fileID);
//			m.updateProperty(SelectLabel, 0);													// ��ʶΪ���ư�
//
////			System.out.println("��ǰ�ڵ��Ƿ�����ļ���"+ from.getFileBuffer().containsKey(this.fileID) +" " + "��ǰʱ�̣�"+ SimClock.getTime() );
//			
//			// ���Ŀ�Ľڵ��Դ�ڵ㲻ͬ���Ŵ�����Ϣ����Ϊȡ���ļ�������ģ�     ͬʱ����ڵ㻺�����ļ������ٷ�������		
//			if(this.toAddr!=this.fromAddr && !from.getFileBuffer().containsKey(this.fileID)) {	
//				from.createNewMessage(m); 														// ����Ϣ�Ž�������ȥ
////				from.putIntoJudgeForRetransfer(m);												// ��Ҫ����Ϣ���뵽�ж���Ϣ�Ƿ��ش���buffer��
//			}
//		}	
//		else{

			DTNHost to = world.getNodeByAddress(this.toAddr);
			DTNHost from = world.getNodeByAddress(this.fromAddr);			
			Message m = new Message(from, to, this.id, this.size);
			
			// set the retransmission time 
		    Settings s = new Settings("Interface");
		    int time = s.getInt("reTransmitTime"); 
			m.updateProperty(RETRANS_TIME, time);
			
			m.setResponseSize(this.responseSize);
			from.createNewMessage(m);
//		}

	}
	
	/**------------------------------   �� MessageCreateEvent ���ӵĺ�������       --------------------------------*/
}