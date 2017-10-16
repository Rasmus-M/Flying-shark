import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Rasmus on 05-08-2017.
 */
public class Encode implements Runnable {

    private static final boolean VERBOSE = false;
    private static final int WINDOW_HEIGHT = 24;
    private static final int[] MAP_HEIGHTS = {382, 450, 340, 364};
    private static final int BANK_OFFSET = 60;

    private static Map<Integer, Integer> animap = new HashMap<>();
    static {
        animap.put(78, 0);
        animap.put(79, 1);
        animap.put(72, 2);
        animap.put(73, 3);
        animap.put(74, 4);
        animap.put(75, 5);
        animap.put(44, 6);
        animap.put(76, 7);
        animap.put(77, 8);
    }

    private final int level;
    private final String fileName;
    private final int width;
    private final int height;

    public static void main(String... args) {
        for (int level = 1; level <= 4; level++) {
            new Encode(level, "map" + level + ".mgb", 18, MAP_HEIGHTS[level - 1]).run();
        }
//        int level = 1;
//        new Encode(1, "map" + level + ".mgb", 18, MAP_HEIGHTS[level - 1]).run();
    }

    private Encode(int level, String fileName, int width, int height) {
        this.level = level;
        this.fileName = fileName;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
        try {
            System.out.println("****************");
            System.out.println("Map " + fileName);
            System.out.println("****************");
            FileInputStream fis = new FileInputStream(this.fileName);
            byte[] buffer = new byte[0x4000];
            int len = fis.read(buffer);
            fis.close();
            if (len == width * height) {
                System.out.println(len + " bytes loaded.");
                // Read characters
                byte[] chars = new byte[0x1000];
                fis = new FileInputStream("chars.bin");
                len = fis.read(chars);
                fis.close();
                int patOffs = len / 2;
                byte[] animChars = new byte[0x1000];
                fis = new FileInputStream("animated.bin");
                len = fis.read(animChars);
                fis.close();
                int aniPatOffs = len / 2;
                // Process map
                int[][] map = new int[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        map[y][x] = buffer[y * width + x] & 0xff;
                    }
                }
                StringBuilder rom_out = new StringBuilder();
                StringBuilder diff_out = new StringBuilder();
                int[][] tMap = new int[height][width];
                List<Integer> tChars = new ArrayList<>();
                Integer[] runningTChars = new Integer[128];
                int maxSize = 0;
                int maxIndex = 0;
                int screen = 0;
                for (int y0 = height - WINDOW_HEIGHT; y0 >= 0; y0--) {
                    if (VERBOSE) System.out.println("Screen " + screen + ":");
                    Set<Integer> used = new HashSet<>();
                    Map<Integer, Integer> added = new HashMap<>();
                    Set<Integer> deleted = new HashSet<>();
                    // Find tChars used in screen
                    for (int y = y0; y < y0 + WINDOW_HEIGHT; y++) {
                        for (int x = 0; x < width; x++) {
                            int fromChar = map[y][x];
                            int toChar = map[y > 0 ? y - 1 : height - 1][x];
                            int key = (toChar << 8) | fromChar;
                            used.add(key);
                        }
                    }
                    // Process screen
                    for (int y = y0; y < Math.min(y0 + WINDOW_HEIGHT, height); y++) {
                        for (int x = 0; x < width; x++) {
                            int fromChar = map[y][x];
                            int toChar = map[y > 0 ? y - 1 : height - 1][x];
                            int key = (toChar << 8) | fromChar;
                            // Is key in current set?
                            Integer runningIndex = null;
                            for (int i = 0; i < runningTChars.length && runningIndex == null; i++) {
                                Integer globalIndex = runningTChars[i];
                                if (globalIndex != null && tChars.get(globalIndex) == key) {
                                    runningIndex = i;
                                }
                            }
                            // If not, add it
                            if (runningIndex == null) {
                                for (int i = 0; i < runningTChars.length && runningIndex == null; i++) {
                                    Integer oldGlobalIndex = runningTChars[i];
                                    if (oldGlobalIndex == null || !used.contains(tChars.get(oldGlobalIndex))) {
                                        runningIndex = i;
                                        Integer globalIndex = null;
                                        for (int j = 0; j < tChars.size() && globalIndex == null; j++) {
                                            if (tChars.get(j) == key) {
                                                globalIndex = j;
                                            }
                                        }
                                        if (globalIndex == null) {
                                            globalIndex = tChars.size();
                                            tChars.add(key);
                                        }
                                        runningTChars[runningIndex] = globalIndex;
                                        maxIndex = Math.max(maxIndex, runningIndex);
                                        added.put(runningIndex, globalIndex);
                                        used.add(key);
                                    }
                                }
                            }
                            // Record in map
                            if (runningIndex != null) {
                                tMap[y][x] = runningIndex;
                            } else {
                                throw new Exception("No room found for key " + hexWord(key));
                            }
                        }
                    }
                    int iMax = 0;
                    for (int i = 0; i < runningTChars.length; i++) {
                        Integer globalIndex = runningTChars[i];
                        if (globalIndex != null && !used.contains(tChars.get(globalIndex))) {
                            runningTChars[i] = null;
                            deleted.add(i);
                        }
                        if (runningTChars[i] != null) {
                            iMax = i;
                        }
                    }
                    maxSize = Math.max(maxSize, used.size());

                    if (VERBOSE) System.out.print("Add: ");
                    diff_out.append("level_" + level + "_" + to3Digits(screen) + "_add:\n");
                    diff_out.append("       byte " + hexByte(added.size()) + "\n");
                    for (Integer i : added.keySet()) {
                        int globalIndex = added.get(i);
                        if (VERBOSE) System.out.print(hexByte(i) + ":" + hexWord(globalIndex) + " ");
                        diff_out.append("       byte " + hexByte(i) + ", " + hexByte(globalIndex >> 8) + ", " + hexByte(globalIndex & 0xff) + "              ; " + hexWord(tChars.get(globalIndex)) + "\n");
                    }
                    if (VERBOSE) System.out.println();

                    if (VERBOSE) System.out.print("Delete: ");
                    diff_out.append("level_" + level + "_" + to3Digits(screen) + "_delete:\n");
                    diff_out.append("       byte " + hexByte(deleted.size()) + "\n");
                    for (Integer i : deleted) {
                        if (VERBOSE) System.out.print(hexByte(i) + " ");
                        diff_out.append("       byte " + hexByte(i) + "\n");
                    }
                    if (VERBOSE) System.out.println();

                    if (VERBOSE) System.out.println("Deleted: " + deleted.size() + ", Added: " + added.size() + ", Used = "+ used.size() + " of " + (iMax + 1));
                    screen++;
                }
                System.out.println("TChars: " + tChars.size());
                for (int scrollOffset = 0; scrollOffset < 8; scrollOffset++) {
                    rom_out.append("*******************************************\n");
                    rom_out.append("       aorg >6000," + scrollOffset + "\n");
                    rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                    // Patterns
                    rom_out.append("*******************************************\n");
                    rom_out.append("* Patterns offset " + scrollOffset + "\n");
                    rom_out.append("*******************************************\n");
                    for (int i = 0; i < tChars.size(); i++) {
                        int key = tChars.get(i);
                        int toChar = (key >> 8) & 0xff;
                        int toAniIndex = animap.getOrDefault(toChar, -1);
                        int fromChar = key & 0xff;
                        int fromAniIndex = animap.getOrDefault(fromChar, -1);
                        if (VERBOSE) System.out.println(hexWord(i) + ": " + hexWord(key));
                        rom_out.append("* From " + hexByte(fromChar) + " to " + hexByte(toChar) + "\n");
                        rom_out.append("level_" + level + "_pattern_" + to3Digits(i) + "_" + scrollOffset + ":\n");
                        rom_out.append("       byte ");
                        int n = 0;
                        for (int j = scrollOffset; j < 8; j++, n++) {
                            byte b;
                            if (toAniIndex == -1) {
                                b = chars[patOffs + toChar * 8 + j];
                            } else {
                                b = animChars[aniPatOffs + toAniIndex * 64 + (7 - scrollOffset) * 8 + j];
                            }
                            rom_out.append(hexByte(b & 0xff)).append(n < 7 ? "," : "\n");
                        }
                        for (int j = 0; j < scrollOffset; j++, n++) {
                            byte b;
                            if (fromAniIndex == -1) {
                                b = chars[patOffs + fromChar * 8 + j];
                            } else {
                                b = animChars[aniPatOffs + fromAniIndex * 64 + (7 - scrollOffset) * 8 + j];
                            }
                            rom_out.append(hexByte(b & 0xff)).append(n < 7 ? "," : "\n");
                        }
                    }
                    rom_out.append("       bss  " + hexWord(0x1000 - tChars.size() * 8) + "\n");
                    // Colors
                    rom_out.append("*******************************************\n");
                    rom_out.append("* Colors offset " + scrollOffset + "\n");
                    rom_out.append("*******************************************\n");
                    for (int i = 0; i < tChars.size(); i++) {
                        int key = tChars.get(i);
                        int toChar = (key >> 8) & 0xff;
                        int toAniIndex = animap.getOrDefault(toChar, -1);
                        int fromChar = key & 0xff;
                        int fromAniIndex = animap.getOrDefault(fromChar, -1);
                        rom_out.append("* From " + hexByte(fromChar) + " to " + hexByte(toChar) + "\n");
                        rom_out.append("level_" + level + "_color_" + to3Digits(i)  + "_" + scrollOffset + ":\n");
                        rom_out.append("       byte ");
                        int n = 0;
                        for (int j = scrollOffset; j < 8; j++, n++) {
                            byte b;
                            if (toAniIndex == -1) {
                                b = chars[toChar * 8 + j];
                            } else {
                                b = animChars[toAniIndex * 64 + (7 - scrollOffset) * 8 + j];
                            }
                            rom_out.append(hexByte(b & 0xff)).append(n < 7 ? "," : "\n");
                        }
                        for (int j = 0; j < scrollOffset; j++, n++) {
                            byte b;
                            if (fromAniIndex == -1) {
                                b = chars[fromChar * 8 + j];
                            } else {
                                b = animChars[fromAniIndex * 64 + (7 - scrollOffset) * 8 + j];
                            }
                            rom_out.append(hexByte(b & 0xff)).append(n < 7 ? "," : "\n");
                        }
                    }
                }
                if (VERBOSE) System.out.println("Map:");
                rom_out.append("       aorg >6000,8\n");
                rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                rom_out.append("*******************************************\n");
                rom_out.append("level_" + level + "_map:\n");
                for (int y = 0; y < tMap.length; y++) {
                    int[] row = tMap[y];
                    rom_out.append("       byte ");
                    for (int x = 0; x < row.length; x++) {
                        if (VERBOSE) System.out.print(hexByte(row[x]));
                        rom_out.append(hexByte(row[x])).append(x < row.length - 1 ? "," : "\n");
                    }
                    if (VERBOSE) System.out.println();
                }
                rom_out.append("       aorg >6000,9\n");
                rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                rom_out.append("*******************************************\n");
                rom_out.append("level_" + level + "_map_high:\n");
                for (int y = 0; y < tMap.length; y++) {
                    int[] row = tMap[y];
                    rom_out.append("       byte ");
                    for (int x = 0; x < row.length; x++) {
                        rom_out.append(hexByte(row[x] + 0x80)).append(x < row.length - 1 ? "," : "\n");
                    }
                }
                rom_out.append("       aorg >6000,10\n");
                rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                rom_out.append("*******************************************\n");
                rom_out.append("level_" + level + "_max:\n");
                rom_out.append("       byte " + hexByte(maxIndex + 1) + "\n");
                rom_out.append(diff_out);

                if (VERBOSE) System.out.println();
                System.out.println("Max size: " + maxSize);
                System.out.println("Max index: " + maxIndex);
                System.out.println();

                // Write output
                FileWriter writer = new FileWriter("../source/level" + level + "-rom.a99");
                writer.write(rom_out.toString());
                writer.close();
            } else {
                throw new Exception("Error: " + len + " bytes found. Expected " + (width * height) + " bytes.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String to3Digits(int i) {
        StringBuilder s = new StringBuilder(Integer.toString(i));
        while (s.length() < 3) {
            s.insert(0, "0");
        }
        return s.toString();
    }

    private String hexByte(int b) {
        if (b < 256) {
            StringBuilder s = new StringBuilder(Integer.toHexString(b));
            while (s.length() < 2) {
                s.insert(0, "0");
            }
            return ">" + s.toString();
        } else {
            return "!!";
        }
    }

    private String hexWord(int w) {
        StringBuilder s = new StringBuilder(Integer.toHexString(w));
        while (s.length() < 4) {
            s.insert(0, "0");
        }
        return ">" + s.toString();
    }
}
