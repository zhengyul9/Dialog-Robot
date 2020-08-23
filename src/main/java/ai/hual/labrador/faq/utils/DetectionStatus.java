package ai.hual.labrador.faq.utils;

public enum DetectionStatus {
    NOTBAD(0), EMERGENT(1);
    private int level;

    DetectionStatus(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }


}
