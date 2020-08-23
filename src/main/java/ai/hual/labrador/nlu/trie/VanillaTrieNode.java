package ai.hual.labrador.nlu.trie;

import ai.hual.labrador.nlu.Dict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VanillaTrieNode {
    protected Object token;   // token of node
    protected HashMap<Object, VanillaTrieNode> children;  // node's children nodes as map
    protected List<Dict> content;
    protected int level;  // length from root
    protected boolean isLeaf; // tell if is leaf node
    protected VanillaTrieNode parent;

    protected VanillaTrieNode() {
        this(null, 0, new ArrayList<>());
    }

    protected VanillaTrieNode(Object token) {

        this(token, 0, new ArrayList<>());
    }

    protected VanillaTrieNode(Object token, int level, List<Dict> content, VanillaTrieNode parent) {
        this.token = token;
        this.children = new HashMap<>();
        this.level = level;
        this.isLeaf = content != null;
        this.content = content;
        this.parent = parent;
    }

    protected VanillaTrieNode(Object token, int level, List<Dict> content) {

        this.token = token;
        this.children = new HashMap<>();
        this.level = level;
        this.isLeaf = content.size() != 0;
        this.content = content;
    }

    public Object getToken() {
        return token;
    }

    public void setToken(Object token) {
        this.token = token;
    }

    public HashMap<Object, VanillaTrieNode> getChildren() {
        return children;
    }

    public void setChildren(HashMap<Object, VanillaTrieNode> children) {
        this.children = children;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    protected void setAsLeaf() {
        this.isLeaf = true;
    }

    protected List<Dict> getContent() {
        return this.content;
    }

    protected void setContent(List<Dict> content) {
        this.content = content;
    }

    protected VanillaTrieNode getParent() {
        return this.parent;
    }

    protected void setParent(VanillaTrieNode parent) {
        this.parent = parent;
    }

    /**
     * Add content for {@link ai.hual.labrador.nlu.trie.VanillaTrie}
     *
     * @param value a {@link ai.hual.labrador.nlu.Dict}
     */
    void addContent(Dict value) {
        content.add(value);
    }
}
