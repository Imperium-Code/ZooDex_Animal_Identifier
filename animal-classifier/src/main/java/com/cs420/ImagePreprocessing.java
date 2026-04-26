package com.cs420;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class ImagePreprocessing {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java com.cs420.ImagePreprocessing <input_path> <output_path>");
            System.exit(1);
        }

        String inputPath = args[0];
        String outputPath = args[1];

        preprocessImage(inputPath, outputPath);
    }

    public static void preprocessImage(String inputPath, String outputPath) {
        try {
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                throw new RuntimeException("Error: Could not read image from " + inputPath);
            }

            BufferedImage originalImage = ImageIO.read(inputFile);
            if (originalImage == null) {
                throw new RuntimeException("Error: Could not interpret image from " + inputPath);
            }

            // Create a grayscale BufferedImage of size 128x128
            BufferedImage resizedImage = new BufferedImage(128, 128, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = resizedImage.createGraphics();

            // Interpolation area/bilinear for high quality reduction
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(originalImage, 0, 0, 128, 128, null);
            g2d.dispose();

            // Extract file extension and determine format (defaulting to png to avoid
            // missing format info)
            String formatName = "png";
            int dotIndex = outputPath.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < outputPath.length() - 1) {
                formatName = outputPath.substring(dotIndex + 1).toLowerCase();
                if (formatName.equals("jpg") || formatName.equals("jpeg")) {
                    formatName = "jpg";
                }
            }

            File outputFile = new File(outputPath);
            boolean success = ImageIO.write(resizedImage, formatName, outputFile);
            if (success) {
                System.out.println("Successfully processed: " + outputPath);
            } else {
                throw new RuntimeException("Error: Could not write image to " + outputPath + " (format support issue?)");
            }

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("Error processing " + inputPath + ": " + e.getMessage(), e);
        }
    }
}
