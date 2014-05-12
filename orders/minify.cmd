echo off
REM ***************************************  HPA Minify Process *********************************************

REM Minification (also minimisation or minimization), in computer programming languages and especially JavaScript, 
REM is the process of removing all unnecessary characters from source code, without changing its functionality. 
REM These unnecessary characters usually include white space characters, new line characters, comments, and sometimes block delimiters, 
REM which are used to add readability to the code but are not required for it to execute.

REM There are a few ways to minify your project, especially if it depends on the framework you are working with (Sencha, Enyo, Angular, etc'.).

REM 
REM *************** Custom minify Process ****************************
REM one can run the following command: "mvn clean install �D minify"
REM it will exec the following file "minify.bat" before packing the war file for production
REM this way, user can manage his framework minify process using external tools
REM ******************************************************************



REM http://www.sencha.com/products/sdk-tools
REM http://enyojs.com/
REM http://angularjs.org/
REM http://requirejs.org/

REM ********************************************************************

REM 					Write here your project minify process


echo.
echo -------------------------------------------------------
echo  HPA - Minify Process
echo -------------------------------------------------------

IF EXIST %BTOA_HOME%/node.exe (set NODE_BIN=%BTOA_HOME%/node.exe) ELSE (SET NODE_BIN=node.exe)
set TARGET_FOLDER=src/webapp

IF "%1"=="" (
	goto exit

IF NOT "%2"=="" (
	set TARGET_FOLDER=%2%
)

echo.
echo Minify process started - %1% project  
echo.
	
) else IF "%1"=="enyo" (
	goto enyo
	
) else IF "%1"=="jqm" (
	goto native
) else IF "%1"=="angular" (
	goto native
) else IF "%1"=="native" (
	echo.
	echo Minify process started - Native project  
	echo.
	goto native
	
) else IF "%1"=="sencha" (
	goto sencha	         
)



goto done 

:enyo
@call src\webapp\enyo\tools\minify.bat package.js -output %TARGET_FOLDER%\app.min %@
goto done

:jqm
:native
@call %NODE_BIN% %BTOA_HOME%\node_modules\requirejs\bin\r.js -o package.js 
REM @call node %BTOA_HOME%\node_modules\requirejs\bin\r.js -o package.js out=%TARGET_FOLDER%\app.min.js
goto done

:sencha
cd ./src/webapp
@call sencha create jsb -a index.html -p app.jsb3 -v
@call sencha build -p app.jsb3 -d . -v
REM In case the minify process failed you can run it manually to be able to see the errors
REM e.g. java -jar "C:/Program Files (x86)/SenchaSDKTools-2.0.0-beta3/bin/yuicompressor.jar" -o app-all-min.js all-classes.js
 
goto done


:exit
echo *** HPA - Minify process aborted
echo.
echo *** Required parameters:
echo *** param: project type [enyo / sencha / native / jqm (using requirejs)] 
echo.

echo ****** HPA - Minify Process done
echo.
:done


REM ********************************************************************

