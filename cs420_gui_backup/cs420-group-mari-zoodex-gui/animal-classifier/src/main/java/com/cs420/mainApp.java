package com.cs420;

import java.io.File;

/*
 *
 * To Compile:
 *   cd /workspaces/cs420-group/animal-classifier
 *   mvn compile
 *
 * to Run:
 *   mvn exec:java -Dexec.args="path/to/image.jpg"
 *
 * Example:
 *   mvn exec:java -Dexec.args="test-images/test31.jpg"
 *
 * The program will output the top ImageNet predictions and a final
 * decision on whether the image contains an animal.
 */

public class mainApp {

    public static void main(String[] args) throws Exception {

        // Make sure the user gives exactly one image path when running the program
        if (args.length != 1) {
            System.out.println("Usage: java com.cs420.mainApp /path/to/image.jpg");
            System.exit(1);
        }

        // Create a File object from the command-line argument
        File imageFile = new File(args[0]);

        // Stop early if the file does not exist
        if (!imageFile.exists()) {
            throw new IllegalArgumentException("File not found: " + imageFile);
        }

        // Call the DL4J classification
        AnimalOrNotDL4J.ClassificationResult result = AnimalOrNotDL4J.classifyImage(imageFile);

        // Print the model's raw best guesses
        // This helps us debug and understand what the network is seeing.
        System.out.println("Top " + result.topKIndices.length + " raw ImageNet guesses:");
        for (int i = 0; i < result.topKIndices.length; i++) {
            int idx = result.topKIndices[i];
            System.out.printf("  #%d  %-35s  %.4f%n", i + 1, result.labels.getLabel(idx), result.probabilities[idx]);
        }

        // Print the final simplified result
        System.out.println("\n✅ Final decision:");
        if (result.animalResult.isAnimal) {
            System.out.printf("  ANIMAL: %s (score=%.4f)%n", result.animalResult.animalName, result.animalResult.score);
        } else {
            System.out.printf("  NOT AN ANIMAL (best animal score=%.4f)%n", result.animalResult.score);
        }
    }
}
