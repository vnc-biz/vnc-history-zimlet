/*
	http://www.vnc.biz
	Copyright 2014-TODAY, VNC - Virtual Network Consult AG
	Released under GPL Licenses.
*/
package biz.vnc.zimbra.lighthistoryzimlet;
import java.io.File;
import java.io.RandomAccessFile;
import biz.vnc.zimbra.util.JSPUtil;
import biz.vnc.zimbra.util.ZLog;
import com.zimbra.cs.account.soap.SoapProvisioning.Options;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.account.Domain;
import java.util.List;

public class MailHistoryLogging {
	public static final String DELIVERED = "1";
	public static final String MOVE = "2";
	public static final String DELETE = "3";
	public static Thread internalthread = null,externalthread=null,availablethread=null;
	public static File fstream=null,filestream=null;
	public static SoapProvisioning provisioning;

	static {
		try{
			fstream=new File("/opt/zimbra/log/mailbox.log");
			filestream = new File("/var/log/zimbra.log");
			internalthread = new Thread(new RecipientExternalMailHistoryLogging(filestream));
			externalthread = new Thread(new RecipientInternalMailHistoryLogging(fstream));
			Options options = new Options();
			options.setLocalConfigAuth(true);
			provisioning = new SoapProvisioning(options);
		} catch(Exception e) {
			ZLog.err(
			    "biz_vnc_lightweight_history",
			    "Error while static method called",
			    e
			);
		}
		internalthread.start();
		externalthread.start();
	}

	public static String getDataFromBracket(String name) {
		if (name == null) {
			return null;
		}
		if (name.contains("<")) {
			return name.substring(name.indexOf("<")+1, name.indexOf(">",name.indexOf("<")));
		}
		return name;
	}
}

class RecipientExternalMailHistoryLogging extends Thread {
	long filePointer;
	public static File filestream=null;

	public  RecipientExternalMailHistoryLogging (File fstream) {
		filestream = fstream;
		filePointer = filestream.length();
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				long len = filestream.length();
				if (len < filePointer) {
					filePointer = len;
				} else {
					RandomAccessFile raf = new RandomAccessFile(filestream, "r");
					raf.seek(filePointer);
					String line = null;
					while ((line = raf.readLine()) != null) {
						getExternalMailEvents(line);
					}
					filePointer = raf.getFilePointer();
					raf.close();
				}
				continue;
			} catch (Exception e) {
				ZLog.err(
				    "biz_vnc_lightweight_history",
				    "Error in run method while ExternalMailHistoryLogging",
				    e
				);
			}
		}
	}

	public void getExternalMailEvents(String strLine) {
		try {
			String senderName = null;
			String receiverName = null;
			String message_id = null;
			String receivers = null;
			String sender = null;
			if(strLine.contains("Passed CLEAN")&& strLine.contains("Queue-ID")) {
				receivers = strLine.substring(strLine.indexOf("->")+2,strLine.indexOf(", Queue-ID")).trim();
				sender = strLine.split("->")[0].split("<")[1].split(">")[0];
				message_id=MailHistoryLogging.getDataFromBracket(strLine.split("Message-ID:")[1].trim());
			} else if(strLine.contains("Passed CLEAN")) {
				if(strLine.contains("Message-ID")) {
					receivers = strLine.substring(strLine.indexOf("->")+2,strLine.indexOf(", Message-ID")).trim();
					sender = strLine.split("->")[0].split("<")[1].split(">")[0];
					message_id=MailHistoryLogging.getDataFromBracket(strLine.split("Message-ID:")[1].trim());
				}
			}
			if(message_id!=null && receivers!=null) {
for(String receiver : receivers.split(",")) {
					String ereceiver = MailHistoryLogging.getDataFromBracket(receiver);
					if(isExternalMail(ereceiver)) {
						ZLog.info(
						    "biz_vnc_lightweight_history",
						    "External Mail Deliver Event"
						);
						ZLog.info(
						    "biz_vnc_lightweight_history",
						    "Sender : "+sender
						);
						ZLog.info(
						    "biz_vnc_lightweight_history",
						    "External Receiver : "+ereceiver
						);
						ZLog.info(
						    "biz_vnc_lightweight_history",
						    "Event : "+MailHistoryLogging.DELIVERED
						);
						ZLog.info(
						    "biz_vnc_lightweight_history",
						    "Message_id : "+message_id
						);
						DataBaseUtil.writeHistory(
						    message_id,
						    sender,
						    ereceiver,
						    MailHistoryLogging.DELIVERED,
						    "",
						    ""
						);
					}
				}
				message_id=null;
				receivers=null;
				sender=null;
			}
		} catch(Exception e) {
			ZLog.err(
			    "biz_vnc_lightweight_history",
			    "Error while getting External Mail Event ",
			    e
			);
		}
	}

	synchronized public boolean isExternalMail(String receiver) {
		List<Domain> alldomains = null;
		boolean result = false;
		int flag =1;
		String receiverdomain = receiver.split("@")[1];
		try {
			Options options = new Options();
			options.setLocalConfigAuth(true);
			SoapProvisioning provisioning = new SoapProvisioning(options);
			alldomains = provisioning.getAllDomains();
		} catch(Exception e) {
			ZLog.err(
			    "biz_vnc_lightweight_history",
			    "Error while getting Domains List ",
			    e
			);
		}
		for(int i=0; i<alldomains.size(); i++) {
			if(alldomains.get(i).getName().equals(receiverdomain)) {
				flag =0;
			}
		}
		if(flag == 1) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}
}

