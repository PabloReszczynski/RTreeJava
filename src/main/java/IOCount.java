public class IOCount {
    private static IOCount instance = null;
    private long writes;
    private long reads;
    protected IOCount() {
        writes = 0;
        reads = 0;
    }
    public static IOCount getInstance() {
        if (instance == null) {
            instance = new IOCount();
        }
        return instance;
    }

    public void write() {
        writes++;
    }

    public void read() {
        reads++;
    }

    public void reset() {
        writes = 0;
        reads = 0;
    }

    public long getWrites() {
        return writes;
    }

    public long getReads() {
        return reads;
    }
}
