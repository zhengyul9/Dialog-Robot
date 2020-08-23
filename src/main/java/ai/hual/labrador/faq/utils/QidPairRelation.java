package ai.hual.labrador.faq.utils;

import java.util.List;
import java.util.Map;

public class QidPairRelation {
    private Integer size;
    private Double score;
    private Integer grade;
    private Map<Integer, List<Integer>> mapping;
    private IntegerPair parentId;

    public QidPairRelation() {
    }

    public QidPairRelation(Integer size, Map<Integer, List<Integer>> mapping) {
        this.size = size;
        this.mapping = mapping;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Map<Integer, List<Integer>> getMapping() {
        return mapping;
    }

    public void setMapping(Map<Integer, List<Integer>> mapping) {
        this.mapping = mapping;
    }

    public IntegerPair getParentId() {
        return parentId;
    }

    public void setParentId(IntegerPair parentId) {
        this.parentId = parentId;
    }


    @Override
    public String toString() {
        return " { size: " + size + "\t" + "mapping: " + mapping + "\t" + "atanValueSum: " + score + "\t" + "parentId: " + parentId + " }\n";
    }

}
