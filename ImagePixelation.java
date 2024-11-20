import java.io.File;
import java.io.IOException;
import javax.swing.Timer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImagePixelation {
    private static int WIDTH;
    private static int HEIGHT;
    private static int TYPE;
    private static int SQUARE_SIZE;
    private static BufferedImage resultImage;
    
    public static void main(String[] args) throws IOException, InterruptedException {
        if (!validateArgs(args)) {
            return;
        }
        String fileName = args[0];
        SQUARE_SIZE = Integer.parseInt(args[1]);
        String mode = args[2].toUpperCase();

        BufferedImage image = loadImage(fileName);
        getImageProps(image);
        
        JFrame frame = new JFrame("Image Pixelation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        
        JLabel label = new JLabel(new ImageIcon(resultImage));
        frame.add(label);
        frame.setVisible(true);
        
        int[] getScaledResolution = getScaledResolution(frame);
        int scaledWidth = getScaledResolution[0];
        int scaledHeight = getScaledResolution[1];

        BufferedImage pixelatedImage = processImage(mode, image, label, scaledWidth, scaledHeight);
        
        saveImage(pixelatedImage);
    }
    
    private static BufferedImage singleThreadPixelation(BufferedImage image, JLabel label, int scaledWidth, int scaledHeight) throws InterruptedException {
        for (int y = 0; y < HEIGHT; y += SQUARE_SIZE) {
            for (int x = 0; x < WIDTH; x += SQUARE_SIZE) {
                fillSquareWithAverageColor(image, resultImage, x, y);
                displayScaledImage(label, scaledWidth, scaledHeight);
                Thread.sleep(10);
            }
        }
        return resultImage;
    }

    private static BufferedImage multiThreadPixelation(BufferedImage image, JLabel label, int scaledWidth, int scaledHeight) throws InterruptedException {
        int numOfThreads = Runtime.getRuntime().availableProcessors();
        int threadRows = HEIGHT / numOfThreads;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfThreads);

        displayScaledImage(label, scaledWidth, scaledHeight);
        
        for (int i = 0; i < numOfThreads; i++) {
            int startY = i * threadRows;
            int endY = Math.min(startY + threadRows, HEIGHT);
            executorService.submit(() -> pixelateRows(image, startY, endY));
        }
        Timer timer = new Timer(50, e -> {
            displayScaledImage(label, scaledWidth, scaledHeight);
            
        });
        timer.start();
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        timer.stop();
    
        displayScaledImage(label, scaledWidth, scaledHeight);
        return resultImage;
    }

    private static void pixelateRows(BufferedImage image, int startY, int endY) {
        for (int y = startY; y < endY; y += SQUARE_SIZE) {
            for (int x = 0; x < WIDTH; x += SQUARE_SIZE) {
                synchronized (resultImage) {
                    fillSquareWithAverageColor(image, resultImage, x, y);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static Image displayScaledImage(JLabel label, int scaledWidth, int scaledHeight) {
        Image scaledImage = resultImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
        label.setIcon(new ImageIcon(scaledImage));
        label.repaint();
        return scaledImage;
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
    private static boolean validateArgs(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java ImagePixelation <filename> <squareSize> <mode>");
            return false;
        }
    
        int squareSize = Integer.parseInt(args[1]);
        if (squareSize <= 0) {
            System.err.println("Invalid square size, provide a positive integer");
            return false;
        }
    
        String mode = args[2].toUpperCase();
        if (!mode.equals("S") && !mode.equals("M")) {
            System.err.println("Invalid mode, use 'S' for single-threaded or 'M' for multi-threaded pixelation");
            return false;
        }
        return true;
    }
    
    private static void getImageProps(BufferedImage image) {
        WIDTH = image.getWidth();
        HEIGHT = image.getHeight();
        TYPE = image.getType();
    
        resultImage = new BufferedImage(WIDTH, HEIGHT, TYPE);
        resultImage.getGraphics().drawImage(image, 0, 0, null);
    
        System.err.println("Pixelating image of size " + WIDTH + "x" + HEIGHT);
    }

    private static int[] getScaledResolution(JFrame frame) {
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        double scaleFactor = Math.min((double) frameWidth / WIDTH, (double) frameHeight / HEIGHT);
        int scaledWidth = (int) (WIDTH * scaleFactor);
        int scaledHeight = (int) (HEIGHT * scaleFactor);
        return new int[]{scaledWidth, scaledHeight};
    }

    private static BufferedImage processImage(String mode, BufferedImage image, JLabel label, int scaledWidth, int scaledHeight) throws InterruptedException {
        if (mode.equals("S")) {
            return singleThreadPixelation(image, label, scaledWidth, scaledHeight);
        } else {
            return multiThreadPixelation(image, label, scaledWidth, scaledHeight);
        }
    }

    private static BufferedImage loadImage(String fileName) {
        try {
            return ImageIO.read(new File(fileName));
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private static void saveImage(BufferedImage pixelatedImage) {
        try {
            ImageIO.write(pixelatedImage, "jpg", new File("result.jpg"));
            System.out.println("Pixelated image saved as result.jpg");
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        System.out.println("Pixelated image is saved as result.jpg");
    }
}

