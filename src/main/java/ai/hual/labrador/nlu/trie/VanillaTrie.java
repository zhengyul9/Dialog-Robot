package ai.hual.labrador.nlu.trie;

import ai.hual.labrador.nlu.Dict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VanillaTrie implements Trie {

    public VanillaTrieNode root;
    private boolean VERBOSE = false;

    private static Logger logger = LoggerFactory.getLogger(VanillaTrie.class);

    public VanillaTrie() {
        root = new VanillaTrieNode();
        root.setContent(new ArrayList<>());
    }

    public VanillaTrieNode getRoot() {
        return root;
    }

    @Override
    public List<Emit> parse(String text) {
        List<Emit> emits = new ArrayList<>();

        int length = text.length();
        int textPos = 0;
        while (textPos < length) {
            VanillaTrieNode crawl = this.root;
            String subText = text.substring(textPos, length);
            Emit emit = new Emit();
            HashMap<Object, VanillaTrieNode> children = crawl.getChildren();
            // find word with max length
            while (!children.isEmpty() && crawl.getLevel() < subText.length()) {
                char token = subText.charAt(crawl.getLevel());
                if (!children.containsKey(token))
                    break;
                crawl = children.get(token);
                children = crawl.getChildren();
                if (crawl.isLeaf()) {
                    emit.setLevel(crawl.getLevel());
                    int realEnd = emit.getLevel() + textPos;
                    emit.setStart(textPos);
                    emit.setEnd(realEnd);
                    emit.setContent(text.substring(textPos, realEnd));
                }
            }
            if (emit.getLevel() != 0 && emit.getContent() != null) { // has a meaningful emit
                textPos += emit.getEnd() - textPos;
                emits.add(emit);
            } else {    // no match, move on
                textPos++;
            }
        }
        return emits;
    }

    @Override
    public void insert(String word, Object content) {

        Dict dict = (Dict) content;

        // Find length of the given word
        int length = word.length();
        VanillaTrieNode crawl = root;

        // Traverse through all characters of given word
        for (int level = 0; level < length; level++) {

            HashMap<Object, VanillaTrieNode> children = crawl.getChildren();
            Object token = word.charAt(level);

            if (children.containsKey(token)) {// already a children for current character
                crawl = children.get(token);
            } else { // create a children
                VanillaTrieNode newNode = new VanillaTrieNode(token);
                newNode.setLevel(crawl.getLevel() + 1); // parent's level + 1
                children.put(token, newNode);
                crawl = newNode;
            }
        }

        // Set isLeaf true for last character
        // use add to make sure words with same char but different labels is added
        crawl.addContent(dict);
        crawl.setAsLeaf();
    }

    @Override
    public Object search(String word) {

        StringBuilder resultBuilder = new StringBuilder();  // result string
        int length = word.length();

        VanillaTrieNode crawl = this.root;
        // Iterate through all elements of input string and traverse
        // down the Trie
        for (int level = 0; level < length; level++) {
            // Find current character of str
            Object token = word.charAt(level);

            HashMap<Object, VanillaTrieNode> children = crawl.getChildren();

            // See if there is a Trie edge for the current character
            if (children.containsKey(token)) {
                resultBuilder.append(token);
                crawl = children.get(token); // Update crawl to move down in Trie
            } else if (children.containsKey('?')) {
                resultBuilder.append(token);
                crawl = children.get('?'); // Update crawl to move down in Trie
            } else
                return null;
        }

        if (crawl.isLeaf()) {
            if (VERBOSE)
                logger.debug(resultBuilder.toString());
            return crawl.getContent();
        } else
            return null;
    }
}
