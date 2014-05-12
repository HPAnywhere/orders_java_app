require.config({
    baseUrl: ".",
    paths: {
        jquery: "libs/jquery/jquery-1.10.2.min",
        jquerymobile: "libs/jqmobile/jquery.mobile-1.3.2.min",
        jqueryconf: "app/smartphone/conf/conf"
    }
});

require([
    "jquery",
    "jquerymobile",
    "jqueryconf",
    "libs/addons/iscroll",
    "app/smartphone/initialize",
    "app/smartphone/app"
], function($, jqm, conf, iscroll, initialize, app) {
    window["openEntryPoint"] = function(entryPoint, params, callback, scope) {
        console.log("DEMO openEntryPoint called: ", entryPoint);
        initialize.app.onReady(entryPoint, params, callback, scope);
    };
});