package ai.hual.labrador.nlu.trie;

import java.util.HashMap;

public class PlainOldTrieNode {
    protected Object token;   // token of node
    protected HashMap<Object, PlainOldTrieNode> children;  // node's children nodes as map
    protected int level;  // length from root
    protected boolean isLeaf; // tell if is leaf node
    protected Object content;
    protected PlainOldTrieNode parent;

    protected PlainOldTrieNode() {
        this(null, 0, null);
    }

    protected PlainOldTrieNode(Object token) {

        this(token, 0, null);
    }

    protected PlainOldTrieNode(Object token, int level, Object content, PlainOldTrieNode parent) {
        this.token = token;
        this.children = new HashMap<>();
        this.level = level;
        this.isLeaf = content != null;
        this.content = content;
        this.parent = parent;
    }

    protected PlainOldTrieNode(Object token, int level, Object content) {

        this.token = token;
        this.children = new HashMap<>();
        this.level = level;
        this.isLeaf = content != null;
        this.content = content;
    }

    protected void setToken(Object token) {
        this.token = token;
    }

    protected Object getToken() {
        return this.token;
    }

    protected HashMap<Object, PlainOldTrieNode> getChildren() {
        return this.children;
    }

    protected int getLevel() {
        return this.level;
    }

    protected void setLevel(int level) {
        this.level = level;
    }

    protected Object getContent() {
        return this.content;
    }

    protected void setContent(Object content) {
        this.content = content;
    }

    protected void setAsLeaf() {
        this.isLeaf = true;
    }

    protected boolean isLeaf() {
        return this.isLeaf;
    }

    protected PlainOldTrieNode getParent() {
        return this.parent;
    }

    protected void setParent(PlainOldTrieNode parent) {
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

}
