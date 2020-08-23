package ai.hual.labrador.nlu.trie;

import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.pinyin.hanyupinyin.ChinesePinyinTuple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PlainOldTrieSerDeser {

    /**
     * Read out a plainOldTrie from file in pre-order.
     *
     * @param file Name of the trie file.
     */
    public void fileToTrie(String file, PlainOldTrie trie) {
        new Config();
        String TRIE_FILE = Config.get(file);

        // loop over the file line by line
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Config.getLoader().getResourceAsStream(TRIE_FILE), StandardCharsets.UTF_8))) {
            // parse trie line by line
            String line;
            PlainOldTrieNode currentNode = trie.getRoot();
            while ((line = br.readLine()) != null) {
                String[] splitLine = line.split("\\t");
                currentNode = parseLinePreOrder(currentNode, splitLine);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

//    public PlainOldTrie fileToTrie(String file) {
//        new Config();
//        String TRIE_FILE = Config.get(file);
//        PlainOldTrie trie = new PlainOldTrie();
//
//        // loop over the file line by line
//        FileInputStream inputStream = null;
//        Scanner sc = null;
//        try {
//            inputStream = new FileInputStream(TRIE_FILE);
//            sc = new Scanner(inputStream, "UTF-8");
//            // parse trie line by line
//            PlainOldTrieNode currentNode = trie.getRoot();
//            while (sc.hasNextLine()) {
//                String line = sc.nextLine();
//                String[] splitLine = line.split("\\t");
//                currentNode = parseLinePreOrder(currentNode, splitLine);
//            }
//            // note that Scanner suppresses exceptions
//            if (sc.ioException() != null) {
//                throw sc.ioException();
//            }
//        } catch (IOException e){
//            e.printStackTrace();
//        } finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (sc != null) {
//                sc.close();
//            }
//        }
//        return trie;
//    }


//    /**
//     * Iteratively construct trie by one line.
//     *
//     * @param node          start node
//     * @param splittedLine  line splitted by tab, which contains encoding trie structure
//     * @return next node in concern
//     */
//    TrieNode<Object> parseLinePreOrder(TrieNode<Object> node, String[] splittedLine) {
//        if (splittedLine.length == 1) {  // empty line, go upper level
//            if (node.getLevel() == 0) // if root
//                return node;
//            return node.getParent();
//        }
//        for (String item : splittedLine)
//            node = parseItem(node, item);
//        return node.getParent();
//    }

    /**
     * Iteratively construct trie by one line.
     *
     * @param node         start node
     * @param splittedLine line splitted by tab, which contains encoding trie structure
     * @return next node in concern
     */
    PlainOldTrieNode parseLinePreOrder(PlainOldTrieNode node, String[] splittedLine) {
        if (splittedLine.length == 1) {  // empty line, go upper level
            if (node.getLevel() == 0) // if root
                return node;
            return node.getParent();
        }
        for (String item : splittedLine)
            node = parseItem(node, item);
        return node.getParent();
    }

    /**
     * Get next node in concern from input node by parsing item string.
     *
     * @param currentNode start node
     * @param item        node string
     * @return next node in concern
     */
    PlainOldTrieNode parseItem(PlainOldTrieNode currentNode, String item) {
        int level = currentNode.getLevel();
        if (!item.equals("|")) {    // go deeper
            String[] splitted = item.split(":");
            ChinesePinyinTuple tuple = null;
            boolean isLeaf = false;
            if (splitted.length == 2) {
                tuple = (ChinesePinyinTuple) parseContentStr(item);
                isLeaf = true;
            }
            PlainOldTrieNode newNode = new PlainOldTrieNode(splitted[0], level + 1, tuple, currentNode);
            if (isLeaf)
                newNode.setAsLeaf();
            newNode.setLevel(level + 1);
            newNode.setParent(currentNode);
            currentNode.getChildren().put(newNode.getToken(), newNode);
            return newNode;
        } else { // return to upper level, next pos
            return currentNode.getParent();
        }
    }

    /**
     * Parse the content string to content object.
     *
     * @param str content in string
     * @return content object
     */
    Object parseContentStr(String str) {
        String[] splitStr = str.split(":");
        if (splitStr.length == 1) { // not a leaf node
            assert str.length() == 1;   // must be only one chinese char
            return new PlainOldTrieNode(str);
        } else {
            String[] tupleArray = splitStr[1].split("=");
            assert tupleArray.length == 2;
            return new ChinesePinyinTuple(tupleArray[0], tupleArray[1]);
        }
    }

}
