// see https://github.com/jrburke/r.js/blob/master/build/example.build.js
( {
    baseUrl: "./src/webapp",
        paths: {

            // Core Libraries 
      "jquery": "libs/jquery/jquery-1.10.2.min",
      "jquerymobile": "libs/jqmobile/jquery.mobile-1.3.2.min", 
      "jqueryconf": "app/smartphone/conf/conf"

        },
    
    out: "./src/webapp/app/smartphone/controller/app.min.js",
    name: "app/smartphone/require",
    optimize: "uglify"
       
} )
