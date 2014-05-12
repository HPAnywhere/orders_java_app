define([
    "app/smartphone/initialize"
], function(initialize) {
    initialize.views.welcomePage = {
        tplName: "welcomePage",
        pageTemplate: null
    };

    initialize.views.welcomePage.setTemplate = function(tpl) {
        this.pageTemplate = tpl;
    };


    initialize.views.welcomePage.queryData = function() {
        var key = "loginFlag";
        $.ajax({
            type: "GET",
            url: "services/orders/validateFirstLogin/" + key,
            success: function(data) {
                if (data.loginFlag === "Y") {
                    initialize.views.welcomePage.renderPage();
                    $("#enter").click(function() {
                        initialize.views.listView.renderPage();
                        $.mobile.changePage($("#listView"), {
                            transition: "flip",
                            reverse: true
                        });
                    });
                } else {
                    initialize.views.listView.renderPage();
                }
            }
        });
    };
    
    initialize.views.welcomePage.replaceHtml = function(){
    	$("#welcomeLine1").html(HPA.I18n.localize("orders.welcome.welcomeLine1"));
    	$("#welcomeLine2").html(HPA.I18n.localize("orders.welcome.welcomeLine2"));
    	$("#welcomeLine3").html(HPA.I18n.localize("orders.welcome.welcomeLine3"));
    	$("#enter").html(HPA.I18n.localize("orders.welcome.enter"));
    };

    initialize.views.welcomePage.renderPage = function() {
        $(document.body).append(this.pageTemplate);

        $.mobile.changePage($("#welcomePage"), {
            transition: "flip"
        });
        
        initialize.views.welcomePage.replaceHtml();
        
        HPA.Framework.setActions(true);
    };

    return initialize.views.welcomePage;
});