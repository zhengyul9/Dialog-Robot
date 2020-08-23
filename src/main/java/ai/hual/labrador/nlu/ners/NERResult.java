package ai.hual.labrador.nlu.ners;

import java.util.HashMap;
import java.util.List;

public class NERResult {

    public static class Candidate {

        private int realStart;
        private int realEnd;
        private String pos;
        private String recognizer;
        private String text;
        private String entity;
        private String segments;

        public Candidate(int realStart, int realEnd, String pos, String recognizer,
                         String text, String entity, String segments) {
            this.realStart = realStart;
            this.realEnd = realEnd;
            this.pos = pos;
            this.recognizer = recognizer;
            this.text = text;
            this.entity = entity;
            this.segments = segments;
        }

        public int getRealStart() {
            return realStart;
        }

        public void setRealStart(int realStart) {
            this.realStart = realStart;
        }

        public int getRealEnd() {
            return realEnd;
        }

        public void setRealEnd(int realEnd) {
            this.realEnd = realEnd;
        }

        public String getPos() {
            return pos;
        }

        public void setPos(String pos) {
            this.pos = pos;
        }

        public String getRecognizer() {
            return recognizer;
        }

        public void setRecognizer(String recognizer) {
            this.recognizer = recognizer;
        }

        public String getEntity() {
            return entity;
        }

        public void setEntity(String entity) {
            this.entity = entity;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getSegments() {
            return segments;
        }

        public void setSegments(String segments) {
            this.segments = segments;
        }

        public String toString() {
            return   new HashMap<String,String>(){{
                    put("realStart", Integer.toString(realStart));
                    put("realEnd", Integer.toString(realEnd));
                    put("pos", pos);
                    put("recognizer", recognizer);
                    put("entity", entity);
                    put("text", text);
                    put("segments", segments);
                }}.toString();

        }
    }

    private String query;
    private List<Candidate> candidates;

    public NERResult(String text, List<Candidate> candidates) {
        this.query = text;
        this.candidates = candidates;
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public String getQuery() {
        return this.query;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public void setQuery(String text) {
        this.query = text;
    }
}
