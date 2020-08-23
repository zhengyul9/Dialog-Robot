package ai.hual.labrador.kg;

import ai.hual.labrador.exceptions.KnowledgeException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A knowledge util help to handle result.
 * Created by Dai Wentao on 2017/5/9.
 */
public class KnowledgeUtil {

    public final ObjectMapper mapper;

    public KnowledgeUtil() {
        this(new ObjectMapper());
    }

    public KnowledgeUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public SelectResult parseJSONSelectResult(String result) {
        try {
            return mapper.readValue(result, SelectResult.class);
        } catch (IOException e) {
            throw new KnowledgeException(e);
        }
    }

    public List<String> parseJSONSelectVarResult(String result, String varName) {
        return parseJSONSelectResult(result).getBindings().stream()
                .map(binding -> binding.value(varName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