class RecipientInternalMailHistoryLogging extends Thread {
	public static File filestream=null;
	long filePointer;
	String msgId = null;
	String senderName=null;

	public RecipientInternalMailHistoryLogging(File stream) {
		filestream = stream;
		filePointer = filestream.length();
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(1000);
				long len = filestream.length();
				if (len < filePointer) {
					filePointer = len;
				} else if (len > filePointer) {
					RandomAccessFile raf = new RandomAccessFile(filestream, "r");
					raf.seek(filePointer);
					String line = null;
					while ((line = raf.readLine()) != null) {
						getInternalMailEvents(new String(line.getBytes("ISO-8859-1")));
					}
					filePointer = raf.getFilePointer();
					raf.close();
				}
				continue;
			} catch (Exception e) {
				ZLog.err(
				    "biz_vnc_lightweight_history",
				    "Error while  run method in InternalMailHistoryLogging",
				    e
				);
			}
		}
	}

	public void getInternalMailEvents(String strLine) {
		try {
			String receivermsgId;
			String receiverName=null;
			if(strLine.contains("lmtp - Delivering")) {
				if(strLine.contains("sender")) {
					senderName = strLine.split("msgid=")[0].split("sender=")[1].split(",")[0].trim();
				}
				if(strLine.contains("msgid")) {
					msgId = MailHistoryLogging.getDataFromBracket(strLine.split("msgid=")[1]);
				}
			}
			if(strLine.contains("mailop - Adding Message")) {
				String receiverdata[] = strLine.split(",");
for(String receive:receiverdata) {
					if(receive.contains("name")) {
						receiverName = receive.split("=")[1].split(";")[0];
					}
					if(receive.contains("Message-ID")) {
						receivermsgId = MailHistoryLogging.getDataFromBracket(receive.split("=")[1]);
						if(msgId !=null) {
							if(msgId.equals(receivermsgId)) {
								String smallMessageId = strLine.split("id=")[2].split(",")[0];
								ZLog.info(
								    "biz_vnc_lightweight_history",
								    "smallid : "+smallMessageId
								);
								ZLog.info(
								    "biz_vnc_lightweight_history",
								    "message_id : "+msgId
								);
								ZLog.info(
								    "biz_vnc_lightweight_history",
								    "Sender : "+senderName
								);
								ZLog.info(
								    "biz_vnc_lightweight_history",
								    "Receiver : "+receiverName
								);
								ZLog.info(
								    "biz_vnc_lightweight_history",
								    "Event : "+MailHistoryLogging.DELIVERED
								);
								DataBaseUtil.writeHistory(
								    msgId,
								    senderName,
								    receiverName,
								    MailHistoryLogging.DELIVERED,
								    smallMessageId,
								    ""
								);
							}
						}
					}
				}
			}
			if(strLine.contains("mailop - Moving Conversation") || strLine.contains("mailop - moving Conversation")) {
				String moveinfoid = strLine.split(",")[1].split("INFO")[0];
				String moveId = strLine.split("Affected message ids:")[1];
				String receiver = strLine.split("]")[1].split("=")[1].split(";")[0];
				moveId = moveId.substring(0,moveId.length()-1).trim();
				String messageId=DataBaseUtil.getMsgId(moveId,receiver);
				String folderName = strLine.split("Folder")[1].split("\\(")[0].trim();
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Moving to : "+folderName
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "message_id : "+messageId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "small_id : "+moveId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "info_id : "+moveinfoid
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Receiver : "+receiver
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Event : "+MailHistoryLogging.MOVE
				);
				if(!messageId.equals("")) {
					DataBaseUtil.writeMoveHistory(
					    messageId,
					    "",
					    receiver,
					    MailHistoryLogging.MOVE,
					    moveId,
					    moveinfoid,
					    folderName
					);
				}
			}
			if(strLine.contains("mailop - Moving Message")) {
				String moveinfoid = strLine.split(",")[1].split("INFO")[0];
				String moveId = strLine.split("Affected message ids:")[1];
				String receiver = strLine.split("]")[1].split("=")[1].split(";")[0];
				moveId = moveId.substring(0,moveId.length()-1).trim();
				String messageId=DataBaseUtil.getMsgId(moveId,receiver);
				String folderName = strLine.split("Folder")[1].split("\\(")[0].trim();
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Moving to : "+folderName
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "message_id : "+messageId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "small_id : "+moveId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "info_id : "+moveinfoid
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Receiver : "+receiver
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Event : "+MailHistoryLogging.MOVE
				);
				if(!messageId.equals("")) {
					DataBaseUtil.writeMoveHistory(
					    messageId,
					    "",
					    receiver,
					    MailHistoryLogging.MOVE,
					    moveId,
					    moveinfoid,
					    folderName
					);
				}
			}
			if(strLine.contains("mailop - moving Message")) {
				String moveinfoid = strLine.split(",")[1].split("INFO")[0];
				String moveId = strLine.split("\\(id=")[1].split("\\) to Folder")[0].trim();
				String receiver = strLine.split("]")[1].split("=")[1].split(";")[0];
				String messageId=DataBaseUtil.getMsgId(moveId,receiver);
				String folderName = strLine.split("Folder")[1].split("\\(")[0].trim();
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Moving to : "+folderName
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "message_id : "+messageId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "small_id : "+moveId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "info_id : "+moveinfoid
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Receiver : "+receiver
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Event : "+MailHistoryLogging.MOVE
				);
				if(!messageId.equals("")) {
					DataBaseUtil.writeMoveHistory(
					    messageId,
					    "",
					    receiver,
					    MailHistoryLogging.MOVE,
					    moveId,
					    moveinfoid,
					    folderName
					);
				}
			}
			if(strLine.contains("mailop - Moving VirtualConversation") || strLine.contains("mailop - moving VirtualConversation")) {
				String moveinfoid = strLine.split(",")[1].split("INFO")[0];
				String moveId = strLine.split("Affected message ids:")[1];
				String receiver = strLine.split("]")[1].split("=")[1].split(";")[0];
				moveId = moveId.substring(0,moveId.length()-1).trim();
				String messageId=DataBaseUtil.getMsgId(moveId,receiver);
				String folderName = strLine.split("Folder")[1].split("\\(")[0].trim();
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Moving to : "+folderName
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "message_id : "+messageId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "small_id : "+moveId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "info_id : "+moveinfoid
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Receiver : "+receiver
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Event : "+MailHistoryLogging.MOVE
				);
				if(!messageId.equals("")) {
					DataBaseUtil.writeMoveHistory(messageId, "", receiver, MailHistoryLogging.MOVE, moveId, moveinfoid,folderName);
				}
			}
			if(strLine.contains("Deleting items")) {
				String deletedId = strLine.split(":")[4];
				deletedId = deletedId.substring(0,deletedId.length()-1).trim();
				String receiver =strLine.split("]")[1].split("=")[1].split(";")[0];
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "small ID : "+deletedId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Receiver : "+receiver
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Event : "+MailHistoryLogging.DELETE
				);
				DataBaseUtil.writeDeleteHistory(deletedId,receiver);
			}
			if(strLine.contains("mailop - Deleting Message")) {
				String deletedId = strLine.split("id")[2];
				deletedId = deletedId.substring(1,deletedId.length()-2).trim();
				String receiver =strLine.split("]")[1].split("=")[1].split(";")[0];
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "small ID : "+deletedId
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Receiver : "+receiver
				);
				ZLog.info(
				    "biz_vnc_lightweight_history",
				    "Event : "+MailHistoryLogging.DELETE
				);
				DataBaseUtil.writeDeleteHistory(deletedId,receiver);
			}
		} catch(Exception e) {
			ZLog.err(
			    "biz_vnc_lightweight_history",
			    "Error while getting Internal Mail Event from Log file",
			    e
			);
		}
	}
}
