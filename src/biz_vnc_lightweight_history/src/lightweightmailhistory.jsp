<%
/*
	http://www.vnc.biz
    Copyright 2012, VNC - Virtual Network Consult GmbH
    Released under GPL Licenses.
*/
%>
<%@ page import="biz.vnc.zimbra.lighthistoryzimlet.MailhistoryReader" %>
<%
	String msgid=request.getParameter("messageID");
	MailhistoryReader mailhistory=new MailhistoryReader();
	out.println(mailhistory.getRecord(msgid.trim()));
%>
