define([
    "app/smartphone/initialize",
    "app/smartphone/controller/list"
], function(initialize) {
    var itemView = initialize.views.itemView = {
        tplName: "itemView",
        pageTemplate: null
    },
        listView = initialize.views.listView,
        dName = window.parent.device ? window.parent.device.model : "Desktop",
        dPlatform = window.parent.device ? window.parent.device.platform : "Desktop",
        separater = "&",
        enableSubmission = true;

    itemView.setTemplate = function(tpl) {
        this.pageTemplate = tpl;
    };

    itemView.getCurrentDate = function(d) {
        var year = d.getFullYear().toString().substring(2),
            month = d.getMonth() + 1,
            day = d.getDate();
        // return date like "dd.mm.yy"
        return (day > 9 ? day : ("0" + day)) + "." + (month > 9 ? month : ("0" + month)) + "." + year;
    };

    itemView.errorHandler = function() {
        $.mobile.loading("hide");
        enableSubmission = true;
        HPA.Message.error(HPA.I18n.localize("ajaxError"), null, this);
    };

    itemView.getOrderById = function(id) {
        var i = 0,
            len = listView.allOrders.length;
        for (; i < len; i++) {
            if (listView.allOrders[i].id == id) return listView.allOrders[i];
        }
    };

    itemView.initializeElements = function() {
        $("#reason").attr("readonly", false).val("");
        $("#btnReject").attr("disabled", false).removeClass("disableReject rejectSelected").html(HPA.I18n.localize("orders.html.reject"));
        $("#btnApprove").attr("disabled", false).removeClass("disableApprove approveSelected").html(HPA.I18n.localize("orders.html.approve"));
        $("#processStatus").html("");
    };

    itemView.backPage = function() {
        // refresh data
        listView.queryData();
        setTimeout(itemView.initializeElements, 200);
        // initialize action strip
        HPA.ActionsBar.setActions([{
            text: HPA.I18n.localize("orders.action.rules"),
            action: "moreAction",
            params: "ruleList"
        }, {
            text: HPA.I18n.localize("orders.action.restore"),
            action: "moreAction",
            params: "restore"
        }]);
        // change page
        $.mobile.changePage($("#listView"), {
            transition: "flip",
            reverse: true
        });
    };

    itemView.updateStatus = function(item, order, operation, currentDate) {
        var content = operation==="Approved" ? HPA.I18n.localize("orders.html.approved") : HPA.I18n.localize("orders.html.rejected");
        $("#date_status").html(item.createdDate + " | " + content);
        $("#processStatus").html(content + " " + HPA.I18n.localize("orders.preposition.at") + " " + (order ? order.updatedDate : currentDate) + "<br/>" + HPA.I18n.localize("orders.preposition.from") + " " + dPlatform);
        $("#reason").attr("readonly", true);
        $("#btnReject").attr("disabled", true);
        $("#btnApprove").attr("disabled", true);
        if (operation === "Approved") {
        	$("#btnReject").addClass("disableReject");
        	$("#btnApprove").addClass("approveSelected").html(HPA.I18n.localize("orders.html.approved"));
        } else {
        	$("#btnApprove").addClass("disableApprove");
        	$("#btnReject").addClass("rejectSelected").html(HPA.I18n.localize("orders.html.rejected"));
        }
    };

    itemView.handleStatusDuringOnline = function(item, status) {
        if (!enableSubmission) return;

        var dt = new Date(),
            updatedTimestamp = dt.getTime(),
            currentDate = itemView.getCurrentDate(dt);

        $.mobile.loading("show");
        enableSubmission = false;
        $.ajax({
            type: "PUT",
            url: "services/orders/" + item.id,
            data: JSON.stringify({
                reason: $("#reason").val(),
                status: status,
                updatedDate: updatedTimestamp,
                deviceInfo: dName + separater + dPlatform
            }),
            success: function(response) {
                itemView.updateStatus(item, response, response.status, currentDate);
                var order = itemView.getOrderById(item.id);
                order.status = response.status;
                order.reason = response.reason;
                order.deviceInfo = response.deviceInfo;
                order.updatedDate = response.updatedDate;
                $.mobile.loading("hide");
                enableSubmission = true;
            },
            error: function() {
                itemView.errorHandler();
            }
        });
    };

    itemView.handleStatusDuringOffline = function(item, status) {
        var dt = new Date(),
            updatedTimestamp = dt.getTime(),
            currentDate = itemView.getCurrentDate(dt),
            order = itemView.getOrderById(item.id),
            storedOrders = localStorage.getItem("offlineData") ? JSON.parse(localStorage.getItem("offlineData")) : { offlineOrders: [] };

        order.reason = $("#reason").val();
        order.status = status;
        order.deviceInfo = dName + separater + dPlatform;
        order.updatedDate = updatedTimestamp;

        storedOrders.offlineOrders.push({
            id: item.id,
            reason: order.reason,
            status: order.status,
            updatedDate: order.updatedDate,
            deviceInfo: order.deviceInfo
        });

        localStorage.setItem("offlineData", JSON.stringify(storedOrders));
        localStorage.setItem("orders", JSON.stringify(listView.allOrders));
        itemView.updateStatus(item, null, status, currentDate);
    };

    itemView.onclickApproveBtn = function(item) {
        if ($("#btnApprove").attr("disabled")) return;
        return HPA.Framework.isOffline() ? itemView.handleStatusDuringOffline(item, "Approved") : itemView.handleStatusDuringOnline(item, "Approved");
    };

    itemView.onclickRejectBtn = function(item) {
        if ($("#btnReject").attr("disabled")) return;
        return HPA.Framework.isOffline() ? itemView.handleStatusDuringOffline(item, "Rejected") : itemView.handleStatusDuringOnline(item, "Rejected");
    };

    itemView.replaceHtml = function(){
    	$("#itemViewTitle").html(HPA.I18n.localize("orders.itemView.itemViewTitle"));
    	$("#btnApprove").html(HPA.I18n.localize("orders.itemView.btnApprove"));
    	$("#btnReject").html(HPA.I18n.localize("orders.itemView.btnReject"));
    	$("#offLineItem").html(HPA.I18n.localize("orders.offline.offLine"));
    	$("#onLineItem").html(HPA.I18n.localize("orders.online.onLine"));
    };
    
    itemView.renderPage = function(item) {
        var currentOrder = itemView.getOrderById(item.id);
        $.mobile.loading("hide");
        enableSubmission = true;

        if ($("#itemView").length === 0) {
            $(document.body).append(itemView.pageTemplate);
            $("#itemViewBack").unbind("click").bind("click", itemView.backPage);
        }
        // get approve
        $("#btnApprove").unbind("click").bind("click", function() {
            itemView.onclickApproveBtn(item);
        });
        // get reject
        $("#btnReject").unbind("click").bind("click", function() {
            itemView.onclickRejectBtn(item);
        });


        $("#orderName").html(currentOrder.name);
        $("#orderAmount").html("$" + Number(currentOrder.amount).toLocaleString()).css("color", initialize.views.ruleListView.convertColorToRGB(item.color));
        $("#date_status").html(currentOrder.createdDate + " | " + currentOrder.status);

        if (currentOrder.status === "Waiting") itemView.initializeElements();
        else {
            $("#reason").val(currentOrder.reason);
            $("#processStatus").html(currentOrder.status + " " + HPA.I18n.localize("orders.preposition.at") + " " + currentOrder.updatedDate + "<br/>" + HPA.I18n.localize("orders.preposition.from") + " " + currentOrder.deviceInfo.split(separater)[1]);
        }

        window["iScrollItemViewWrapper"] = new iScroll("itemViewWrapper");

        $.mobile.changePage($("#itemView"), {
            transition: "flip"
        });
        itemView.replaceHtml();
    };
    // register back button event
    HPA.Events.on("backbutton", itemView.backPage);

    return itemView;
});