package ai.hual.labrador.dm.java;

/**
 * This class containing the result of transition.
 */
public class TransitionResult {

    /**
     * The resulting leaf state.
     */
    private DMState leafState;

    /**
     * The score of this transition process.
     */
    private Float score;

    public TransitionResult(DMState leafState, Float score) {
        this.leafState = leafState;
        this.score = score;
    }

    public DMState getLeafState() {
        return leafState;
    }

    public void setLeafState(DMState leafState) {
        this.leafState = leafState;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }
}
