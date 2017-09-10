/**
 * Created by Rasmus on 09-09-2017.
 */
public class Atan {

    private static final int SCALE = 32;

    public static void main(String... args) {
        int x0 = 9;
        int y0 = 12;
        for (int y = 0; y < 25; y++) {
            System.out.print("       byte ");
            int y1 = y - y0;
            for (int x = 0; x < 19; x++) {
                int x1 = x - x0;
                double a = Math.atan2(-x1, y1); // Rotate one quarter clockwise
                int result;
                if (Double.isFinite(a)) {
                    result = (int) Math.floor(((a / Math.PI) + 1) * SCALE / 2);
                    result = (result + 2) & (SCALE - 1);
                } else {
                    result = -1;
                }
                System.out.print((result < 10 ? "0" : "") + result + (x < 18 ? "," : ""));
            }
            System.out.println();
        }
        System.out.println();
        for (int v = 0; v < SCALE; v++) {
            double a = ((v - 2) & (SCALE -1)) * (2 * Math.PI) / SCALE;
            double x = Math.sin(a);
            double y = -Math.cos(a);
            System.out.println("       data " + hexWord(x) + "," + hexWord(y));
        }
    }

    private static String hexWord(double d) {
        int i = (int) Math.floor(d * 256) & 0xffff;
        String s = Integer.toHexString(i);
        while (s.length() < 4) {
            s = "0" + s;
        }
        return ">" + s;
    }
}
