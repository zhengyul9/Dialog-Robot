package ai.hual.labrador.nlu.trie;

import ai.hual.labrador.nlu.Dict;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import java.util.HashMap;

public class PinyinTrieNode {
    protected Object token;   // token of node
    protected HashMap<Object, PinyinTrieNode> children;  // node's children nodes as map
    protected ListMultimap<String, Dict> content;
    protected int level;  // length from root
    protected boolean isLeaf; // tell if is leaf node
    protected PinyinTrieNode parent;

    protected PinyinTrieNode() {
        this(null, 0, ArrayListMultimap.create());
    }

    protected PinyinTrieNode(Object token) {

        this(token, 0, ArrayListMultimap.create());
    }

    protected PinyinTrieNode(Object token, int level, ListMultimap<String, Dict> content, PinyinTrieNode parent) {

        this.token = token;
        this.children = new HashMap<>();
        this.level = level;
        this.isLeaf = content != null;
        this.content = content;
        this.parent = parent;
    }

    protected PinyinTrieNode(Object token, int level, ListMultimap<String, Dict> content) {

        this.token = token;
        this.children = new HashMap<>();
        this.level = level;
        this.isLeaf = content.size() != 0;
        this.content = content;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    protected void setToken(Object token) {
        this.token = token;
    }

    protected Object getToken() {
        return this.token;
    }

    protected HashMap<Object, PinyinTrieNode> getChildren() {
        return this.children;
    }

    public void setChildren(HashMap<Object, PinyinTrieNode> children) {
        this.children = children;
    }

    protected int getLevel() {
        return this.level;
    }

    protected void setLevel(int level) {
        this.level = level;
    }

    protected ListMultimap<String, Dict> getContent() {
        return this.content;
    }

    protected void setContent(ListMultimap<String, Dict> content) {
        this.content = content;
    }

    protected void setAsLeaf() {
        this.isLeaf = true;
    }

    protected boolean isLeaf() {
        return this.isLeaf;
    }

    protected PinyinTrieNode getParent() {
        return this.parent;
    }

    protected void setParent(PinyinTrieNode parent) {
        this.parent = parent;
    }

    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("TOKEN: ").append(this.token).append(" | ");
        builder.append("LEVEL: ").append(this.level).append(" | ");
        builder.append("LEAF: ").append(this.isLeaf).append("\n");
        if (this.isLeaf)
            builder.append("with Content: ").append(this.content).append("\n");
        builder.append("with children: ").append(this.children.keySet()).append("\n");

        return builder.toString();
    }

    /**
     * Add content for {@link ai.hual.labrador.nlu.trie.PinyinTrie}
     *
     * @param value a {@link ai.hual.labrador.nlu.Dict}
     */
    void putContent(String key, Dict value) {
        content.put(key, value);
    }
}
