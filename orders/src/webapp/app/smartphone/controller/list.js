define([
    "app/smartphone/initialize"
], function(initialize) {
    var listView = initialize.views.listView = {
        tplName: "listView",
        pageTemplate: null,
        allOrders: "",
        restoreFlag: true
    };
    listView.setTemplate = function(tpl) {
        this.pageTemplate = tpl;
    };

    //format the amount,i.e. 1234 format to 1,234
    listView.formatAmount = function(str) {
        var x = str.toString().split('.'),
            x1 = x[0],
            x2 = x.length > 1 ? '.' + x[1] : '',
            rgx = /(\d+)(\d{3})/;
        while (rgx.test(x1)) {
            x1 = x1.replace(rgx, '$1' + ',' + '$2');
        }
        return x1 + x2;
    };


    //generate the list page
    listView.drawList = function(node, list) {
        $.each(list, function(i, item) {
            $("<div>", {
                "class": "item-container",
                "html": "<div class='left-item'>" + "<div class='item-name'>" + item.name + "</div>" + "<div class='item-info'>" + item.createdDate + " | " + item.status + "</div>" + "</div>" + "<div class='right-item' style='color: " + initialize.views.ruleListView.convertColorToRGB(item.color) + ";'>$" + listView.formatAmount(item.amount) + "</div>"
            }).appendTo(node).click(function() {
                initialize.views.itemView.renderPage(item);
                if (item.status != "Waiting") {
                    $('#reason').attr('readonly', true);
                    $('#btnReject').attr('disabled', true).addClass("disableReject");
                    $('#btnApprove').attr('disabled', true).addClass("disableApprove");
                    switch (item.status) {
                        case "Approved":
                            return $("#btnApprove").addClass("approveSelected").html(HPA.I18n.localize("orders.html.approved"));
                        case "Rejected":
                            return $('#btnReject').addClass("rejectSelected").html(HPA.I18n.localize("orders.html.rejected"));
                    }
                }
            });
        });
    };
    
    listView.queryData = function() {
        var deferred = $.Deferred(),
            node = $("#listViewScroller");

        function handleOnline() {
            if (listView.allOrders.length === 0) {
                $.get("services/orders/rules", function(data) {
                    initialize.views.ruleListView.allRules = data;
                    localStorage.setItem("rules", JSON.stringify(data));
                });
                $.get("services/orders", function(data) {
                    listView.allOrders = data;
                    localStorage.setItem("orders", JSON.stringify(listView.allOrders));
                    listView.drawList(node, listView.allOrders);
                });
            } else listView.drawList(node, listView.allOrders);
        }

        function handleOffline() {
            listView.allOrders = JSON.parse(localStorage.getItem("orders"));
            initialize.views.ruleListView.allRules = JSON.parse(localStorage.getItem("rules"));
            listView.drawList(node, listView.allOrders);
        }

        node.empty();
        if (HPA.Framework.isOffline() === true) {
            handleOffline();
        } else {
            handleOnline();
        }

        deferred.resolve();
        return deferred.promise();
    };

    listView.replaceHtml = function(){
    	$("#listTitle").html(HPA.I18n.localize("orders.listView.listTitle"));
    	$("#offLine").html(HPA.I18n.localize("orders.offline.offLine"));
    	$("#onLine").html(HPA.I18n.localize("orders.online.onLine"));
    };
    
    listView.renderPage = function() {
        $(document.body).append(this.pageTemplate);

        $.when(this.queryData()).then(function() {
            $.mobile.changePage($("#listView"), {
                transition: "flip"
            });

            window["iScrollListViewWrapper"] = new iScroll("listViewWrapper");
        });
        listView.replaceHtml();
        HPA.Framework.setActions(false);
        HPA.ActionsBar.setActions([{
            text: HPA.I18n.localize("orders.action.rules"),
            action: "moreAction",
            params: "ruleList"
        }, {
            text: HPA.I18n.localize("orders.action.restore"),
            action: "moreAction",
            params: "restore"
        }]);
    };

    //reset all the data to the default value
    listView.restoreOrder = function() {
        if (HPA.Framework.isOffline()) return HPA.Message.error(HPA.I18n.localize("ajaxError"), null, this);
        if (!listView.restoreFlag) return;

        $("#ordersMask").show();
        $.mobile.loading("show");
        listView.restoreFlag = false;
        $.ajax({
            type: "PUT",
            url: "services/orders/restore",
            success: function(data) {
                $("#ordersMask").hide();
                $.mobile.loading("hide");
                listView.restoreFlag = true;
                if (data.restoreSuccess == "Y") {
                    initialize.views.ruleListView.allRules = "";
                    listView.allOrders = "";
                    listView.queryData();
                    localStorage.removeItem("rules");
                    $.mobile.changePage($("#listView"), {
                        transition: "flip",
                        reverse: true
                    });
                }
            },
            error: function() {
                $("#ordersMask").hide();
                $.mobile.loading("hide");
                listView.restoreFlag = true;
                HPA.Message.error(HPA.I18n.localize("ajaxError"), null, this);
            }
        });
    };

    return listView;
});