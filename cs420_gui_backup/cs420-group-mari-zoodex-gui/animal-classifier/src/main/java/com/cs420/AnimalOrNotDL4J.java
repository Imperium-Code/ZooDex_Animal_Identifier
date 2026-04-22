package com.cs420;

import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.api.Model;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.zoo.ZooModel;
import org.deeplearning4j.zoo.model.ResNet50;
import org.deeplearning4j.zoo.util.imagenet.ImageNetLabels;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.dataset.api.preprocessor.VGG16ImagePreProcessor;
import java.io.File;
import java.util.*;
/*
We will be using a lot of imported libraries, documentation:
DL4J / DataVec / ND4J Documentation Links

NativeImageLoader
https://deeplearning4j.konduit.ai/datavec/reference/image-loader
https://javadoc.io/doc/org.datavec/datavec-data-image/latest/org/datavec/image/loader/NativeImageLoader.html

Model
https://javadoc.io/doc/org.deeplearning4j/deeplearning4j-nn/latest/org/deeplearning4j/nn/api/Model.html

MultiLayerNetwork
https://deeplearning4j.konduit.ai/deeplearning4j/reference/multi-layer-network
https://javadoc.io/doc/org.deeplearning4j/deeplearning4j-nn/latest/org/deeplearning4j/nn/multilayer/MultiLayerNetwork.html

ComputationGraph
https://deeplearning4j.konduit.ai/deeplearning4j/reference/computation-graph
https://javadoc.io/doc/org.deeplearning4j/deeplearning4j-nn/latest/org/deeplearning4j/nn/graph/ComputationGraph.html

ZooModel
https://deeplearning4j.konduit.ai/models/model-zoo
https://javadoc.io/doc/org.deeplearning4j/deeplearning4j-zoo/latest/org/deeplearning4j/zoo/ZooModel.html

ResNet50
https://deeplearning4j.konduit.ai/models/model-zoo/resnet
https://javadoc.io/doc/org.deeplearning4j/deeplearning4j-zoo/latest/org/deeplearning4j/zoo/model/ResNet50.html

ImageNetLabels
https://javadoc.io/doc/org.deeplearning4j/deeplearning4j-zoo/latest/org/deeplearning4j/zoo/util/imagenet/ImageNetLabels.html

INDArray
https://deeplearning4j.konduit.ai/nd4j/reference/ndarray
https://javadoc.io/doc/org.nd4j/nd4j-api/latest/org/nd4j/linalg/api/ndarray/INDArray.html

ImagePreProcessingScaler
https://javadoc.io/doc/org.nd4j/nd4j-api/latest/org/nd4j/linalg/dataset/api/preprocessor/ImagePreProcessingScaler.html

VGG16ImagePreProcessor
https://javadoc.io/doc/org.nd4j/nd4j-api/latest/org/nd4j/linalg/dataset/api/preprocessor/VGG16ImagePreProcessor.html

Java File
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/io/File.html

Java util package, our basic methods
https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/package-summary.html
*/



public class AnimalOrNotDL4J {

    // Basic classifier settings:
    // TOP_K = how many of the model's best guesses we want to inspect
    // MIN_ANIMAL_SCORE = minimum combined confidence needed before we call something an animal
    private static final int TOP_K = 12;
    private static final double MIN_ANIMAL_SCORE = 0.12; // if too low -> "not an animal"



    // ------------------ Animal mapping logic ------------------

    public static ClassificationResult classifyImage(File imageFile) throws Exception {
        // 1) Load a pretrained ResNet50 model
        // ResNet50 is already trained on ImageNet, so we do not train our own model here.
        // We are reusing an existing model and asking it to classify our image.
        ZooModel<?> zooModel = ResNet50.builder().build();
        Model model = zooModel.initPretrained();

        // 2) Load and resize the image
        // ResNet50 expects images in 224x224 resolution with 3 color channels (RGB).
        int height = 224, width = 224, channels = 3;
        NativeImageLoader loader = new NativeImageLoader(height, width, channels);
        INDArray image = loader.asMatrix(imageFile);

        // 3) Preprocess the image
        // This step transforms pixel values into the format expected by the pretrained network.
        // We use the same preprocessing style commonly used for ImageNet/VGG-style models.
        VGG16ImagePreProcessor scaler = new VGG16ImagePreProcessor();
        scaler.transform(image);

        // 4) Run the image through the model to get prediction probabilities
        // The output is a vector of probabilities across the 1000 ImageNet classes.
        INDArray probs;

        // Different pretrained models can be stored as different DL4J model types,
        // so we check which one we got and run prediction the correct way.
        if (model instanceof ComputationGraph) {
            probs = ((ComputationGraph) model).outputSingle(image);
        } else if (model instanceof MultiLayerNetwork) {
            probs = ((MultiLayerNetwork) model).output(image);
        } else {
            throw new RuntimeException("Unknown model type: " + model.getClass());
        }

        // Convert the output probabilities into a regular Java double array
        // This makes it easier to sort and inspect.
        double[] p = probs.toDoubleVector();

        // 5) Decode the top-K labels
        // ImageNetLabels lets us convert class indices like 341 into readable names like "hog".
        ImageNetLabels labels = new ImageNetLabels();

        // Get the indices of the top K highest probabilities
        int[] topK = topKIndices(p, TOP_K);

        // 6) Convert ImageNet guesses into our simpler "animal or not" decision
        // Instead of trusting one exact label, we group similar labels together
        // and sum their probabilities.
        AnimalResult result = classifyAnimalFromTopK(topK, p, labels);

        return new ClassificationResult(result, topK, p, labels);
    }

