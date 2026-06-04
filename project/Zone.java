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
    private boolean satisfiesH1(int m) {
        return m > 0;
    }

    private boolean satisfiesH2(int m) {
        return satisfiesH1(m) && hasSecurity && hasHealth && hasEducation;
    }

    private boolean satisfiesH3(int m) {
        return satisfiesH2(m) && receivedLifestyle > 0;
    }

    private boolean satisfiesI1(int m) {
        return m > 0;
    }

    private boolean satisfiesI2(int m) {
        return satisfiesI1(m) && hasSecurity;
    }

    private boolean satisfiesI3(int m) {
        return satisfiesI2(m) && receivedPopulation > demand;
    }

    private boolean satisfiesC1(int m) {
        return m > 0;
    }

    private boolean satisfiesC2(int m) {
        return satisfiesC1(m) && hasSecurity;
    }

    private boolean satisfiesC3(int m) {
        return satisfiesC2(m) && receivedPopulation > demand && receivedGoods > demand;
    }
    public void computeNewState() {
        if (type == 'H' && (electricity == 0 || water == 0 || internet == 0)) {
            level = 0;
            currentOutput = 0;
            return;
        }
        if (type == 'I' && (electricity == 0 || water == 0)) {
            level = 0;
            currentOutput = 0;
            return;
        }
        if (type == 'C' && (electricity == 0 || water == 0 || internet == 0)) {
            level = 0;
            currentOutput = 0;
            return;
        }

        int m = 0;
        int targetLevel = 0;

        if (type == 'H') {
            m = Math.min(electricity, Math.min(water, internet));
            if (satisfiesH3(m)) targetLevel = 3;
            else if (satisfiesH2(m)) targetLevel = 2;
            else if (satisfiesH1(m)) targetLevel = 1;
        } else if (type == 'I') {
            m = Math.min(electricity, water);
            if (satisfiesI3(m)) targetLevel = 3;
            else if (satisfiesI2(m)) targetLevel = 2;
            else if (satisfiesI1(m)) targetLevel = 1;
        } else if (type == 'C') {
            m = Math.min(electricity, Math.min(water, internet));
            if (satisfiesC3(m)) targetLevel = 3;
            else if (satisfiesC2(m)) targetLevel = 2;
            else if (satisfiesC1(m)) targetLevel = 1;
        }

        if (targetLevel > level) level++;
        else if (targetLevel < level) level--;

        if (level == 0) {
            currentOutput = 0;
        } else if (level == 1) {
            currentOutput = m;
        } else if (level == 2) {
            currentOutput = 2 * m;
        } else if (level == 3) {
            if (type == 'H') currentOutput = 2 * m + receivedLifestyle;
            else if (type == 'I') currentOutput = 2 * m + receivedPopulation;
            else if (type == 'C') currentOutput = 2 * m + Math.min(receivedPopulation, receivedGoods);
        }
    }
}
