xas99.py -R -i -S -L shark.lst source/shark.a99 -o shark
@IF %ERRORLEVEL% NEQ 0 GOTO :end

java -jar tools/ea5tocart.jar shark "FLYING SHARK" > make.log

copy /b ^
shark8.bin + ^
build\level1_b0.bin + ^
build\level1_b1.bin + ^
build\level1_b2.bin + ^
build\level1_b3.bin + ^
build\level1_b4.bin + ^
build\level1_b5.bin + ^
build\level1_b6.bin + ^
build\level1_b7.bin + ^
build\level1_b8.bin + ^
build\level1_b9.bin + ^
build\level2_b0.bin + ^
build\level2_b1.bin + ^
build\level2_b2.bin + ^
build\level2_b3.bin + ^
build\level2_b4.bin + ^
build\level2_b5.bin + ^
build\level2_b6.bin + ^
build\level2_b7.bin + ^
build\level2_b8.bin + ^
build\level2_b9.bin + ^
build\level3_b0.bin + ^
build\level3_b1.bin + ^
build\level3_b2.bin + ^
build\level3_b3.bin + ^
build\level3_b4.bin + ^
build\level3_b5.bin + ^
build\level3_b6.bin + ^
build\level3_b7.bin + ^
build\level3_b8.bin + ^
build\level3_b9.bin + ^
build\level4_b0.bin + ^
build\level4_b1.bin + ^
build\level4_b2.bin + ^
build\level4_b3.bin + ^
build\level4_b4.bin + ^
build\level4_b5.bin + ^
build\level4_b6.bin + ^
build\level4_b7.bin + ^
build\level4_b8.bin + ^
build\level4_b9.bin + ^
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
