xas99.py -R -i -L shark.lst source/shark.a99 -o shark
@IF %ERRORLEVEL% NEQ 0 GOTO :end

java -jar tools/ea5tocart.jar shark "FLYING SHARK" > make.log

xas99.py -R -b source/level1-rom.a99 -o build/level1-rom
tools\pad.exe build/level1-rom_6000_b0 build/level1_b0.bin 8192
tools\pad.exe build/level1-rom_6000_b1 build/level1_b1.bin 8192
tools\pad.exe build/level1-rom_6000_b2 build/level1_b2.bin 8192
tools\pad.exe build/level1-rom_6000_b3 build/level1_b3.bin 8192
copy /b shark8.bin + build\level1_b0.bin + build\level1_b1.bin + build\level1_b2.bin + build\level1_b3.bin flying-shark-8.bin

WHERE jar
@IF %ERRORLEVEL% NEQ 0 GOTO :end
jar -cvf flying-shark.rpk flying-shark-8.bin layout.xml > make.log

:end
