package ai.hual.labrador.nlu.ners;

import java.util.List;

public class NERResource {
    private String name;
    private List<?> contents;

    public NERResource(String name,List<?> contents){
        this.name = name;
        this.contents = contents;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<?> getContents() {
        return contents;
    }

    public void setContents(List<?> contents) {
        this.contents = contents;
    }
}
