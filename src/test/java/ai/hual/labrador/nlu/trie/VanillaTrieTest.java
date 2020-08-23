package ai.hual.labrador.nlu.trie;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VanillaTrieTest {
    private static VanillaTrie trie;

    @BeforeAll
    static void setUp() {
        trie = new VanillaTrie();
    }

    @Test
    void testParse() {
        List<String> words = Arrays.asList("sent", "sen", "nte", "nce", "nc");
        words.forEach(w -> trie.insert(w, null));
        String text = "sentence";
        List<Emit> emits = trie.parse(text);

        assertEquals(2, emits.size());
        assertEquals("sent", emits.get(0).getContent());
        assertEquals(0, emits.get(0).getStart());
        assertEquals(4, emits.get(0).getEnd());

        assertEquals("nce", emits.get(1).getContent());
        assertEquals(5, emits.get(1).getStart());
        assertEquals(8, emits.get(1).getEnd());
    }
}