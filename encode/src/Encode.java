import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Rasmus on 05-08-2017.
 */
public class Encode implements Runnable {

    private static final int SCREEN_HEIGHT = 24;
    private static final int[] MAP_HEIGHTS = {358, 450, 316, 364};


    private final String fileName;
    private final int width;
    private final int height;

    public static void main(String... args) {
//        for (int m = 0; m < 4; m++) {
//            new Encode("map" + (m + 1) + ".mgb", 18, MAP_HEIGHTS[m]).run();
//        }
        int m = 3;
        new Encode("map" + (m + 1) + ".mgb", 18, MAP_HEIGHTS[m]).run();
    }

    private Encode(String fileName, int width, int height) {
        this.fileName = fileName;
        this.width = width;
        this.height = height;
    }

    @Override
    public void run() {
        try {
            System.out.println("************");
            System.out.println("Map " + fileName);
            System.out.println("************");
            FileInputStream fis = new FileInputStream(this.fileName);
            byte[] buffer = new byte[0x4000];
            int len = fis.read(buffer);
            fis.close();
            if (len == width * height) {
                System.out.println(len + " bytes loaded.");
                int[][] map = new int[height][width];
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        map[y][x] = buffer[y * width + x] & 0xff;
                    }
                }
                int[][] tMap = new int[height][width];
                Set<Integer> tChars = new HashSet<>();
                Integer[] runningTChars = new Integer[128];
                int maxSize = 0;
                int screen = 0;
                int n = 0;
                for (int y0 = height - SCREEN_HEIGHT; y0 >= 0; y0--) {
                    Set<Integer> used = new HashSet<>();
                    Map<Integer, Integer> added = new HashMap<>();
                    Set<Integer> deleted = new HashSet<>();
                    for (int y = y0; y < y0 + SCREEN_HEIGHT; y++) {
                        for (int x = 0; x < width; x++) {
                            int fromChar = map[y][x];
                            int toChar = map[y > 0 ? y - 1 : height - 1][x];
                            int key = (toChar << 8) | fromChar;
                            used.add(key);
                        }
                    }
                    for (int y = y0; y < y0 + SCREEN_HEIGHT; y++) {
                        for (int x = 0; x < width; x++) {
                            int fromChar = map[y][x];
                            int toChar = map[y > 0 ? y - 1 : height - 1][x];
                            int key = (toChar << 8) | fromChar;
                            Integer tChar = null;
                            for (int i = 0; i < runningTChars.length && tChar == null; i++) {
                                Integer oldKey = runningTChars[i];
                                if (oldKey != null && oldKey == key) {
                                    tChar = i;
                                }
                            }
                            if (tChar == null) {
                                for (int i = 0; i < runningTChars.length && tChar == null; i++) {
                                    Integer oldKey = runningTChars[i];
                                    if (oldKey == null || !used.contains(oldKey)) {
                                        tChar = i;
                                        runningTChars[tChar] = key;
                                        tChars.add(key);
                                        added.put(i, key);
                                    }
                                }
                            }
                            if (tChar != null) {
                                tMap[y][x] = tChar;
                            } else {
                                throw new Exception("No room found for key >" + hexWord(key));
                            }
                        }
                    }
                    for (int i = 0; i < runningTChars.length; i++) {
                        Integer key = runningTChars[i];
                        if (key != null && !used.contains(key)) {
                            runningTChars[i] = null;
                            deleted.add(i);
                        }
                    }
                    maxSize = Math.max(maxSize, used.size());
                    System.out.println("Screen " + screen + ", deleted: " + deleted.size() + ", added: " + added.size() + ", size = "+ used.size());
                    screen++;
                }
                for (int y = 0; y < tMap.length; y++) {
                    int[] row = tMap[y];
                    for (int x = 0; x < row.length; x++) {
                        System.out.print(hexByte(row[x]));
                    }
                    System.out.println();
                }



                System.out.println("Max size: " + maxSize);
                System.out.println("TChars: " + tChars.size());
                System.out.println();
            } else {
                throw new Exception("Error: " + len + " bytes found. Expected " + (width * height) + " bytes.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String hexByte(int b) {
        StringBuilder s = new StringBuilder(Integer.toHexString(b));
        while (s.length() < 2) {
            s.insert(0, "0");
        }
        return s.toString();
    }

    private String hexWord(int w) {
        StringBuilder s = new StringBuilder(Integer.toHexString(w));
        while (s.length() < 4) {
            s.insert(0, "0");
        }
        return s.toString();
    }
}
