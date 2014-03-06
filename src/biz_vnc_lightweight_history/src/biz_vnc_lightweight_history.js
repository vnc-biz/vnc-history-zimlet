/*
	http://www.vnc.biz
	Copyright 2014-TODAY, VNC - Virtual Network Consult AG
    Released under GPL Licenses.
*/
biz_vnc_lightweight_history_HandlerObject = function() {
	this.isPrintEnabled = true;
};
biz_vnc_lightweight_history_HandlerObject.prototype = new ZmZimletBase;
biz_vnc_lightweight_history_HandlerObject.prototype.constructor = biz_vnc_lightweight_history_HandlerObject;

biz_vnc_lightweight_history_HandlerObject.prototype.init = function() {
	AjxPackage.require("vnc.json.json2");
    AjxPackage.require("vnc.json.json-minified");
	var param = [];
	var url = "/service/zimlet/biz_vnc_lightweight_history/threadhandle.jsp";
	var response=AjxRpc.invoke(param.join("&"), url, null, null, false);
};

biz_vnc_lightweight_history_HandlerObject.prototype.doDrop = function(droppedItem) {
	
	/* Animation  loading*/
	this.animation = new DwtComposite(this.getShell());
    this.animation.setSize("50", "50");
	this.animation.getHtmlElement().innerHTML =this._animationDialogView();
	this.animationDialog = new ZmDialog({view:this.animation, parent:this.getShell(),standardButtons:[DwtDialog.NO_BUTTONS]});
	this.animationDialog.popup();
	/*  End of Animation Loading  */
    if(droppedItem instanceof Array) {
		this.animationDialog.popdown();
		var msg =  appCtxt.getMsgDialog();
        msg.setMessage(this.getMessage("warning"),DwtMessageDialog.WARNING_STYLE,this.getMessage("alert"));
        msg.popup();
    }else{
		var obj = droppedItem.srcObj ? droppedItem.srcObj : droppedItem;
        if (obj.type == "CONV"){
                myMsg=obj.getFirstHotMsg();
                myMsg.load({"callback":new AjxCallback(this,this._loadCallBack,[myMsg])});
		}else if(obj.type == "MSG"){
                obj.load({"callback":new AjxCallback(this,this._loadCallBack,[obj])});
        }
    }
};

biz_vnc_lightweight_history_HandlerObject.prototype._loadCallBack=function(response){
    var mid = response.messageId;
    var subject=response.subject;
    var fromAddr=response.getAddress(AjxEmailAddress.FROM);
    var messageID=mid.substr(1,mid.length-2);
    var acc=appCtxt.getActiveAccount();
	folderTree=appCtxt.getFolderTree(acc);
    this.pView = new DwtComposite(this.getShell());
    this.pView.setSize("650", "350");
    this.pView.getHtmlElement().style.overflow = "auto";
	if(fromAddr!=null || messageID !=null){
    	this.pView.getHtmlElement().innerHTML = this._createDialogView(messageID,folderTree,subject,fromAddr.address,appCtxt.get(ZmSetting.USERNAME));
	}else{
		var msg =  appCtxt.getMsgDialog();
        msg.setMessage(this.getMessage("error_drag"),DwtMessageDialog.CRITICAL_STYLE,this.getMessage("alert"));
        msg.popup();	
		return;
	}
    this.pbDialog = new ZmDialog({title:biz_vnc_lightweight_history.mailHistory, view:this.pView, parent:this.getShell(), standardButtons:[DwtDialog.OK_BUTTON,DwtDialog.DISMISS_BUTTON]});
    this.pbDialog.getButton(DwtDialog.OK_BUTTON).setText(ZmMsg.print);
    if(fromAddr.address != appCtxt.get(ZmSetting.USERNAME)){
        this.isPrintEnabled = false;
    }
    sender=appCtxt.get(ZmSetting.USERNAME);
    this.pbDialog.setButtonListener(DwtDialog.OK_BUTTON, new AjxListener(this, this._printBtnListener,[fromAddr.address,sender,subject,messageID]));
    this.pbDialog.setButtonListener(DwtDialog.DISMISS_BUTTON, new AjxListener(this, this._okBtnListener));
	if(this.isPrintEnabled){
        this.pbDialog.getButton(DwtDialog.OK_BUTTON).setEnabled(true,true);
    }else{
        this.pbDialog.getButton(DwtDialog.OK_BUTTON).setEnabled(false,false);
    }
    this.animationDialog.popdown();
    this.pbDialog.popup();
}

