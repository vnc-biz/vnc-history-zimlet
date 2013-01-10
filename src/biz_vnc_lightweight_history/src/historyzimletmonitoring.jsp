<%
/*
    http://www.vnc.biz
    Copyright 2012, VNC - Virtual Network Consult GmbH
    Released under GPL Licenses.
*/
%>
<%@ page import="biz.vnc.zimbra.util.JSPUtil" %>
<%@ page import="biz.vnc.zimbra.util.ZStats" %>
<%@ page import="biz.vnc.zimbra.util.ZLog" %>
<%@ page import="com.zimbra.cs.account.Account" %>
<%@ page import="java.util.Properties"%>
<%
try{
    JSPUtil.nocache(response);
    ZStats.product_feedback(
        "biz_vnc_lightweight_history",
        JSPUtil.getZimletTranslationProperties(application,"biz_vnc_lightweight_history").getProperty("ZIMLET_VERSION"),
        JSPUtil.getCurrentAccount(request).getName()
    );
    out.println("success");
} catch (Exception e) {
    out.println("error");
    ZLog.err("biz_vnc_lightweight_history", "historyzimletmonitoring fail ", e);
}
%>
