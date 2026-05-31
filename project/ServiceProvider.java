package project;

public class ServiceProvider  extends Cell {
    private int range = 5;

    public ServiceProvider(int x, int y, char type) {
        super(x, y, type);
        if (type == 'S') {
            this.range = 4;
        } else if (type == 'D') {
            this.range = 3;
        } else if (type == 'F') {
            this.range = 5;
        }
    }

    public int getRange() {
        return range;
    }
}

