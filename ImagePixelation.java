import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImagePixelation extends JPanel {
    private static int WIDTH;
    private static int HEIGHT;
    private static int TYPE;
    private static int SQUARE_SIZE = 40;
    private static BufferedImage resultImage;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        String fileName = "test.jpg";
        BufferedImage image = ImageIO.read(new File(fileName));
        WIDTH = image.getWidth();
        HEIGHT = image.getHeight();
        TYPE = image.getType();

        System.err.println("Pixelating image of size " + WIDTH + "x" + HEIGHT);

        resultImage = new BufferedImage(WIDTH, HEIGHT, TYPE);
        resultImage.getGraphics().drawImage(image, 0, 0, null);

        // Create a window to display the image
        JFrame frame = new JFrame("Image Pixelation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        JLabel label = new JLabel(new ImageIcon(resultImage));
        frame.add(label);
        frame.setVisible(true);

        // Scale the image to fit the window
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        double scaleFactor = Math.min((double) frameWidth / WIDTH, (double) frameHeight / HEIGHT);
        int scaledWidth = (int) (WIDTH * scaleFactor);
        int scaledHeight = (int) (HEIGHT * scaleFactor);

        BufferedImage pixelatedImage = singleThreadPixelation(image, label, scaledWidth, scaledHeight);
        ImageIO.write(pixelatedImage, "jpg", new File("result.jpg"));
        System.out.println("Pixelated image is saved as result.jpg");
    }
    
    private static BufferedImage singleThreadPixelation(BufferedImage image, JLabel label, int scaledWidth, int scaledHeight) throws InterruptedException {
        for (int y = 0; y < HEIGHT; y += SQUARE_SIZE) {
            for (int x = 0; x < WIDTH; x += SQUARE_SIZE) {
                fillSquareWithAverageColor(image, resultImage, x, y);
                Image scaledImage = resultImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
                label.repaint();
                Thread.sleep(10);
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
