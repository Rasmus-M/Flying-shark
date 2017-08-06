import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
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
        for (int m = 0; m < 4; m++) {
            new Encode("map" + (m + 1) + ".bin", 18, MAP_HEIGHTS[m]).run();
        }
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
                Map<Integer, Integer> tCharMap = new HashMap<>();
                Set<Integer> useSet = new HashSet<>();
                int maxSize = 0;
                int screen = 0;
                for (int y0 = height - SCREEN_HEIGHT; y0 >= 0; y0--) {
                    useSet.clear();
                    int n = 0;
                    int added = 0;
                    for (int y = y0; y < y0 + SCREEN_HEIGHT; y++) {
                        for (int x = 0; x < width; x++) {
                            int fromChar = map[y][x];
                            int toChar = map[y > 0 ? y - 1 : height - 1][x];
                            int key = (toChar << 8) | fromChar;
                            useSet.add(key);
                            Integer tChar = tCharMap.get(key);
                            if (tChar == null) {
                                tChar = n++;
                                tCharMap.put(key, tChar);
                                added++;
                            }
                            tMap[y][x] = tChar;
                        }
                    }
                    Set<Integer> deleted = new HashSet<>();
                    for (int key : tCharMap.keySet()) {
                        if (!useSet.contains(key)) {
                            deleted.add(key);
                        }
                    }
                    for (int key : deleted) {
                        tCharMap.remove(key);
                    }
                    maxSize = Math.max(maxSize, useSet.size());
                    System.out.println("Screen " + screen + ", deleted: " + deleted.size() + ", added: " + added + ", size = "+ useSet.size());
                    screen++;
                }
                System.out.println("Max size: " + maxSize);
                System.out.println();
            } else {
                throw new Exception("Error: " + len + " bytes found. Expected " + (width * height) + " bytes.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
