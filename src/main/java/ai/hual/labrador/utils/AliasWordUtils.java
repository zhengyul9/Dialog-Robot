package ai.hual.labrador.utils;

import ai.hual.labrador.nlu.Dict;

import java.util.ArrayList;
import java.util.HashMap;

public class AliasWordUtils {

    // 产生所有单词字的2的n次方种组合
    public static ArrayList<Dict> generateAliasWords(Dict dict) {
        ArrayList<Dict> res = new ArrayList<>();
        String word = dict.getWord();
        ArrayList<String> wordList = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) wordList.add(String.valueOf(word.charAt(i)));

        int len = 2 << (wordList.size() - 1);

        for (int i = 0; i < len; i++) {
            int n = i, j = 0;
            StringBuilder aliasWord = new StringBuilder();
            while (n > 0) {
                if (n % 2 != 0) aliasWord.append(wordList.get(j));
                j += 1;
                n >>= 1;
            }
            if (!aliasWord.toString().equals("")) {
                Dict aliasDict = new Dict(dict);
                aliasDict.setWord(aliasWord.toString());
                res.add(aliasDict);
            }
        }

        return res;
    }

    /**
     * 当产生单词的长度为1,2...n-k-1时，生成单词为该长度在原单词中滑动取得；
     * 当单词的长度为n-k,n-k+1,...n时（即原单词分别减去1,2，...k个字符），生成单词为该长度在原单词中取组合后得到
     **/
    public static ArrayList<Dict> generateAliasWords(Dict dict, int k) {
        ArrayList<Dict> res = new ArrayList<>();
        String word = dict.getWord();
        HashMap<String, Integer> dictCount;
        int n = word.length();
        // 当产生单词的长度为1,2...n-k-1时，生成单词为该长度在原单词中滑动取得;此种情况暂时不用
//        for (int i = 1; i <= n-k-1; i++) {
//            for (int j = 0; j <= n-i; j++) {
//                Dict aliasDict = new Dict(dict);
//                aliasDict.setWord(word.substring(j, j+i));
//                res.add(aliasDict);
//            }
//        }

        for (int i = n - k; i < n; i++) {
            ArrayList<Dict> tempRes = combinations(dict, n, i);
            for (Dict aliasDict : tempRes) res.add(aliasDict);
        }

        return res;
    }

    // 只开关小括号
    public static ArrayList<Dict> generateAliasWordsWithBrackets(Dict dict) {
        ArrayList<Dict> res = new ArrayList<>();
        String word = dict.getWord();
        ArrayList<Integer> bracketsIndex = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (ch == '【' || ch == '】' || ch == '(' || ch == ')') bracketsIndex.add(i);
        }
        if (bracketsIndex.size() == 0) {
            res.add(dict);
            return res;
        }
        int len = 2 << (bracketsIndex.size() - 1);
        for (int i = 0; i < len; i++) {
            int n = i, j = 0;
            StringBuilder aliasWord = new StringBuilder(word);
            while (n > 0) {
                if (n % 2 == 0) aliasWord.setCharAt(bracketsIndex.get(j), ' ');
                j += 1;
                n >>= 1;
            }
            for (int k = j; k < bracketsIndex.size(); k++) aliasWord.setCharAt(bracketsIndex.get(k), ' ');
            if (!aliasWord.toString().equals("")) {
                Dict aliasDict = new Dict(dict);
                aliasDict.setWord(aliasWord.toString().replaceAll(" ", ""));
                res.add(aliasDict);
            }
        }

        return res;
    }

    // 生成组合的递归方法
    public static ArrayList<Dict> combinations(Dict dict, int n, int k) {
        if (n == k) {
            Dict aliasDict = new Dict(dict);
            aliasDict.setWord(dict.getWord().substring(0, n));
            ArrayList<Dict> tempRes = new ArrayList<>();
            tempRes.add(aliasDict);
            return tempRes;
        }

        if (k == 1) {
            ArrayList<Dict> tempRes = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Dict aliasDict = new Dict(dict);
                aliasDict.setWord(String.valueOf(dict.getWord().charAt(i)));
                tempRes.add(aliasDict);
            }
            return tempRes;
        }


        ArrayList<Dict> tempRes = combinations(dict, n - 1, k);
        ArrayList<Dict> tempRes2 = combinations(dict, n - 1, k - 1);
        for (Dict aliasDict : tempRes2) {
            aliasDict.setWord(aliasDict.getWord() + String.valueOf(dict.getWord().charAt(n - 1)));
            tempRes.add(aliasDict);
        }
        return tempRes;

    }

    // just for test
    public static void main(String[] args) {
        Dict dict = new Dict();
        ArrayList<Dict> dicts;

        dict.setWord("abcd");
        dicts = AliasWordUtils.generateAliasWords(dict, 2);

        for (Dict tdict : dicts) {
            System.out.println(tdict.getWord());
        }
        System.out.println(dicts.size());
    }
}
