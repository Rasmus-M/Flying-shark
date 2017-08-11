xas99.py -R -b source/level1-rom.a99 -o build/level1-rom
tools\pad.exe build/level1-rom_6000_b0 build/level1_b0.bin 8192
tools\pad.exe build/level1-rom_6000_b1 build/level1_b1.bin 8192
tools\pad.exe build/level1-rom_6000_b2 build/level1_b2.bin 8192
tools\pad.exe build/level1-rom_6000_b2 build/level1_b3.bin 8192