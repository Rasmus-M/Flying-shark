IF EXIST shark.dsk GOTO :dskok
xdm99.py shark.dsk --initialize DSSD -n SHARK
:dskok

xas99.py -R -L shark.lst source/shark.a99
@IF %ERRORLEVEL% NEQ 0 GOTO :end

xdm99.py shark.dsk -a shark.obj -f DF80 -n SHARK3

xas99.py -R -i source/shark.a99 -o shark

xdm99.py shark.dsk -a shark -n SHARK
xdm99.py shark.dsk -a sharl -n SHARL
rem xdm99.py shark.dsk -a sharkm -n SHARM

java -jar tools/ea5tocart.jar shark "FLYING SHARK" > make.log

WHERE jar
@IF %ERRORLEVEL% NEQ 0 GOTO :end
jar -cvf flying-shark.rpk shark8.bin layout.xml > make.log

:end
