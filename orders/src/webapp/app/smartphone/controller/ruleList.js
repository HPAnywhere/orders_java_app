define([
    "app/smartphone/initialize"
], function(initialize) {
    var ruleListView = initialize.views.ruleListView = {
        tplName: "ruleListView",
        pageTemplate: null
    };
    ruleListView.setTemplate = function(tpl) {
        this.pageTemplate = tpl;
    };

    ruleListView.allRules = '';

    var $overlay = $('<div id="miasLoadingMask" class="loadingMask" style="height:' + $(document).height() + 'px;"></div>');

    //convert the color from rgb
    initialize.views.ruleListView.convertColorFromRGB = function(rgbColor) {
        if (rgbColor === "") return "";
        if ("rgb(255, 165, 0)".toString() == rgbColor) {
            return "orange";
        } else if ("rgb(255, 222, 0)".toString() == rgbColor) {
            return "yellow";
        } else {
            return "red";
        }
    };

    //convert the color to rgb
    initialize.views.ruleListView.convertColorToRGB = function(color) {
        if ("yellow" == color) {
            return "rgb(255, 222, 0)";
        } else {
            return color;
        }
    };

    //handle the color picker
    ruleListView.selectColor = function(colorPicker) {
        $.each($(colorPicker).children(), function(i, item) {
            $(item).unbind("click").bind("click", function() {
                if ($(item).hasClass("color-selected")) {
                    $(item).removeClass("color-selected");
                    $($(item).children()[0]).removeClass("flag");
                    $(colorPicker).val("");
                } else {
                    $(colorPicker).find(".flag").removeClass("flag");
                    $(colorPicker).find(".color-selected").removeClass("color-selected");
                    $(item).addClass("color-selected");
                    $($(item).children()[0]).addClass("flag");
                    $(colorPicker).val($(item).css('background-color'));
                }
            });
        });
    };

    //check the the color exist or not
    ruleListView.colorIsExist = function(selectedColor) {
        var isExist = false;
        if (ruleListView.allRules.length > 0) {
            for (var i = 0; i < ruleListView.allRules.length; i++) {
                if (selectedColor == ruleListView.allRules[i].color) {
                    isExist = true;
                }
            }
        }
        return isExist;
    };

    //reset the color in the order list once the rule list got changed
    ruleListView.resetColor = function(orderList, ruleList) {
        var k = 0;
        for (var j = 0; j < orderList.length; j++) {
            while (k < ruleList.length) {
                var lowerValue = ruleList[k].lowerLimitValue;
                if (Number(orderList[j].amount) > Number(lowerValue)) {
                    orderList[j].color = ruleList[k].color;
                    break;
                }
                k++;
            }
            k = 0;
        }
    };

    ruleListView.getIndexById = function(id) {
        for (var i = 0; i < ruleListView.allRules.length; i++) {
            if (id === ruleListView.allRules[i].id) {
                return i;
            }
        }
    };

    ruleListView.onDeleteRuleSuccess = function(msg, node, ruleItem) {
        //synchronize all the rules
    	var index = ruleListView.getIndexById(ruleItem.id);
    	if(index === -1 || index === "undefined") return;
    	ruleListView.allRules.splice(index, 1);
        for (var m = 0; m < initialize.views.listView.allOrders.length; m++) {
            initialize.views.listView.allOrders[m].color = "gray";
        }

        //synchronize all the rules orders
        if (initialize.views.listView.allOrders.length > 0) {
            if (ruleListView.allRules.length > 0) {
                ruleListView.resetColor(initialize.views.listView.allOrders, ruleListView.allRules);
            }
        }
        localStorage.setItem("orders", JSON.stringify(initialize.views.listView.allOrders));
        localStorage.setItem("rules", JSON.stringify(ruleListView.allRules));
        ruleListView.renderRuleEditPage();
        $.mobile.loading("hide");
        $overlay.remove();
        HPA.Framework.setActions(false);
    };

    //delete the rule
    ruleListView.deleteRule = function(template, ruleItem) {
    	var node = $('<div>', {
            'class': "delete-rule"
        }).prependTo(template).one("click", function() {
            if (HPA.Framework.isOffline()) return HPA.Message.error(HPA.I18n.localize('offlineDelRule'), null, this);
            HPA.Framework.setActions(true);
            $overlay.appendTo($('body'));
            $.mobile.loading("show");
            $.ajax({
                type: "DELETE",
                url: "services/orders/rules/" + ruleItem.id,
                success: function(msg) {
                    ruleListView.onDeleteRuleSuccess(msg, node, ruleItem);
                },
                error: function() {
                    ruleListView.errorHandler();
                }
            });
        });
    };

    ruleListView.validateUpdate = function(ruleItem, ruleAmount, ruleColor) {
        var r = /^([1-9]\d*|(0|[1-9]\d*)\d*[1-9])$/;
        if ("" === ruleColor) {
            HPA.Message.error(HPA.I18n.localize("selColor"), null, this);
            $.mobile.loading("hide");
            $overlay.remove();
            return false;
        }
        if (ruleAmount.charAt(0) == "0" || !r.test(ruleAmount)) {
            HPA.Message.error(HPA.I18n.localize("validateAmount"), null, this);
            $.mobile.loading("hide");
            $overlay.remove();
            return false;
        }
        if (ruleColor != ruleItem.color) {
            if (ruleListView.colorIsExist(ruleColor)) {
                HPA.Message.error(HPA.I18n.localize("hasColor"), null, this);
                $.mobile.loading("hide");
                $overlay.remove();
                return false;
            }
        }
        //if the amount and the color didn't be changed, don't request the server.
        if (ruleAmount == ruleItem.lowerLimitValue && ruleColor == ruleItem.color) {
            $.each($("#edit_rule_colorPicker").children(), function(i, item1) {
                $(item1).removeClass("color-selected");
                $($(item1).children()[0]).removeClass("flag");
            });
            $.mobile.loading("hide");
            $("#editRules").hide();
            $("#addRulesMask").hide();
            $overlay.remove();
            HPA.Framework.setActions(false);
            return false;
        }
        return true;
    };

    ruleListView.onUpdateRuleSuccess = function(msg, ruleItem) {
        if (msg.id !== null) {
            ruleListView.allRules.splice(ruleListView.getIndexById(ruleItem.id), 1);
            ruleListView.allRules.push(msg);
            ruleListView.allRules.sort(
                function(a, b) {
                    return Number(a.lowerLimitValue) > Number(b.lowerLimitValue) ? -1 : (Number(a.lowerLimitValue) < Number(b.lowerLimitValue) ? 1 : 0);
                }
            );
            for (var m = 0; m < initialize.views.listView.allOrders.length; m++) {
                initialize.views.listView.allOrders[m].color = "gray";
            }
            ruleListView.resetColor(initialize.views.listView.allOrders, ruleListView.allRules);
            localStorage.setItem("orders", JSON.stringify(initialize.views.listView.allOrders));
            localStorage.setItem("rules", JSON.stringify(ruleListView.allRules));
        } else {
            HPA.Message.error(HPA.I18n.localize("updateError"), null, this);
        }
        $.mobile.loading("hide");
        $("#editRules").hide();
        $("#addRulesMask").hide();
        ruleListView.renderRuleEditPage();
        $.each($("#edit_rule_colorPicker").children(), function(i, item1) {
            $(item1).removeClass("color-selected");
            $($(item1).children()[0]).removeClass("flag");
            $("#edit_rule_colorPicker").val('');
        });
        $overlay.remove();
        HPA.Framework.setActions(false);
    };

    ruleListView.onclickUpdateBtn = function(ruleItem) {
        if (HPA.Framework.isOffline()) return HPA.Message.error(HPA.I18n.localize("offlineEditRule"), null, this);

        $overlay.appendTo($('body'));
        $.mobile.loading("show");
        var ruleAmount = $("#editAmount").val().toString(),
            ruleColor = ruleListView.convertColorFromRGB($("#edit_rule_colorPicker").val());

        if (!ruleListView.validateUpdate(ruleItem, ruleAmount, ruleColor)) return;

        if (ruleAmount != ruleItem.lowerLimitValue || ruleColor != ruleItem.color) {
            $.ajax({
                type: "PUT",
                url: "services/orders/rules/" + ruleItem.id,
                data: JSON.stringify({
                    id: ruleColor,
                    name: "Higher than $" + initialize.views.listView.formatAmount(ruleAmount),
                    lowerLimitValue: ruleAmount,
                    color: ruleColor
                }),
                success: function(msg) {
                    ruleListView.onUpdateRuleSuccess(msg, ruleItem);
                },
                error: function() {
                    ruleListView.errorHandler();
                }
            });
        }
    };

    //update a rule, changed the amount or the color for the rule
    ruleListView.updateRule = function(template, ruleItem) {
        $('<div>', {
            'class': "edit-rule"
        }).appendTo(template).click(function() {
            if (HPA.Framework.isOffline()) return HPA.Message.error(HPA.I18n.localize("offlineEditRule"), null, this);

            HPA.Framework.setActions(true);
            $("#editAmount").val(ruleItem.lowerLimitValue);
            ruleListView.selectColor("#edit_rule_colorPicker");
            $.each($("#edit_rule_colorPicker").children(), function(i, itemcolor) {
                if (ruleListView.convertColorFromRGB($(itemcolor).css('background-color').toString()) == ruleItem.color) {
                    $(itemcolor).addClass("color-selected");
                    $($(itemcolor).children()[0]).addClass("flag");
                    $("#edit_rule_colorPicker").val($(itemcolor).css('background-color'));
                }
            });
            $("#addRulesMask").show();
            $("#editRules").show();

            //handle the update button
            $("#btnUpdateRule").unbind("click").bind("click", function() {
                ruleListView.onclickUpdateBtn(ruleItem);
            });

            //handle the cancel button in edit page
            ruleListView.handleCancelButton();
        });
    };

    ruleListView.handleCancelButton = function() {
        $("#btnEditCancelRule").click(function() {
            $.each($("#edit_rule_colorPicker").children(), function(i, item1) {
                $(item1).removeClass("color-selected");
                $($(item1).children()[0]).removeClass("flag");
            });
            $("#editRules").hide();
            $("#addRulesMask").hide();
            HPA.Framework.setActions(false);
        });
    };

    ruleListView.errorHandler = function() {
        HPA.Message.error(HPA.I18n.localize('ajaxError'), null, this);
        $.mobile.loading("hide");
        $overlay.remove();
        HPA.Framework.setActions(false);
    };

    //render the edit page
    ruleListView.renderRuleEditPage = function() {
        var ruleEditListNode = $("#ruleListViewScroller").find("#ruleEditList");
        ruleEditListNode.empty();
        if (ruleListView.allRules.length === 0) {
        	$("#ruleList").empty();
        	$("#ruleEditList").empty();
            $("#noRules").show();
        } else {
            $.each(ruleListView.allRules, function(i, item) {
                var tmp = $("<div>", {
                    "class": "edit-rule-container",
                    "html": "<div class='left-edit-rule'>" + item.name + "</div>"
                    	+ "<div class='right-edit-rule' style='color:"
                    	+ ruleListView.convertColorToRGB(item.color) + "'>"
                    	+ item.color.toUpperCase() + "</div>"
                }).appendTo(ruleEditListNode);
                //delete a rule
                ruleListView.deleteRule(tmp, item);
                //update a rule
                ruleListView.updateRule(tmp, item);
            });
        }
        $("#ruleList").hide();
        $("#ruleEditList").show();
    };
    
    //generate the dom element
    ruleListView.generateRuleListElement = function(list) {
    	var ruleListNode = $("#ruleListViewScroller").find("#ruleList");
        ruleListNode.empty();
        $.each(list, function(j, item1) {
            var tmp = $('<div></div>');
            tmp.addClass("rule-container").appendTo(ruleListNode);
            $("<div class=\"left-rule\">" + item1.name +
                "</div>" +
                "<div class=\"right-rule\" style=\"color: " + ruleListView.convertColorToRGB(item1.color) + ";\">" + item1.color.toUpperCase() +
                "</div>").appendTo(tmp);
        });
    };

    ruleListView.queryData = function() {
        var deferred = $.Deferred();

        if (HPA.Framework.isOffline()) {
            ruleListView.allRules = JSON.parse(localStorage.getItem("rules"));
            if (ruleListView.allRules.length === 0) {
                $("#noRules").show();
            } else {
                $("#noRules").hide();
                ruleListView.generateRuleListElement(ruleListView.allRules);
            }
        } else {
            if (ruleListView.allRules.length === 0) {
                $.get("services/orders/rules",
                    function(data) {
                        ruleListView.allRules = data;
                        localStorage.setItem("rules", JSON.stringify(ruleListView.allRules));
                        if (data.length === 0) {
                            $("#noRules").show();
                        } else {
                            $("#noRules").hide();
                        }
                        ruleListView.generateRuleListElement(data);
                    });
            } else {
                $("#noRules").hide();
                ruleListView.generateRuleListElement(ruleListView.allRules);
            }
        }

        //go to the orders list page
        // process the back button for the andriod device
        $("#backToOrders").unbind("click").bind("click", backToOrders);

        function backToOrders() {
            initialize.views.listView.queryData();
            $.mobile.changePage($("#listView"), {
                transition: "flip",
                reverse: true
            });
            HPA.ActionsBar.setActions([{
                text: HPA.I18n.localize("orders.action.rules"),
                action: "moreAction",
                params: "ruleList"
            }, {
                text: HPA.I18n.localize("orders.action.restore"),
                action: "moreAction",
                params: "restore"
            }]);
            HPA.ActionsBar.setVisible(true);
        }

        deferred.resolve();
        return deferred.promise();
    };

    ruleListView.validateSave = function(ruleAmount, ruleColor) {
        var r = /^([1-9]\d*|(0|[1-9]\d*)\d*[1-9])$/;
        if (ruleAmount.charAt(0) == "0" || !r.test(ruleAmount)) {
            HPA.Message.error(HPA.I18n.localize("validateAmount"), null, this);
            $.mobile.loading("hide");
            $overlay.remove();
            return false;
        } else if (ruleColor === "") {
            HPA.Message.error(HPA.I18n.localize("selColor"), null, this);
            $.mobile.loading("hide");
            $overlay.remove();
            return false;
        } else if (ruleListView.colorIsExist(ruleColor)) {
            HPA.Message.error(HPA.I18n.localize("hasColor"), null, this);
            $.mobile.loading("hide");
            $overlay.remove();
            return false;
        }
        return true;
    };

    ruleListView.onCreateRuleSuccess = function(msg, ruleListNode) {
        ruleListNode.empty();
        $("#amount").val('');
        $("#rule_colorPicker").find(".flag").removeClass("flag");
        $("#rule_colorPicker").find(".color-selected").removeClass("color-selected");
        $("#rule_colorPicker").val('');
        document.body.style.overflow = "";
        $("#addRules").hide();
        $("#addRulesMask").hide();
        $("#editList").hide();
        $("#noRules").hide();
        ruleListView.allRules.push(msg);
        ruleListView.allRules.sort(
            function(a, b) {
                return Number(a.lowerLimitValue) > Number(b.lowerLimitValue) ? -1 : (Number(a.lowerLimitValue) < Number(b.lowerLimitValue) ? 1 : 0);
            }
        );
        ruleListView.resetColor(initialize.views.listView.allOrders, ruleListView.allRules);
        localStorage.setItem("orders", JSON.stringify(initialize.views.listView.allOrders));
        localStorage.setItem("rules", JSON.stringify(ruleListView.allRules));
        $.each(ruleListView.allRules, function(i, item) {
            console.log(item);
            $("<div>", {
                "class": "rule-container",
                "html": "<div class='left-rule'>" + item.name + "</div>" + "<div class='right-rule' style='color:" + ruleListView.convertColorToRGB(item.color) + "'>" + item.color.toUpperCase() + "</div>"
            }).appendTo(ruleListNode);
        });
        $.mobile.loading("hide");
        $overlay.remove();
        HPA.Framework.setActions(false);
    };

    ruleListView.onclickSaveBtn = function() {
        if (HPA.Framework.isOffline()) return HPA.Message.error(HPA.I18n.localize("saveRule"), null, this);

        HPA.Framework.setActions(true);
        $overlay.appendTo($('body'));
        $.mobile.loading("show");
        var ruleListNode = $("#ruleListViewScroller").find("#ruleList"),
            ruleColor = ruleListView.convertColorFromRGB($("#rule_colorPicker").val()),
            ruleAmount = $("#amount").val().toString();

        if (!ruleListView.validateSave(ruleAmount, ruleColor)) return;

        $.ajax({
            type: "POST",
            url: "services/orders/rules",
            data: JSON.stringify({
                id: ruleColor,
                name: "Higher than $" + initialize.views.listView.formatAmount(ruleAmount),
                lowerLimitValue: ruleAmount,
                color: ruleColor
            }),
            success: function(msg) {
                ruleListView.onCreateRuleSuccess(msg, ruleListNode);
            },
            error: function() {
                ruleListView.errorHandler();
            }
        });
    };

    ruleListView.onclickCancelBtn = function() {
        $("#amount").val('');
        $("#rule_colorPicker").find(".flag").removeClass("flag");
        $("#rule_colorPicker").find(".color-selected").removeClass("color-selected");
        $("#rule_colorPicker").val('');
        $("#addRules").hide();
        $("#addRulesMask").hide();
        HPA.Framework.setActions(false);
    };

    ruleListView.bindEvent = function() {
        //handle the color picker
        ruleListView.selectColor("#rule_colorPicker");
        //handle the cancel button
        $("#btnCancelRule").unbind("click").bind("click", ruleListView.onclickCancelBtn);
        //handle save button
        $("#btnSaveRule").unbind("click").bind("click", ruleListView.onclickSaveBtn);
    };

    ruleListView.replaceHtml = function(){
    	$("#ruleTitle").html(HPA.I18n.localize("orders.ruleListView.ruleTitle"));
    	$("#noRules").html(HPA.I18n.localize("orders.ruleListView.noRules"));
    	$("#newRule").html(HPA.I18n.localize("orders.ruleListView.newRule"));
    	$("#lblAmount").html(HPA.I18n.localize("orders.ruleListView.lblAmount"));
    	$("#lblColor").html(HPA.I18n.localize("orders.ruleListView.lblColor"));
    	$("#btnCancelRule").html(HPA.I18n.localize("orders.ruleListView.btnCancelRule"));
    	$("#btnSaveRule").html(HPA.I18n.localize("orders.ruleListView.btnSaveRule"));
    	$("#editRuleTitle").html(HPA.I18n.localize("orders.ruleListView.editRuleTitle"));
    	$("#btnUpdateRule").html(HPA.I18n.localize("orders.ruleListView.btnUpdateRule"));
    	$("#lblEditAmount").html(HPA.I18n.localize("orders.ruleListView.lblAmount"));
    	$("#lblEditColor").html(HPA.I18n.localize("orders.ruleListView.lblColor"));
    	$("#btnEditCancelRule").html(HPA.I18n.localize("orders.ruleListView.btnCancelRule"));
    	$("#offLineRule").html(HPA.I18n.localize("orders.offline.offLine"));
    	$("#onLineRule").html(HPA.I18n.localize("orders.online.onLine"));
    };

    ruleListView.renderPage = function() {
        $(document.body).append(this.pageTemplate);
        $("#ruleListViewWrapper").css("height", (window.innerHeight - 60) + "px");
        $.when(
            this.queryData()
        ).then(
            function() {
                $.mobile.changePage($("#ruleListView"), {
                    transition: "flip"
                });

                window["iScrollListViewWrapper"] = new iScroll("ruleListViewWrapper");
            }
        );
        ruleListView.replaceHtml();
        HPA.Framework.setActions(false);
        HPA.ActionsBar.setActions([{
            icon: 'Add',
            action: "barAction",
            params: "addRule"
        }, {
            icon: "Edit",
            action: "barAction",
            params: "editRules"
        }]);
        HPA.ActionsBar.setVisible(!HPA.Framework.isOffline());
        window.ordersAppCurrentPage = "ruleListPage";
    };

    //back button in Android device
    HPA.Events.on('backbutton', function backToOrders() {
        initialize.views.listView.queryData();
        $.mobile.changePage($("#listView"), {
            transition: "flip",
            reverse: true
        });
        HPA.ActionsBar.setActions([{
            text: HPA.I18n.localize("orders.action.rules"),
            action: "moreAction",
            params: "ruleList"
        }, {
            text: HPA.I18n.localize("orders.action.restore"),
            action: "moreAction",
            params: "restore"
        }]);
        HPA.ActionsBar.setVisible(true);
    });

    return ruleListView;
});