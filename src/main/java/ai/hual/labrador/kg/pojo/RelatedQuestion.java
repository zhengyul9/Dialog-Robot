package ai.hual.labrador.kg.pojo;

public class RelatedQuestion {
    private String instanceIRI;
    private String propertyiri;
    private String propertyLabel;
    private String relatedQuestion;


    public RelatedQuestion(String instanceIRI, String propertyiri, String propertyLabel, String relatedQustions) {
        this.instanceIRI = instanceIRI;
        this.propertyiri = propertyiri;
        this.propertyLabel = propertyLabel;
        this.relatedQuestion = relatedQustions;


    }

    public String getInstanceIRI() {
        return instanceIRI;
    }

    public void setInstanceIRI(String instanceIRI) {
        this.instanceIRI = instanceIRI;
    }

    public String getPropertyiri() {
        return propertyiri;
    }

    public void setPropertyiri(String propertyiri) {
        this.propertyiri = propertyiri;
    }

    public String getPropertyLabel() {
        return propertyLabel;
    }

    public void setPropertyLabel(String propertyLabel) {
        this.propertyLabel = propertyLabel;
    }

    public String getRelatedQuestion() {
        return relatedQuestion;
    }

    public void setRelatedQuestion(String relatedQuestion) {
        this.relatedQuestion = relatedQuestion;
    }
}
