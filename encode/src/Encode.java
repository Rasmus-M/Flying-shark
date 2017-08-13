import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by Rasmus on 05-08-2017.
 */
public class Encode implements Runnable {

    private static final int WINDOW_HEIGHT = 24;
    private static final int[] MAP_HEIGHTS = {358, 450, 316, 364};
    private static final int BANK_OFFSET = 60;

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
                byte[] chars = new byte[0x1000];
                fis = new FileInputStream("chars.bin");
                len = fis.read(chars);
                fis.close();
                int patOffs = len / 2;
                int[][] map = new int[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        map[y][x] = buffer[y * width + x] & 0xff;
                    }
                }
                StringBuilder rom_out = new StringBuilder();
                StringBuilder ram_out = new StringBuilder();
                int[][] tMap = new int[height][width];
                List<Integer> tChars = new ArrayList<>();
                Integer[] runningTChars = new Integer[128];
                int maxSize = 0;
                int screen = 0;
                for (int y0 = height - WINDOW_HEIGHT; y0 >= 0; y0--) {
                    System.out.println("Screen " + screen + ":");
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
                                        added.put(runningIndex, globalIndex);
                                        used.add(key);
                                    }
                                }
                            }
                            // Record in map
                            if (runningIndex != null) {
                                tMap[y][x] = runningIndex;
                            } else {
                                throw new Exception("No room found for key >" + hexWord(key));
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

                    System.out.print("Add: ");
                    ram_out.append("level_" + level + "_" + to3Digits(screen) + "_add:\n");
                    ram_out.append("       byte " + hexByte(added.size()) + "\n");
                    for (Integer i : added.keySet()) {
                        int globalIndex = added.get(i);
                        System.out.print(hexByte(i) + ":" + hexWord(globalIndex) + " ");
                        ram_out.append("       byte " + hexByte(i) + ", " + hexByte(globalIndex >> 8) + ", " + hexByte(globalIndex & 0xff) + "              ; " + hexWord(tChars.get(globalIndex)) + "\n");
                    }
                    System.out.println();

                    System.out.print("Delete: ");
                    ram_out.append("level_" + level + "_" + to3Digits(screen) + "_delete:\n");
                    ram_out.append("       byte " + hexByte(deleted.size()) + "\n");
                    for (Integer i : deleted) {
                        System.out.print(hexByte(i) + " ");
                        ram_out.append("       byte " + hexByte(i) + "\n");
                    }
                    System.out.println();

                    System.out.println("Deleted: " + deleted.size() + ", Added: " + added.size() + ", Used = "+ used.size() + " of " + (iMax + 1));
                    screen++;
                }
                rom_out.append("       aorg >6000,0\n");
                rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                System.out.println("TChars: " + tChars.size());
                for (int i = 0; i < tChars.size(); i++) {
                    int key = tChars.get(i);
                    int toChar = (key >> 8) & 0xff;
                    int fromChar = key & 0xff;
                    System.out.println(hexWord(i) + ": " + hexWord(key));
                    rom_out.append("* From " + hexByte(fromChar) + " to " + hexByte(toChar) + "\n");
                    rom_out.append("level_" + level + "_pattern_" + to3Digits(i) + ":\n");
                    rom_out.append("       byte ");
                    for (int j = 0; j < 8; j++) {
                        rom_out.append(hexByte(chars[patOffs + toChar * 8 + j] & 0xff)).append(j < 7 ? "," : "\n");
                    }
                    rom_out.append("       byte ");
                    for (int j = 0; j < 8; j++) {
                        rom_out.append(hexByte(chars[patOffs + fromChar * 8 + j] & 0xff)).append(j < 7 ? "," : "\n");
                    }
                }
                rom_out.append("       aorg >6000,1\n");
                rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                for (int i = 0; i < tChars.size(); i++) {
                    int key = tChars.get(i);
                    int toChar = (key >> 8) & 0xff;
                    int fromChar = key & 0xff;
                    rom_out.append("* From " + hexByte(fromChar) + " to " + hexByte(toChar) + "\n");
                    rom_out.append("level_" + level + "_color_" + to3Digits(i) + ":\n");
                    rom_out.append("       byte ");
                    for (int j = 0; j < 8; j++) {
                        rom_out.append(hexByte(chars[toChar * 8 + j] & 0xff)).append(j < 7 ? "," : "\n");
                    }
                    rom_out.append("       byte ");
                    for (int j = 0; j < 8; j++) {
                        rom_out.append(hexByte(chars[fromChar * 8 + j] & 0xff)).append(j < 7 ? "," : "\n");
                    }
                }
                System.out.println("Map:");
                rom_out.append("       aorg >6000,2\n");
                rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                rom_out.append("level_" + level + "_map:\n");
                for (int y = 0; y < tMap.length; y++) {
                    int[] row = tMap[y];
                    rom_out.append("       byte ");
                    for (int x = 0; x < row.length; x++) {
                        System.out.print(hexByte(row[x]));
                        rom_out.append(hexByte(row[x])).append(x < row.length - 1 ? "," : "\n");
                    }
                    System.out.println();
                }
                rom_out.append("       aorg >6000,3\n");
                rom_out.append("       bss  " + hexWord(BANK_OFFSET) + "\n");
                rom_out.append("level_" + level + "_map_high:\n");
                for (int y = 0; y < tMap.length; y++) {
                    int[] row = tMap[y];
                    rom_out.append("       byte ");
                    for (int x = 0; x < row.length; x++) {
                        rom_out.append(hexByte(row[x] + 0x80)).append(x < row.length - 1 ? "," : "\n");
                    }
                }
                System.out.println();
                System.out.println("Max size: " + maxSize);
                System.out.println();

                // Write output
                FileWriter writer = new FileWriter("../source/level" + level + "-rom.a99");
                writer.write(rom_out.toString());
                writer.close();
                writer = new FileWriter("../source/level" + level + "-diff.a99");
                writer.write(ram_out.toString());
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
