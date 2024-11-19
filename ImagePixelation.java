import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImagePixelation {
    static int WIDTH;
    static int HEIGHT;
    static int TYPE;
    static int SQUARE_SIZE = 40;
    
    public static void main(String[] args) throws IOException {
    String fileName = "test.jpg";
    BufferedImage image = ImageIO.read(new File(fileName));
    WIDTH = image.getWidth();
    HEIGHT = image.getHeight();
    TYPE = image.getType();

    System.err.println("Pixelating image of size " + WIDTH + "x" + HEIGHT);
    BufferedImage pixelatedImage = singleThreadPixelation(image);

    ImageIO.write(pixelatedImage, "jpg", new File("result.jpg"));
    System.out.println("Pixelated image is saved as result.jpg");
    }
    
    private static BufferedImage singleThreadPixelation(BufferedImage image) {
        BufferedImage resultImage = new BufferedImage(WIDTH, HEIGHT, TYPE);
        for (int y = 0; y < HEIGHT; y += SQUARE_SIZE) {
            for (int x = 0; x < WIDTH; x += SQUARE_SIZE) {
                fillSquareWithAverageColor(image, resultImage, x, y);
            }
        }
        return resultImage;
    }

    private static void fillSquareWithAverageColor(BufferedImage image, BufferedImage resultImage, int startX, int startY) {
        int widthToFill = Math.min(SQUARE_SIZE, WIDTH - startX);
        int heightToFill = Math.min(SQUARE_SIZE, HEIGHT - startY);

        int R = 0;
        int G = 0;
        int B = 0;
        int pixelCount = 0;

        for (int y = startY; y < startY + heightToFill; y++) {
            for (int x = startX; x < startX + widthToFill; x++) {
                Color color = new Color(image.getRGB(x, y));
                R += color.getRed();
                G += color.getGreen();
                B += color.getBlue();
                pixelCount++;
            }
        }
    
        int avgRGB = getAverageRGB(R, G, B, pixelCount);
        fillArea(resultImage, startX, startY, avgRGB);
    }

    private static int getAverageRGB(int R, int G, int B, int pixelCount) {
        int avgR = (int) (R / pixelCount);
        int avgG = (int) (G / pixelCount);
        int avgB= (int) (B / pixelCount);
    
        Color avgColor = new Color(avgR, avgG, avgB);
        int avgRGB = avgColor.getRGB();
        return avgRGB;
    }

    private static void fillArea(BufferedImage resultImage, int startX, int startY, int avgRGB) {
        for (int y = startY; y < startY + SQUARE_SIZE && y < HEIGHT; y++) {
            for (int x = startX; x < startX + SQUARE_SIZE && x < WIDTH; x++) {
                resultImage.setRGB(x, y, avgRGB);
            }
        }
    }
}
