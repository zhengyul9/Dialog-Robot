package ai.hual.labrador.nlu.resourceFormats;

import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StopWordsTable {
    public List<String> stopWordList = new ArrayList<>();

    public StopWordsTable(List<String> stopWordList) throws IOException {
        this.stopWordList = stopWordList;
    }

//    去除String中的停用词
    public String wipeQueryStopWord(String query) {
        for (int j = query.length() - 1; j >= 0; j--) {
            if (stopWordList.contains(query.substring(j, j + 1))) {
                String strIn = query.substring(j, j + 1);
                query = query.replace(query.substring(j, j + 1), "");
            }
        }
        return query;
    }

//    批量去除String中的停用词
    public List<String> wipeStopWordStrList(List<String> queryList){
        return queryList;
    }

//    去除Anntator中的停用词
    public List<List> wipeStopWordAnntator(List<List> Anntator) {
        int tempalteLength = Anntator.size();
        for (int i = tempalteLength - 1; i >= 0; i--) {
//            System.out.println(template.get(i).getKey());
            String nowWord = (String) Anntator.get(i).get(2);
            if (stopWordList.contains(nowWord)) {
                Anntator.remove(i);
            }
        }
        return Anntator;
    }

//    匹配template中的停用词

    public List<String> matchedStopWord(List<Pair<String,String>> template){
        List<String> stopList=new ArrayList<>();
        for(int i=0;i<stopWordList.size();i++){
            for(int j=0;j<template.size();j++){
                if(stopWordList.get(i).equals(template.get(j).getKey())){
                    stopList.add(stopWordList.get(i));
                }
            }
        }
        return stopList;
    }

    public List<List> wipeStopElement(List<List> queryElementOld){
        List<List> queryElement=new ArrayList<>();

//        for(int i=queryElement.size()-1;i>=0;i--){
////            System.out.println(queryElement.get(i).get(2).toString());
//            if(stopWordList.contains(queryElement.get(i).get(2).toString())){
//                queryElement.remove(i);
//            }
//        }

//        System.out.println(queryElementOld);
        for(int i=0;i<queryElementOld.size();i++){
            if(stopWordList.contains(queryElementOld.get(i).get(2).toString())==false){
                queryElement.add(queryElementOld.get(i));
            }
        }
        return queryElement;
    }



//    去除template中的停用词
    public List<Pair<String, String>> wipeStopWord(List<Pair<String, String>> template) {
        int tempalteLength = template.size();
        for (int i = tempalteLength - 1; i >= 0; i--) {
//            System.out.println(template.get(i).getKey());
            String nowWord = template.get(i).getKey();
            boolean isExist = stopWordList.contains(template.get(i).getKey());
            boolean isExist2 = stopWordList.contains("的");
            if (stopWordList.contains(template.get(i).getKey())) {
                template.remove(i);
            }
        }
        return template;
    }

//    批量去除template中的停用词
    public List<Pair<String, List<Pair<String, String>>>> wipeStopWordTemplateBtach(List<Pair<String, List<Pair<String, String>>>> templateList){

        return templateList;
    }

}
