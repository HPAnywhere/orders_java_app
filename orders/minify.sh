#!/bin/sh

TARGET_FOLDER=src/webapp


if [ -z "$1" ]
  then
    echo "No argument supplied"
	exit
fi

 
if [ -z "$1" ]
  then
	TARGET_FOLDER=%2%
fi
 
echo Minify process started - $1 project  
 
if [ "$1" == "enyo" ]; then
	echo enyo
	exec src/webapp/enyo/tools/minify.sh package.js -output $TARGET_FOLDER/app.min $@
  else
	  if [ "$1" == "jqm" ]; then
			echo "jqm"
			exec node $BTOA_HOME/node_modules/requirejs/bin/r.js -o package.js | cat
		else
			if [  "$1" == "angular" ];then
						echo "angular"
						exec node $BTOA_HOME/node_modules/requirejs/bin/r.js -o package.js | cat
				else
					if [ "$1" == "native" ]; then
							echo "native"
							exec node $BTOA_HOME/node_modules/requirejs/bin/r.js -o package.js | cat
						else
						if [ "$1" == "sencha" ]; then
								echo "sencha"
								cd ./src/webapp
								exec sencha create jsb -a index.html -p app.jsb3 -v | cat
								exec sencha build -p app.jsb3 -d . -v | cat
							else
								 echo "HPA - Minify process aborted"
								 echo "Required parameters:"
								 echo "param: project type [enyo / sencha / native / jqm (using requirejs)]"
							 fi
					fi
			fi
		fi
fi

echo "HPA - Minify Process done"
echo
