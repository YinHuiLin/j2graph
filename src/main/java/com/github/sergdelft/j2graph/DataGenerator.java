package com.github.sergdelft.j2graph;

import com.github.sergdelft.j2graph.ast.JDT;
import com.github.sergdelft.j2graph.graph.*;
import com.github.sergdelft.j2graph.walker.GraphWalker;
import com.github.sergdelft.j2graph.walker.json.JsonVisitor;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataGenerator {

    enum Split {
        TRAIN,
        DEV,
        EVAL
    }

    public void run() {
        try {
            iterateFiles("C:\\Users\\Kasutaja\\DATASET\\duplicated\\java-small\\training", Split.TRAIN);
            iterateFiles("C:\\Users\\Kasutaja\\DATASET\\duplicated\\java-small\\validation", Split.DEV);
            iterateFiles("C:\\Users\\Kasutaja\\DATASET\\duplicated\\java-small\\test", Split.EVAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iterateFiles(String path, Split split) throws IOException {
        System.out.println("Starting to preprocess files for " + split.name().toLowerCase());

        BufferedOutputStream processedDataStream = new BufferedOutputStream(new FileOutputStream(split.name().toLowerCase() + ".txt"));
        PrintWriter processedDataWriter = new PrintWriter(processedDataStream, true, StandardCharsets.UTF_8);

        BufferedOutputStream vocabStream = new BufferedOutputStream(new FileOutputStream(split.name().toLowerCase() + "_vocab.txt"));
        PrintWriter vocabWriter = new PrintWriter(vocabStream, true, StandardCharsets.UTF_8);

        Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .forEach(filePath -> processFile(split, processedDataWriter, vocabWriter, filePath));

        processedDataWriter.close();
        processedDataStream.close();
        vocabWriter.close();
        vocabStream.close();
    }

    private void processFile(Split split, PrintWriter processedDataWriter, PrintWriter vocabWriter, Path filePath) {
        GraphWalker graphWalker = new GraphWalker();
        String sourceCode = loadSourceCode(filePath.toString());
        try {
            ClassGraph graph = new JDT().parse(sourceCode);
            if (graph != null) {
                JsonVisitor jsonVisitor = new JsonVisitor();
                graphWalker.accept(graph, jsonVisitor);
                if (split.equals(Split.TRAIN) && !jsonVisitor.getCorrectAndBuggyPairs().isEmpty()) {
                    saveTokensToFile(vocabWriter, graph);
                }
                for (Pair<JsonObject, JsonObject> pair : jsonVisitor.getCorrectAndBuggyPairs()) {
                    processedDataWriter.println(pair.getLeft());
                    processedDataWriter.println(pair.getRight());
                    processedDataWriter.flush();
                }
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Couldn't parse code");
        }


    }

    protected String loadSourceCode(String fixture) {
        try {
            return new String (Files.readAllBytes(Paths.get(fixture)));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveTokensToFile(PrintWriter vocabWriter, ClassGraph graph) {
        for (MethodGraph methodGraph : graph.getMethods()) {
            vocabWriter.println("");
            methodGraph.getTokens().forEach(t -> vocabWriter.print(t.getTokenName() + " "));
            vocabWriter.println("");
            methodGraph.getSymbols().forEach(s -> vocabWriter.print(s.getSymbol() + " "));
            vocabWriter.println("");
            methodGraph.getVocabulary().forEach(v -> vocabWriter.print(v.getWord() + " "));
            vocabWriter.println("");
            methodGraph.getNonTerminals().forEach(nt -> vocabWriter.print(nt.getName() + " "));
            vocabWriter.flush();
        }
    }
}
