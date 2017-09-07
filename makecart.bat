xas99.py -R -i -S -L shark.lst source/shark.a99 -o shark
@IF %ERRORLEVEL% NEQ 0 GOTO :end

java -jar tools/ea5tocart.jar shark "FLYING SHARK" > make.log

xas99.py -R -b source/level1-rom.a99 -o build/level1-rom
xas99.py -R -b source/level2-rom.a99 -o build/level2-rom
xas99.py -R -b source/level3-rom.a99 -o build/level3-rom
xas99.py -R -b source/level4-rom.a99 -o build/level4-rom

tools\pad.exe build/level1-rom_6000_b0 build/level1_b0.bin 8192
tools\pad.exe build/level1-rom_6000_b1 build/level1_b1.bin 8192
tools\pad.exe build/level1-rom_6000_b2 build/level1_b2.bin 8192
tools\pad.exe build/level1-rom_6000_b3 build/level1_b3.bin 8192
tools\pad.exe build/level2-rom_6000_b0 build/level2_b0.bin 8192
tools\pad.exe build/level2-rom_6000_b1 build/level2_b1.bin 8192
tools\pad.exe build/level2-rom_6000_b2 build/level2_b2.bin 8192
tools\pad.exe build/level2-rom_6000_b3 build/level2_b3.bin 8192
tools\pad.exe build/level3-rom_6000_b0 build/level3_b0.bin 8192
tools\pad.exe build/level3-rom_6000_b1 build/level3_b1.bin 8192
tools\pad.exe build/level3-rom_6000_b2 build/level3_b2.bin 8192
tools\pad.exe build/level3-rom_6000_b3 build/level3_b3.bin 8192
tools\pad.exe build/level4-rom_6000_b0 build/level4_b0.bin 8192
tools\pad.exe build/level4-rom_6000_b1 build/level4_b1.bin 8192
tools\pad.exe build/level4-rom_6000_b2 build/level4_b2.bin 8192
tools\pad.exe build/level4-rom_6000_b3 build/level4_b3.bin 8192

copy /b ^
shark8.bin + ^
build\level1_b0.bin + ^
build\level1_b1.bin + ^
build\level1_b2.bin + ^
build\level1_b3.bin + ^
build\level2_b0.bin + ^
build\level2_b1.bin + ^
build\level2_b2.bin + ^
build\level2_b3.bin + ^
build\level3_b0.bin + ^
build\level3_b1.bin + ^
build\level3_b2.bin + ^
build\level3_b3.bin + ^
build\level4_b0.bin + ^
build\level4_b1.bin + ^
build\level4_b2.bin + ^
build\level4_b3.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin + ^
build\empty.bin ^
flying-shark-8.bin

java -jar tools/CopyHeader.jar flying-shark-8.bin 60

WHERE jar
@IF %ERRORLEVEL% NEQ 0 GOTO :end
jar -cvf flying-shark.rpk flying-shark-8.bin layout.xml > make.log

:end
