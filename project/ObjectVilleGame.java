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
        for (int i = 0; i < rows; i++) {
            String line = lines.get(i);
            String[] cells;
            if (line.contains(" ")) {
                cells = line.split(" ");
            } else {
                cells = new String[line.length()];
                for (int k = 0; k < line.length(); k++) {
                    cells[k] = String.valueOf(line.charAt(k));
                }
            }

            for (int j = 0; j < cols; j++) {
                if (j >= cells.length) {
                    grid[i][j] = new Empty(i, j);
                    continue;
                }
                char t = cells[j].charAt(0);
                switch (t) {
                    case 'H':
                    case 'I':
                    case 'C':
                        grid[i][j] = new Zone(i, j, t);
                        break;
                    case 'P':
                    case 'W':
                    case 'T':
                        grid[i][j] = new UtilityProvider(i, j, t);
                        break;
                    case 'F':
                    case 'D':
                    case 'S':
                        grid[i][j] = new ServiceProvider(i, j, t);
                        break;
                    case 'R':
                        grid[i][j] = new Road(i, j);
                        break;
                    case 'E':
                        grid[i][j] = new Empty(i, j);
                        break;
                    default:
                        throw new MapException("Unknown cell type:" + t);
                }
            }
        }
    }
    private String getUtilityName(char type) {
        if (type == 'P') return "electricity";
        if (type == 'W') return "water";
        if (type == 'T') return "internet";
        return "unknown";
    }
    private String getServiceName(char type) {
        if (type == 'F') return "security";
        if (type == 'D') return "health";
        if (type == 'S') return "education";
        return "unknown";
    }

    public void provideServices() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] instanceof ServiceProvider) {
                    ServiceProvider sp = (ServiceProvider) grid[i][j];
                    int r = sp.getRange();
                    String serviceName = getServiceName(sp.getType());
                    for (int si = 0; si < rows; si++) {
                        for (int sj = 0; sj < cols; sj++) {
                            if (grid[si][sj] instanceof Zone) {
                                Zone z = (Zone) grid[si][sj];
                                if (Math.abs(sp.x - si) + Math.abs(sp.y - sj) <= r) {
                                    if (sp.getType() == 'S' || sp.getType() == 'D') {
                                        if (z.getType() != 'H') continue;
                                    }
                                    if (sp.getType() == 'F') z.hasSecurity = true;
                                    if (sp.getType() == 'D') z.hasHealth = true;
                                    if (sp.getType() == 'S') z.hasEducation = true;
                                    System.out.println(z.getTypeName() + " at (" + si + "," + sj + ") received " + serviceName + " service");
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    public void distributeUtilities() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] instanceof UtilityProvider) {
                    UtilityProvider p = (UtilityProvider) grid[i][j];
                    int remaining = p.getCapacity();
                    String utilityName = getUtilityName(p.getType());
                    Queue<Cell> queue = new LinkedList<>();
                    Set<Cell> visited = new HashSet<>();
                    queue.add(p);
                    visited.add(p);


                }
            }
        }
    }
    public void distributeResources() {
        List<Zone> houses = new ArrayList<>(), industrials = new ArrayList<>(), commercials = new ArrayList<>();
        for (Cell[] row : grid) {
            for (Cell c : row) {
                if (c instanceof Zone) {
                    if (c.getType() == 'H') houses.add((Zone) c);
                    else if (c.getType() == 'I') industrials.add((Zone) c);
                    else if (c.getType() == 'C') commercials.add((Zone) c);
                }
            }
        }

        int popPerZone = 0, goodsPerZone = 0, lifestylePerZone = 0;

        if (!industrials.isEmpty() || !commercials.isEmpty()) {
            int totalConsumers = industrials.size() + commercials.size();
            popPerZone = pooledPopulation / totalConsumers;
            for (Zone z : industrials) z.receivedPopulation = popPerZone;
            for (Zone z : commercials) z.receivedPopulation = popPerZone;
        }

        if (!commercials.isEmpty()) {
            goodsPerZone = pooledGoods / commercials.size();
            for (Zone z : commercials) z.receivedGoods = goodsPerZone;
        }

        if (!houses.isEmpty()) {
            lifestylePerZone = pooledLifestyle / houses.size();
            for (Zone z : houses) z.receivedLifestyle = lifestylePerZone;
        }

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] instanceof Zone) {
                    Zone z = (Zone) grid[i][j];
                    if (z.getType() == 'I' && z.receivedPopulation > 0) {
                        System.out.println(z.getTypeName() + " at (" + i + "," + j + ") received " + z.receivedPopulation + " population");
                    } else if (z.getType() == 'C') {
                        if (z.receivedPopulation > 0)
                            System.out.println(z.getTypeName() + " at (" + i + "," + j + ") received " + z.receivedPopulation + " population");
                        if (z.receivedGoods > 0)
                            System.out.println(z.getTypeName() + " at (" + i + "," + j + ") received " + z.receivedGoods + " goods");
                    } else if (z.getType() == 'H' && z.receivedLifestyle > 0) {
                        System.out.println(z.getTypeName() + " at (" + i + "," + j + ") received " + z.receivedLifestyle + " lifestyle");
                    }
                }
            }
        }
    }
}