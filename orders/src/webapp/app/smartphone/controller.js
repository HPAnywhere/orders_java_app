define([
    "jquery",
    "app/smartphone/initialize",
    "app/smartphone/controller/welcomePage",
    "app/smartphone/controller/list",
    "app/smartphone/controller/item",
    "app/smartphone/controller/ruleList"
], function($, initialize, welcomePage, listView, itemView, ruleListView) {
    function loadPage(path, callback) {
        var deferred = $.Deferred();
        if (localStorage.getItem(path.replace(/(\/)|(\.)/gi, "_")) === null) {
            $.get(path, function(data) {
                localStorage.setItem(path.replace(/(\/)|(\.)/gi, "_"), data);
                if (callback) callback(data);
                deferred.resolve();
            });
        } else {
            if (callback) callback(localStorage.getItem(path.replace(/(\/)|(\.)/gi, "_")));
            deferred.resolve();
        }
        return deferred.promise();
    }

    var isSuccess = false;

    $.ajaxSetup({
        contentType: 'application/json',
        headers: {
            'X-CSRF-HPMEAP': 'FROM-MyApprovals'
        },
        timeout: 60000,
        error: function() {
            HPA.Message.error(HPA.I18n.localize('ajaxError'), null, this);
        }
    });

    function syncData(args) {
        sendRequest(args);
        if (!isSuccess) {
            setTimeout(function() {
                syncData(args);
            }, 500);
        }
    }

    function sendRequest(param) {
        if (param.length > 0) {
            obj = param[0];
            $.ajax({
                type: "PUT",
                async: false,
                url: "services/orders/" + obj.id,
                data: JSON.stringify({
                    reason: obj.reason,
                    status: obj.status,
                    updatedDate: obj.updatedDate,
                    deviceInfo: obj.deviceInfo
                }),
                success: function(msg) {
                    var order = null;
                    order = itemView.getOrderById(obj.id);
                    order.status = msg.status;
                    order.reason = msg.reason;
                    order.deviceInfo = msg.deviceInfo;
                    order.updatedDate = msg.updatedDate;
                    localStorage.setItem("orders", JSON.stringify(listView.allOrders));
                    param.shift();
                    isSuccess = true;
                    setTimeout(function() {
                        sendRequest(param);
                    }, 100);
                }
            });
        } else {
            localStorage.removeItem("offlineData");
            $(".offline").hide();
            $(".offlineOnlineMask").hide();
            $(".online-line1").hide();
            isSuccess = false;
            return;
        }
    }

    initialize.app = {
        onReady: function(entryPoint, params, callback, scope) {
            switch (entryPoint) {
                case 'OpenEntryPoint':
                    if (!HPA.Framework.isOffline()) {
                        welcomePage.queryData();
                    } else {
                        listView.renderPage();
                    }
                    break;
                case 'barAction':
                    if (params.params == "addRule") {
                        $("#btnCancelRule").click();
                        if (!HPA.Framework.isOffline()) {
                            setTimeout(function() {
                                $("#addRulesMask").show();
                                $("#addRules").show();
                                ruleListView.bindEvent();
                            }, 400);
                            HPA.Framework.setActions(true);
                        }
                    } else if (params.params == "editRules") {
                        if (!HPA.Framework.isOffline()) {
                            HPA.Framework.setActions(false);
                            HPA.ActionsBar.setActions([{
                                text: HPA.I18n.localize("orders.action.done"),
                                action: "barAction",
                                params: "done"
                            }]);
                            ruleListView.renderRuleEditPage();
                        }
                    } else if (params.params == "done") {
                        $("#ruleEditList").hide();
                        $("#ruleList").show();
                        ruleListView.renderPage();
                    }
                    break;
                case 'moreAction':
                    if (params.params == "ruleList") {
                        $("#ordersMask").show();
                        $("#ruleEditList").hide();
                        $("#ruleList").show();
                        $("#ordersMask").hide();
                        ruleListView.renderPage();
                    } else if (params.params == "restore") {
                        listView.restoreOrder();
                    }
                    break;
            }
            return false;
        },
        init: function() {
            $.when(
                loadPage("app/smartphone/views/welcomePage.html", $.proxy(welcomePage.setTemplate, welcomePage)),
                loadPage("app/smartphone/views/listView.html", $.proxy(listView.setTemplate, listView)),
                loadPage("app/smartphone/views/itemView.html", $.proxy(itemView.setTemplate, itemView)),
                loadPage("app/smartphone/views/ruleListView.html", $.proxy(ruleListView.setTemplate, ruleListView))
            ).then(
                function() {
                    setTimeout(function() {
                        if (window == top) {
                            console.log("*** Debug mode ***");
                            initialize.views.listView.renderPage();
                            return false;
                        }
                        HPA.Framework.setReady(true);
                        //offline support
                        HPA.Events.on('offline', offlineEventListener);

                        function offlineEventListener(inEvent) {
                            $(".offlineOnlineMask").show();
                            if (inEvent.isOffline) {
                            	if("ruleListPage"== window.ordersAppCurrentPage){
                            		HPA.ActionsBar.setVisible(false);
                            	}                            	
                                localStorage.removeItem("orders");
                                localStorage.setItem("orders", JSON.stringify(listView.allOrders));
                                localStorage.setItem("rules", JSON.stringify(ruleListView.allRules));
                                $(".offline").show();
                                $(".online-line1").hide();
                                $(".offline-line1").show();
                                setTimeout(function() {
                                    $(".offline").hide();
                                    $(".offlineOnlineMask").hide();
                                    $(".offline-line1").hide();
                                }, 3000);
                            } else {
                            	if("ruleListPage"== window.ordersAppCurrentPage){
                            		HPA.ActionsBar.setVisible(true);
                            	}
                                $(".offlineOnlineMask").show();
                                $(".offline").show();
                                $(".online-line1").show();
                                var offData = JSON.parse(localStorage.getItem("offlineData"));
                                if (offData !== null && offData.offlineOrders.length > 0) {
                                    var offOrders = offData.offlineOrders;
                                    syncData(offOrders);
                                }
                                setTimeout(function() {
                                    $(".offline").hide();
                                    $(".offlineOnlineMask").hide();
                                    $(".online-line1").hide();
                                }, 3000);
                            }
                        }
                    }, 400);
                }
            );
        }
    };

    return initialize.app;
});