    // This helper class stores our final simplified classification result.
    // isAnimal = true/false
    // animalName = best-matching animal category
    // score = combined probability score for that animal
    public static class AnimalResult {
        public boolean isAnimal;
        public String animalName;
        public double score;

        public AnimalResult(boolean isAnimal, String animalName, double score) {
            this.isAnimal = isAnimal;
            this.animalName = animalName;
            this.score = score;
        }
    }

    // Class to hold the full classification result including raw data for debugging
    public static class ClassificationResult {
        public AnimalResult animalResult;
        public int[] topKIndices;
        public double[] probabilities;
        public ImageNetLabels labels;

        public ClassificationResult(AnimalResult animalResult, int[] topKIndices, double[] probabilities, ImageNetLabels labels) {
            this.animalResult = animalResult;
            this.topKIndices = topKIndices;
            this.probabilities = probabilities;
            this.labels = labels;
        }
    }

    public static AnimalResult classifyAnimalFromTopK(int[] topK, double[] p, ImageNetLabels labels) {

        // This map defines our custom animal categories.
        // Key = final label we want to report
        // Value = keywords that count as evidence for that animal
        //
        // Example:
        // If ImageNet predicts "hog" or "wild boar",
        // we want to treat both as evidence for "pig".
        Map<String, List<String>> animalKeywords = new LinkedHashMap<>();

        // --- STARTER SET ---
        animalKeywords.put("elephant", Arrays.asList("elephant"));
        animalKeywords.put("cat", Arrays.asList(
                "tabby", "tiger cat", "persian cat", "siamese cat", "egyptian cat", "cat"));
        animalKeywords.put("dog", Arrays.asList(
                "retriever", "terrier", "shepherd", "pug", "beagle", "husky", "dog"));
        animalKeywords.put("horse", Arrays.asList(
                "horse", "sorrel", "zebra")); // note: zebra also appears separately below
        animalKeywords.put("bird", Arrays.asList(
                "bird", "hen", "cock", "eagle", "hawk", "ostrich", "penguin", "flamingo"));

        // --- EXPANDED SET ---
        animalKeywords.put("cow", Arrays.asList("cow", "ox", "bull", "bison"));
        animalKeywords.put("sheep", Arrays.asList("sheep", "ram"));
        animalKeywords.put("pig", Arrays.asList("hog", "boar", "pig"));
        animalKeywords.put("bear", Arrays.asList("bear", "brown bear", "black bear", "polar bear"));
        animalKeywords.put("lion", Arrays.asList("lion"));
        animalKeywords.put("tiger", Arrays.asList("tiger"));
        animalKeywords.put("giraffe", Arrays.asList("giraffe"));
        animalKeywords.put("zebra", Arrays.asList("zebra"));

        // Track the best-scoring animal category
        String bestAnimal = null;
        double bestScore = 0.0;

        // For each animal category, sum the probabilities of any top-K labels
        // whose names contain one of that animal's keywords.
        for (Map.Entry<String, List<String>> entry : animalKeywords.entrySet()) {
            String animal = entry.getKey();
            List<String> keys = entry.getValue();

            double score = 0.0;

            for (int idx : topK) {
                String lab = labels.getLabel(idx).toLowerCase();

                for (String k : keys) {
                    if (lab.contains(k.toLowerCase())) {
                        score += p[idx];
                        break; // stop checking more keywords once one matches
                    }
                }
            }

            // Keep whichever animal category has the highest total score
            if (score > bestScore) {
                bestScore = score;
                bestAnimal = animal;
            }
        }

        // Final rule:
        // If the best animal score is below our threshold,
        // we decide the image is "not an animal."
        if (bestAnimal == null || bestScore < MIN_ANIMAL_SCORE) {
            return new AnimalResult(false, null, bestScore);
        }

        return new AnimalResult(true, bestAnimal, bestScore);
    }

    // ------------------ Helpers ------------------

    public static int[] topKIndices(double[] arr, int k) {

        // Build an array of indices: [0, 1, 2, ..., arr.length-1]
        Integer[] idx = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) {
            idx[i] = i;
        }

        // Sort the indices based on the probability values in descending order
        // Highest probability comes first
        Arrays.sort(idx, (a, b) -> Double.compare(arr[b], arr[a]));

        // Take only the first k indices, or fewer if the array is smaller
        int n = Math.min(k, idx.length);
        int[] out = new int[n];
        for (int i = 0; i < n; i++) {
            out[i] = idx[i];
        }

        return out;
    }
}