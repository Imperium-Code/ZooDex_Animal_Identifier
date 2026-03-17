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

    // 🟦 Tune these
    private static final int TOP_K = 12;
    private static final double MIN_ANIMAL_SCORE = 0.12; // if too low -> "not an animal"

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java AnimalOrNotDL4J /path/to/image.jpg");
            System.exit(1);
        }

        File imageFile = new File(args[0]);
        if (!imageFile.exists()) throw new IllegalArgumentException("File not found: " + imageFile);

        // 1) Pretrained ResNet50 (ImageNet)
        //ZooModel<ComputationGraph> zooModel = ResNet50.builder().build();
        //ComputationGraph model = zooModel.initPretrained(); // downloads weights first time

        ZooModel<?> zooModel = ResNet50.builder().build();
        Model model = zooModel.initPretrained();
        
        // 2) Load image (even if 128x128, resize to 224x224 for ResNet50)
        int height = 224, width = 224, channels = 3;
        NativeImageLoader loader = new NativeImageLoader(height, width, channels);
        INDArray image = loader.asMatrix(imageFile);
        
        
        VGG16ImagePreProcessor scaler = new VGG16ImagePreProcessor();
        scaler.transform(image);
       
        
        // 4) Predict (1000 ImageNet classes)
        INDArray probs;

        if (model instanceof ComputationGraph) {
            probs = ((ComputationGraph) model).outputSingle(image);
        } else if (model instanceof MultiLayerNetwork) {
            probs = ((MultiLayerNetwork) model).output(image);
        } else {
            throw new RuntimeException("Unknown model type: " + model.getClass());
        }
        
        
        double[] p = probs.toDoubleVector();

        // 5) Decode top-K labels
        ImageNetLabels labels = new ImageNetLabels();
        int[] topK = topKIndices(p, TOP_K);

        System.out.println("Top " + TOP_K + " raw ImageNet guesses:");
        for (int i = 0; i < topK.length; i++) {
            int idx = topK[i];
            System.out.printf("  #%d  %-35s  %.4f%n", i + 1, labels.getLabel(idx), p[idx]);
        }

        // 6) Animal-vs-not decision using keyword mapping
        AnimalResult result = classifyAnimalFromTopK(topK, p, labels);

        System.out.println("\n✅ Final decision:");
        if (result.isAnimal) {
            System.out.printf("  ANIMAL: %s (score=%.4f)%n", result.animalName, result.score);
        } else {
            System.out.printf("  NOT AN ANIMAL (best animal score=%.4f)%n", result.score);
        }
    }

    // ------------------ Animal mapping logic ------------------

    private static class AnimalResult {
        boolean isAnimal;
        String animalName;   // e.g. "elephant"
        double score;        // summed probability mass from matching labels
        AnimalResult(boolean isAnimal, String animalName, double score) {
            this.isAnimal = isAnimal;
            this.animalName = animalName;
            this.score = score;
        }
    }

    private static AnimalResult classifyAnimalFromTopK(int[] topK, double[] p, ImageNetLabels labels) {

        // 🟧 Start with "just a few animals" by keeping only a few entries here.
        // You can uncomment/extend anytime. 

        Map<String, List<String>> animalKeywords = new LinkedHashMap<>();

        // --- STARTER SET (few animals) ---
        animalKeywords.put("elephant", Arrays.asList("elephant"));
        animalKeywords.put("cat", Arrays.asList("tabby", "tiger cat", "persian cat", "siamese cat", "egyptian cat", "cat"));
        animalKeywords.put("dog", Arrays.asList("retriever", "terrier", "shepherd", "pug", "beagle", "husky", "dog"));
        animalKeywords.put("horse", Arrays.asList("horse", "sorrel", "zebra")); // (zebra is separate too—see below)
        animalKeywords.put("bird", Arrays.asList("bird", "hen", "cock", "eagle", "hawk", "ostrich", "penguin", "flamingo"));

        // --- EASY EXPANSION (10+ animals) ---
         animalKeywords.put("cow", Arrays.asList("cow", "ox", "bull", "bison"));
         animalKeywords.put("sheep", Arrays.asList("sheep", "ram"));
         animalKeywords.put("pig", Arrays.asList("hog", "boar", "pig"));
         animalKeywords.put("bear", Arrays.asList("bear", "brown bear", "black bear", "polar bear"));
         animalKeywords.put("lion", Arrays.asList("lion"));
         animalKeywords.put("tiger", Arrays.asList("tiger"));
         animalKeywords.put("giraffe", Arrays.asList("giraffe"));
         animalKeywords.put("zebra", Arrays.asList("zebra"));

        // Score each animal by summing probability of any matching labels found in topK
        String bestAnimal = null;
        double bestScore = 0.0;

        for (Map.Entry<String, List<String>> entry : animalKeywords.entrySet()) {
            String animal = entry.getKey();
            List<String> keys = entry.getValue();

            double score = 0.0;
            for (int idx : topK) {
                String lab = labels.getLabel(idx).toLowerCase();
                for (String k : keys) {
                    if (lab.contains(k.toLowerCase())) {
                        score += p[idx];
                        break;
                    }
                }
            }

            if (score > bestScore) {
                bestScore = score;
                bestAnimal = animal;
            }
        }

        // Decision: if our best animal score is too low => "not an animal"
        if (bestAnimal == null || bestScore < MIN_ANIMAL_SCORE) {
            return new AnimalResult(false, null, bestScore);
        }
        return new AnimalResult(true, bestAnimal, bestScore);
    }

    // ------------------ Helpers ------------------

    private static int[] topKIndices(double[] arr, int k) {
        Integer[] idx = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++) idx[i] = i;

        Arrays.sort(idx, (a, b) -> Double.compare(arr[b], arr[a])); // desc

        int n = Math.min(k, idx.length);
        int[] out = new int[n];
        for (int i = 0; i < n; i++) out[i] = idx[i];
        return out;
    }
}