biz_vnc_lightweight_history_HandlerObject.prototype._createDialogView = function(messageID,fTree,s,from,sender) {
    if(s == undefined){
        s=biz_vnc_lightweight_history.noSubject;
    }
    var html = new Array();
    var i = 0;
    html[i++]="<div style='width:100%;height:100%;overflow:scroll;'>";
    html[i++] = "<table width='98%' align='center' class='lightweighthistorygridtable'>";
    html[i++] = "<tr>";
    html[i++] = "<td colspan='4'>";
    html[i++] = "<h3 style='padding:3px 3px 3px 3px;align:center;'>"+biz_vnc_lightweight_history.mailFor+" '"+s+"'</h3>";
    html[i++] = "</td>";
    html[i++] = "</tr>";
    html[i++] = "<tr>";
    html[i++] = "<td colspan='4'>"+biz_vnc_lightweight_history.sender+" : <b>"+sender+"</b></td>";
    html[i++] = "</tr>";
    html[i++] = "<tr>";
    html[i++] = "<td colspan='4'>";
    html[i++] = "<hr width='100%'/>";
    html[i++] = "</td>";
    html[i++] = "</tr>";
	html[i++] = "<tr class='tr_title'>";
    html[i++] = "<td>"+biz_vnc_lightweight_history.dateLabel+"</td><td>"+biz_vnc_lightweight_history.receiver+"</td><td>"+biz_vnc_lightweight_history.eventLabel+"</td><td>"+biz_vnc_lightweight_history.moveLableto+"</td>";
    html[i++] = "</tr>";

    if(from!=sender){
        html[i++] = "<tr>";
        html[i++] = "<td colspan='4' align='center'><h4>"+biz_vnc_lightweight_history.unauthorized+"</h4></td>";
        html[i++] = "</tr>";
    }else{
        var jspUrl=this.getResource("lightweightmailhistory.jsp");
		var response = AjxRpc.invoke(null, jspUrl+"?messageID="+messageID, null, null, true);
		if (response.success == true) {
			mHistory = jsonParse(response.text);
            if(!mHistory.list){
                html[i++]="<tr><td colspan='4' align='center'><h4>"+biz_vnc_lightweight_history.sqlError+"</h4></td></tr>";
                this.isPrintEnabled = false;
            }else{
            	record=mHistory.list.length;
             	if(record==0){
                	html[i++]="<tr><td colspan='4' align='center'><h4>"+biz_vnc_lightweight_history.noResult+"</h4></td></tr>";
             	}else{
					for(var j=0;j<record;j++){
						if(j%2==0){
                        	html[i++]="<tr class='odd'>";
                    	}else{
                        	html[i++]="<tr class='even'>";
                    	}
                    	html[i++]="<td>"+mHistory.list[j].logtime+"</td>";
                    	html[i++]="<td>"+mHistory.list[j].to+"</td>";

						if(mHistory.list[j].event=="1"){
                        	html[i++]="<td>"+biz_vnc_lightweight_history.deliverLabel+"</td>";
                    	}
                    	if(mHistory.list[j].event=="2"){
							html[i++]="<td>"+biz_vnc_lightweight_history.moveLabel+"</td>";
                    	}
                    	if(mHistory.list[j].event=="3"){
							html[i++]="<td>"+biz_vnc_lightweight_history.deleteLabel+"</td>";
                    	}
						html[i++]="<td>"+mHistory.list[j].moveto+"</td>";
						html[i++]="</tr>";
                	}
            	}
        	}
		}else{
			html[i++]="<tr><td>"+biz_vnc_lightweight_history.sqlError+"</td></tr>";
            this.isPrintEnabled = false;
        }
    }
    html[i++]="</table>";
    html[i++]="</div>";
	return html.join("");
};

