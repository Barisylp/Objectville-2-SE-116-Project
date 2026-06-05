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

    public void runSimulation(int ticks) {
        for (int t = 1; t <= ticks; t++) {
            System.out.println("Tick " + t);
            provideServices();
            distributeUtilities();
            distributeResources();
            updateAndReport();
        }
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
    private void distributeUtilities() {
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

                    while (!queue.isEmpty() && remaining > 0) {
                        Cell current = queue.poll();
                        int[] dx = {-1, 1, 0, 0};
                        int[] dy = {0, 0, -1, 1};

                        for (int d = 0; d < 4; d++) {
                            int nx = current.x + dx[d], ny = current.y + dy[d];
                            if (nx >= 0 && nx < rows && ny >= 0 && ny < cols) {
                                Cell neighbor = grid[nx][ny];
                                if (!visited.contains(neighbor) && neighbor.isConnectable()) {
                                    if (neighbor instanceof Zone) {
                                        Zone z = (Zone) neighbor;
                                        int taken = 0;
                                        if (p.getType() == 'P') {
                                            int needed = z.demand - z.electricity;
                                            if (needed > 0) {
                                                taken = Math.min(needed, remaining);
                                                z.electricity += taken;
                                                remaining -= taken;
                                            }
                                        } else if (p.getType() == 'W') {
                                            int needed = z.demand - z.water;
                                            if (needed > 0) {
                                                taken = Math.min(needed, remaining);
                                                z.water += taken;
                                                remaining -= taken;
                                            }
                                        } else if (p.getType() == 'T') {
                                            int needed = z.demand - z.internet;
                                            if (needed > 0) {
                                                taken = Math.min(needed, remaining);
                                                z.internet += taken;
                                                remaining -= taken;
                                            }
                                        }
                                        if (taken > 0) {
                                            System.out.println(z.getTypeName() + " at (" + nx + "," + ny + ") received " + taken + " " + utilityName);
                                        }
                                    }
                                    visited.add(neighbor);
                                    queue.add(neighbor);
                                }
                            }
                        }
                    }
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
    private void updateAndReport() {
        pooledPopulation = 0;
        pooledGoods = 0;
        pooledLifestyle = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] instanceof Zone) {
                    Zone z = (Zone) grid[i][j];
                    int oldLevel = z.level;
                    z.computeNewState();

                    System.out.println(z.getTypeName() + " at (" + i + "," + j + ") generated " + z.currentOutput + " " + z.getOutputName());

                    if (z.level > oldLevel) {
                        System.out.println(z.getTypeName() + " at (" + i + "," + j + ") levels up from " + oldLevel + " to " + z.level);
                    }

                    if (z.getType() == 'H') pooledPopulation += z.currentOutput;
                    else if (z.getType() == 'I') pooledGoods += z.currentOutput;
                    else if (z.getType() == 'C') pooledLifestyle += z.currentOutput;

                    z.updateDemand();
                    z.resetTickData();
                }
            }
        }
    }
    public static void main(String[] args) {
        String fileName;
        int ticks = 10;
        try {
            if (args.length >= 2) {
                fileName = args[0];
                ticks = Integer.parseInt(args[1]);
                if (ticks <= 0) {
                    throw new MapException("Tick count must be greater than 0.");
                }
            } else if (args.length == 1) {
                fileName = args[0];
            } else {
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter your map file name: ");
                fileName = scanner.nextLine();
                System.out.print("Please enter the simulation tick number: ");
                String ticksStr = scanner.nextLine();
                ticks = Integer.parseInt(ticksStr);
                if (ticks <= 0) {
                    throw new MapException("Tick count must be greater than 0.");
                }
            }
            ObjectVilleGame game = new ObjectVilleGame(fileName);
            game.runSimulation(ticks);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid tick number");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}