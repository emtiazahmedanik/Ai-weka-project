import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

public class ImageDatasetToArff {
    public static void main(String[] args) {
        File folder = new File("D:\\Ai Project\\Dataset\\Section 2 image folder"); // folder with your 400 images
        File[] imageFiles = folder.listFiles((dir, name) ->
            name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));

        if (imageFiles == null || imageFiles.length == 0) {
            System.out.println("No image files found.");
            return;
        }

        try (FileWriter writer = new FileWriter("digits_dataset.arff")) {

            // Read one image to determine size
            BufferedImage sampleImage = ImageIO.read(imageFiles[0]);
            int width = sampleImage.getWidth();
            int height = sampleImage.getHeight();
            int totalPixels = width * height;

            // Class labels
            List<String> classLabels = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                classLabels.add("B" + i);
                classLabels.add("E" + i);
            }

            // ARFF Header
            writer.write("@RELATION digit_classification\n\n");

            for (int i = 0; i < totalPixels; i++) {
                writer.write("@ATTRIBUTE pixel" + i + " {0,1}\n");
            }

            writer.write("@ATTRIBUTE class {" + String.join(",", classLabels) + "}\n\n");
            writer.write("@DATA\n");

            // Write data rows
            for (File imgFile : Objects.requireNonNull(imageFiles)) {
                BufferedImage image = ImageIO.read(imgFile);

                if (image.getWidth() != width || image.getHeight() != height) {
                    System.out.println("Skipping " + imgFile.getName() + " due to size mismatch.");
                    continue;
                }

                StringBuilder row = new StringBuilder();

                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int pixel = image.getRGB(x, y);
                        int r = (pixel >> 16) & 0xff;
                        int g = (pixel >> 8) & 0xff;
                        int b = pixel & 0xff;
                        int avg = (r + g + b) / 3;
                        int bin = (avg > 100) ? 1 : 0;
                        row.append(bin).append(",");
                    }
                }

                // Extract class label from filename, like G1_B4 → B4
                String fileName = imgFile.getName();
                String label = fileName.split("_")[1].split("\\.")[0];

                if (!classLabels.contains(label)) {
                    System.out.println("Unknown label in " + fileName);
                    continue;
                }

                row.append(label);
                writer.write(row + "\n");
            }

            System.out.println("ARFF file created successfully: digits_dataset.arff");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