biz_vnc_lightweight_history_HandlerObject.prototype._animationDialogView=function(){
	var html = new Array();
	var i = 0;
	html[i++]="<div id='wait' align='center'><img align='bottom' src='"+this.getResource("submit_please_wait.gif")+"'/></div>";
    return html.join("");
};

biz_vnc_lightweight_history_HandlerObject.prototype._handleConvMsgs = function(conv,obj) {
    var msgs = conv.msgs.getArray();
};

biz_vnc_lightweight_history_HandlerObject.prototype._printBtnListener= function(from,sender,subject,messageID){
    jspUrl=this.getResource("lightweightmailprint.jsp?s="+subject+"&from="+from+"&mainsender="+sender + "&msgid="+messageID+"&locale=" + appCtxt.getSettings().get(ZmSetting.LOCALE_NAME));
	window.open(jspUrl, "","menubar=yes,location=no,resizable=yes,scrollbars=yes,status=yes,width=500,height=500");
}

biz_vnc_lightweight_history_HandlerObject.prototype._okBtnListener= function(){
    this.pbDialog.popdown();
}


/* Right Click Menu */


biz_vnc_lightweight_history_HandlerObject.prototype.onParticipantActionMenuInitialized = function(controller, menu) {
    this.onActionMenuInitialized(controller, menu);
};

biz_vnc_lightweight_history_HandlerObject.prototype.onActionMenuInitialized = function(controller, menu) {
    this.addMenuButton(controller, menu);
};

biz_vnc_lightweight_history_HandlerObject.HIST_ID="view_history";

biz_vnc_lightweight_history_HandlerObject.prototype.addMenuButton = function(controller, menu) {
    var ID = biz_vnc_lightweight_history_HandlerObject.HIST_ID;
    var btnName=this.getMessage("history_name");
    var btnTooltip=this.getMessage("view_history_tooltip");
    if (!menu.getMenuItem(ID)) {
        var op = {
            id : ID,
            text : btnName,
            tooltip: btnTooltip,
			image: "history_zimlet"
        };
        var opDesc = ZmOperation.defineOperation(null, op);
        menu.addOp(ID, 1000);// add the button at the bottom
        menu.addSelectionListener(ID, new AjxListener(this,this._menuButtonListener, controller));
    }
};

biz_vnc_lightweight_history_HandlerObject.prototype._menuButtonListener = function(controller) {
	/* Animation  loading*/
    this.animation = new DwtComposite(this.getShell());
    this.animation.setSize("50", "50");
    this.animation.getHtmlElement().innerHTML =this._animationDialogView();
    this.animationDialog = new ZmDialog({view:this.animation, parent:this.getShell(),standardButtons:[DwtDialog.NO_BUTTONS]});
    this.animationDialog.popup();
    /*  End of Animation Loading  */
	var con=appCtxt.getCurrentController();

	if(ZmCsfeCommand.clientVersion.substring(0,1) == "7") {
    		droppedItem = controller.getCurrentView().getDnDSelection();
	} else {
    		 droppedItem = controller.getListView().getDnDSelection();
	}
if (droppedItem instanceof Array){
        for(var i =0; i < droppedItem.length; i++){
            var obj = droppedItem[i].srcObj ?  droppedItem[i].srcObj :  droppedItem[i];
            if (obj.type == "CONV" ) {
				myMsg=obj.getFirstHotMsg();
                myMsg.load({"callback":new AjxCallback(this,this._loadCallBack,[myMsg])});
            } else if (obj.type == "MSG") {
				obj.load({"callback":new AjxCallback(this,this._loadCallBack,[obj])});
            }
        }
    } else {
		var obj = droppedItem.srcObj ? droppedItem.srcObj : droppedItem;
        if (obj.type == "CONV"){
                myMsg=obj.getFirstHotMsg();
                myMsg.load({"callback":new AjxCallback(this,this._loadCallBack,[myMsg])});
        }else if(obj.type == "MSG"){
                obj.load({"callback":new AjxCallback(this,this._loadCallBack,[obj])});
        }
    }
};
              
