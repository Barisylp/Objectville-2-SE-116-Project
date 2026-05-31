package project;

public class UtilityProvider extends Cell {
    private int capacity = 100;

    public UtilityProvider(int x, int y, char type) {
        super(x, y, type);
    }

    public int getCapacity() {
        return capacity;
    }
}

