package ai.hual.labrador.utils;

public class PositionTuple {

    /**
     * start position
     */
    private int start;

    /**
     * end position
     */
    private int end;

    public PositionTuple(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
