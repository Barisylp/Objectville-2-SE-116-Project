package project;
import java.io.*;
import java.util.*;

public class ObjectVilleGame {
    private Cell[][] grid;
    private int rows, cols;
    private int pooledPopulation = 0, pooledGoods = 0, pooledLifestyle = 0;

    public ObjectVilleGame(String filename) throws IOException {
        String path = filename;
        File f = new File(path);
        if (!f.exists()) {
            path = "src/" + filename;
            f = new File(path);
        }
        if (!f.exists()) {
            throw new MapException("Map file not found: " + filename);
        }
        loadMap(path);
    }
    private void loadMap(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) lines.add(line);
            }
        } catch (FileNotFoundException e) {
            throw new MapException("Map file not found: " + filename);
        }

        if (lines.isEmpty()) {
            throw new MapException("This file is empty: ");
        }

        rows = lines.size();
        String firstLine = lines.get(0);
        if (firstLine.contains(" ")) {
            cols = firstLine.split(" ").length;
        } else {
            cols = firstLine.length();
        }
        grid = new Cell[rows][cols];
    }
}