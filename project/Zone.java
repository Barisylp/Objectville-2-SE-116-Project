package project;
public class Zone extends Cell implements IConnectable {
    public int level = 0;
    public int currentOutput = 0;
    public int demand = 1;
    public int electricity = 0, water = 0, internet = 0;
    public boolean hasSecurity = false, hasHealth = false, hasEducation = false;
    public int receivedPopulation = 0, receivedGoods = 0, receivedLifestyle = 0;

    public Zone(int x, int y, char type) {
        super(x, y, type);
    }
    public String getTypeName() {
        if (type == 'H') return "House";
        if (type == 'I') return "Industrial";
        if (type == 'C') return "Commercial";
        return "Unknown";
    }
    public String getOutputName() {
        if (type == 'H') return "population";
        if (type == 'I') return "goods";
        if (type == 'C') return "lifestyle";
        return "unknown";
    }
    public void resetTickData() {
        electricity = 0; water = 0; internet = 0;
        hasSecurity = false; hasHealth = false; hasEducation = false;
        receivedPopulation = 0; receivedGoods = 0; receivedLifestyle = 0;
    }
    public void updateDemand() {
        this.demand = Math.max(1, currentOutput);
    }
    @Override
    public boolean isConnectable() { return true; }
}
