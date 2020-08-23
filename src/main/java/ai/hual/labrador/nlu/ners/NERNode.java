package ai.hual.labrador.nlu.ners;


import java.util.HashSet;
import java.util.Set;


public class NERNode {
    private String nerName;
    private Set<NERNode> adjoin;
    private int inDegree;
    private int outDegree;
    private boolean active;
    private NERModule nerModule;

    public NERNode(String className, NERModule nerModule) {
        this.nerName = className;
        this.adjoin = new HashSet<>();
        this.inDegree = 0;
        this.outDegree = 0;
        this.active = false;
        this.nerModule = nerModule;
    }

    public String getNerName() {
        return this.nerName;
    }

    public void addEdge(NERNode to) {
        enable();
        this.adjoin.add(to);
        this.outDegree = this.adjoin.size();
        to.autoaddIndegree();
    }

    public void autoaddIndegree() {
        ++this.inDegree;
    }

    public int getInDegree() {
        return this.inDegree;
    }

    public int getOutDegree() {
        return this.outDegree;
    }

    public void enable() {
        this.active = true;
    }

    public void disable() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void setNerModule(NERModule b) {
        this.nerModule = b;
    }

    public NERModule getNerModule() {
        return this.nerModule;
    }

    public Set<NERNode> getAdjoin() {
        return this.adjoin;
    }
}
