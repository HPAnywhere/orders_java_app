define([
    "jquery"
], function($) {
    var initialize = {
        models: {},
        views: {}
    };
    var jqmReadyDef = $.Deferred();
    window["jqmReadyPromise"] = jqmReadyDef.promise();

    $(document).bind("pageinit", function() {
        document.addEventListener("touchmove", function(e) {
            e.preventDefault();
        }, false);
        jqmReadyDef.resolve();
    });
    return initialize;
});