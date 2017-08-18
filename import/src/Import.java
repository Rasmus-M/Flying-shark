import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Rasmus on 17-08-2017.
 */
public class Import {

    public static final int SPRITE_WIDTH = 24;
    public static final int SPRITE_HEIGHT = 16;
    public static final int BITS_PER_PIXEL = 1;
    public static final int PLANES = 2;

    public static final int SPRITES_X = 4;

    public static void main(String[] args) throws IOException {
        FileInputStream fileInputStream = new FileInputStream("zx-spectrum/graphics.bin");
        byte[] buffer = new byte[0x4000];
        int len = fileInputStream.read(buffer);
        fileInputStream.close();
        int lineSize = SPRITE_WIDTH * BITS_PER_PIXEL * PLANES / 8;
        int spriteSize = lineSize * SPRITE_HEIGHT;
        int numberOfSprites = len / spriteSize;
        int width = SPRITES_X * SPRITE_WIDTH;
        int spritesY = ((numberOfSprites / SPRITES_X) + (numberOfSprites % SPRITES_X == 0 ? 0 : 1));
        int height = spritesY * SPRITE_HEIGHT;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster raster = image.getRaster();
        int[] pixel = new int[] {255, 255, 255};
        int i0 = 33;
        for (int spriteY = 0; spriteY < spritesY; spriteY++) {
            int y0 = spriteY * SPRITE_HEIGHT;
            for (int spriteX = 0; spriteX < SPRITES_X; spriteX++) {
                int x0 = spriteX * SPRITE_WIDTH;
                for (int y1 = 0; y1 < SPRITE_HEIGHT; y1++) {
                    int y = y0 + y1;
                    for (int x1 = 0; x1 < SPRITE_WIDTH; x1++) {
                        int x = x0 + x1;
                        int i = i0 + y * PLANES + (x1 / 8) * SPRITE_HEIGHT * PLANES;
                        if (i < len) {
                            byte b = buffer[i];
                            if ((b & (0x80 >> (x1 % 8))) != 0) {
                                raster.setPixel(x, y, pixel);
                            }
                        }
                    }
                }
                i0 += spriteSize;
            }
        }
        FileOutputStream fileOutputStream = new FileOutputStream("zx-spectrum/graphics.png");
        ImageIO.write(image, "png", fileOutputStream);
        fileOutputStream.close();
    }
}
