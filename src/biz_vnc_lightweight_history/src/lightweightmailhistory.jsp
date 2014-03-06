<%
/*
	http://www.vnc.biz
	Copyright 2014-TODAY, VNC - Virtual Network Consult AG
	Released under GPL Licenses.
*/
%>
<%@ page import="biz.vnc.zimbra.lighthistoryzimlet.MailhistoryReader" %>
<%
	String msgid=request.getParameter("messageID");
	MailhistoryReader mailhistory=new MailhistoryReader();
	out.println(mailhistory.getRecord(msgid.trim()));
%>
