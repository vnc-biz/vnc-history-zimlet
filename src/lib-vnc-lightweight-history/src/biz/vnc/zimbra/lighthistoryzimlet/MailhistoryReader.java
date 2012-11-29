/*
	http://www.vnc.biz
    Copyright 2012, VNC - Virtual Network Consult GmbH
    Released under GPL Licenses.
*/
package biz.vnc.zimbra.lighthistoryzimlet;
import biz.vnc.zimbra.util.LocalConfig;
import biz.vnc.zimbra.util.LocalDB;
import biz.vnc.zimbra.util.ZLog;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MailhistoryReader {

	private Connection dbConnection=null;

	public String getRecord(String msgId) {
		JSONObject storejson=new JSONObject();
		JSONArray jsonArray = new JSONArray();
		try {
			dbConnection = LocalDB.connect(LocalConfig.get().db_name);
			String query="SELECT * FROM mail_log_internal WHERE message_id=?" +
			             "ORDER BY logtime ASC";
			PreparedStatement statement=dbConnection.prepareStatement(query);
			statement.setString(1, msgId.trim());
			ResultSet resultSet=statement.executeQuery();
			ZLog.info("biz_vnc_lightweight_history", "Read Message Id"+msgId);
			while (resultSet.next()) {
				JSONObject jsonObject=new JSONObject();
				jsonObject.put("logtime", resultSet.getString("logtime"));
				if (resultSet.getString("from_localpart").equals("-") || resultSet.getString("from_domain").equals("-")) {
					jsonObject.put("from", "-");
				} else {
					jsonObject.put("from", resultSet.getString("from_localpart")+"@"+resultSet.getString("from_domain"));
				}
				if (resultSet.getString("to_localpart").equals("-") || resultSet.getString("to_domain").equals("-")) {
					jsonObject.put("to", "-");
				} else {
					jsonObject.put("to", resultSet.getString("to_localpart")+"@"+resultSet.getString("to_domain"));
				}
				jsonObject.put("moveto",resultSet.getString("foldername"));
				jsonObject.put("event", resultSet.getString("event"));
				jsonArray.add(jsonObject);
			}
			storejson.put("list", jsonArray);
		} catch (Exception e) {
			ZLog.err("mail-history", "getRecord: database query failed", e);
		}
		ZLog.info("biz_vnc_lightweight_history", "Recod Json :: "+storejson.toJSONString());
		return storejson.toJSONString();
	}
}
