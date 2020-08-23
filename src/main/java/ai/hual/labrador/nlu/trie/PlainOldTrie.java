package ai.hual.labrador.nlu.trie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class PlainOldTrie implements Trie {

    public PlainOldTrieNode root;
    private boolean VERBOSE = false;

    private static Logger logger = LoggerFactory.getLogger(PlainOldTrie.class);

    public PlainOldTrie() {
        this.root = new PlainOldTrieNode();
    }

    public PlainOldTrieNode getRoot() {
        return this.root;
    }

    @Override
    public List<Emit> parse(String text) {
        return null;
    }

    @Override
    public void insert(String word, Object content) {

        // Find length of the given word
        int length = word.length();
        PlainOldTrieNode crawl = this.root;

        // Traverse through all characters of given word
        for (int level = 0; level < length; level++) {

            HashMap<Object, PlainOldTrieNode> children = crawl.getChildren();
            Object token = word.charAt(level);

            if (children.containsKey(token)) {// already a children for current character
                crawl = children.get(token);
            } else { // create a children
                PlainOldTrieNode newNode = new PlainOldTrieNode(token);
                newNode.setLevel(crawl.getLevel() + 1); // parent's level + 1
                children.put(token, newNode);
                crawl = newNode;
            }
        }

        // Set isLeaf true for last character
        crawl.setContent(content);
        crawl.setAsLeaf();
    }

    @Override
    public Object search(String word) {

        StringBuilder resultBuilder = new StringBuilder();  // result string
        int length = word.length();

        PlainOldTrieNode crawl = this.root;
        PlainOldTrieNode prevLeaf = new PlainOldTrieNode("");
        // Iterate through all elements of input string and traverse
        // down the Trie
        for (int level = 0; level < length; level++) {

            // Find current character of str
            Object token = word.charAt(level);

            HashMap<Object, PlainOldTrieNode> children = crawl.getChildren();

            // See if there is a Trie edge for the current character
            String tokenStr = token.toString(); // has to use this when hpTrie is read out from file
            if (children.containsKey(tokenStr)) {
                resultBuilder.append(tokenStr);
                crawl = children.get(tokenStr); // Update crawl to move down in Trie
                if (crawl.isLeaf())
                    prevLeaf = crawl;
            } else
                return prevLeaf.getContent();
        }

        if (crawl.isLeaf()) {
            if (VERBOSE)
                logger.debug(resultBuilder.toString());
            return crawl.getContent();
        } else
            return prevLeaf.getContent();
    }
}
