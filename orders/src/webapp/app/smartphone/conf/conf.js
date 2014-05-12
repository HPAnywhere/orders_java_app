define([
	"jquery"
], function($) {
	$(document).bind("mobileinit", function() {
		$.mobile.allowCrossDomainPages = true;
		$.mobile.autoInitializePage = true;
		$.mobile.allowSamePageTransition = true;
		$.support.cors = true;
	});
});