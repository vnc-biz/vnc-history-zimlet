<%
/*
	http://www.vnc.biz
	Copyright 2012, VNC - Virtual Network Consult GmbH
	Released under GPL Licenses.
*/
%>
<html>
<head><title>Print Mail History</title>
<style>
</style>
</head>
<body onLoad="window.print();">
<link rel="stylesheet" type="text/css" href="lightweightmailhistory.css" />
<%@ page import = "biz.vnc.zimbra.lighthistoryzimlet.MailhistoryReader" %>
<%@ page import = "org.json.simple.JSONObject" %>
<%@ page import = "org.json.simple.JSONArray" %>
<%@ page import = "org.json.simple.parser.JSONParser" %>
<%@ page import = "java.util.Properties" %>
<%@ page import = "java.util.Locale" %>
<%@ page import = "java.io.InputStream" %>
<%@ page import = "biz.vnc.zimbra.util.JSPUtil" %>
<% 
	JSPUtil.nocache(response);
	String locale = request.getParameter("locale");
	String fileName = "/biz_vnc_lightweight_history/biz_vnc_lightweight_history_" + locale + ".properties";

	InputStream is = null;

	try{
		is = application.getResourceAsStream(fileName);
		if(is == null){
			throw new Exception("File not found");
		}
	}catch(Exception e){
		System.out.println("No file found for locale " + locale + ". Hence, reading default file");
		try{
			is = application.getResourceAsStream("/biz_vnc_lightweight_history/biz_vnc_lightweight_history.properties");
		}catch(Exception ee){

		}
	}

	Properties prop = new Properties();
	prop.load(is);
	
	String subject=request.getParameter("s");
	String from=request.getParameter("from");
	String dateLabel=prop.getProperty("dateLabel");
	String eventLabel=prop.getProperty("eventLabel");
	String receiver=prop.getProperty("receiver");
	String forwardLabel=prop.getProperty("forwardLabel");
	String msender=prop.getProperty("sender");
	String noresult=prop.getProperty("noResult");
	String deliverLabel=prop.getProperty("deliverLabel");
	String moveLabel=prop.getProperty("moveLabel");
	String deleteLabel=prop.getProperty("deleteLabel");
	String noSubject=prop.getProperty("noSubject");
	String mainsender=request.getParameter("mainsender");
	String unauthorized=prop.getProperty("unauthorized");
	String messageId = request.getParameter("msgid");
	String forward=prop.getProperty("forward");
	String mailFor = prop.getProperty("mailFor");
	String fail= prop.getProperty("deliveryfail");
	String moveto = prop.getProperty("moveLableto");
	
	String jsonString = new MailhistoryReader().getRecord(messageId);
	JSONObject jsonObject =  (JSONObject)new JSONParser().parse(jsonString);
	JSONArray jsonArray = (JSONArray)jsonObject.get("list");
	
%>

<div style="height: auto; width: auto;">
<table width="98%" align="center" class="lightweightgridtable">
<tr>
	<td colspan="4">
	<h3 style="padding:3px 3px 3px 3px;align:center;"><%=mailFor%> 
	
	<%
		if(subject.equals("undefined")){
			subject=noSubject;
		}
	%>
	'<%=subject%>'</h3>
	</td>
</tr>
<tr>
	<td colspan='4'><%=msender%> : <b> <%=mainsender%> </b></td>
</tr>
<tr>
	<td colspan='4'><hr width='100%'/></td>
<tr>
</table>
<table cellspacing="0" width="98%" align="center" class="lightweighthistoryrecord">
<tr class='tr_title'>
	<td><%=dateLabel%></td>
	<td><%=receiver%></td>
	<td><%=eventLabel%></td>
	<td><%=moveto%></td>
<tr>
<%

	if(!from.equals(mainsender)){
		out.println("<tr>");	
		out.println("<td colspan=4 align=center><h4>");
		out.println(unauthorized+"</h4></td>");
		out.println("</tr>");
	}
%>
<%
	if(jsonArray.size()==0){
		out.println("<tr>");
		out.println("<td colspan=4 align=center><h4>"+noresult);
		out.println("</h4></td>");
		out.println("</tr>");
	}else{
		JSONObject temp = null;
		String logtime = null;
		String to = null;
		String event = null;
		String fwd= null;

		for(int i=0;i<jsonArray.size();i++){
			temp = (JSONObject)jsonArray.get(i);	
			logtime = (String)temp.get("logtime");
			to = (String)temp.get("to");
			event=(String)temp.get("event");
			String eventname=null;
			if(i%2==0){
				out.println("<tr class='odd'>");
			}else{
				out.println("<tr class='even'>");
			}	
			
			out.println("<td>"+logtime+"</td><td>"+to+"</td>");
		
			if(event.equals("1")){
				eventname=deliverLabel;
			}else if(event.equals("2")){
				eventname=moveLabel;
			}else if(event.equals("3")){
				eventname=deleteLabel;
			}else if(event.equals("5")){
				eventname=fail;
			}
			
			out.println("<td>"+eventname+"</td>");
			out.println("<td>"+(String)temp.get("moveto")+"</td>");
			out.println("</tr>");
		}	
	}	
%>
</table>
</div>
</body>
</html>
