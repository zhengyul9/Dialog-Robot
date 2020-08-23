package ai.hual.labrador.nlu.resourceFormats;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class EncodingDecoding {


    public SynonymyTable synonymyTable=new SynonymyTable();

    public EncodingDecoding(){}

    public EncodingDecoding(SynonymyTable synonymyTable){
        this.synonymyTable=synonymyTable;
    }

//    将转换后的模板序列化成字符串
    public String encodingTemplate(List<Pair<String,String>> template){
//        System.out.println(template);
        String encodedTemplate="";
        StringBuffer encodedTemplateBuf=new StringBuffer(encodedTemplate);
        for(int i=0;i<template.size();i++){
            Pair<String,String> templateElement=template.get(i);
            if(templateElement.getValue().equals("entity")){
                encodedTemplateBuf.append("/");
                encodedTemplateBuf.append("{{");
                encodedTemplateBuf.append(templateElement.getKey());
                encodedTemplateBuf.append("}}");
            }else if(templateElement.getValue().equals("str")){
                encodedTemplateBuf.append("/");
                encodedTemplateBuf.append(templateElement.getKey());
            }else {
//                查同义词典然后，标上同义词集合名
                if(this.synonymyTable.synonymyDictSet.get(templateElement.getKey())!=null){
                    encodedTemplateBuf.append("/");
                    encodedTemplateBuf.append("[");
                    encodedTemplateBuf.append(this.synonymyTable.synonymyDictSet.get(templateElement.getKey()));
                    encodedTemplateBuf.append("]");
                }else{
                    encodedTemplateBuf.append("/");
                    encodedTemplateBuf.append(templateElement.getKey());
                }
            }
        }
//        System.out.println(encodedTemplate);
        encodedTemplate=encodedTemplateBuf.toString();
        return encodedTemplate;
    }

    public String toStringSynonymyStructure(Pair<List<Pair<String, String>>, List<Pair<String, String>>> synonymyStructure){
//        [{{entity}}=a, [n]=c, 是=Str, 什么=Str, 意思=Str]=[(a)=variable, (c)=variable, 的=str, 定义=str, 是=str, 什么=str]
        String resultStr="";
        StringBuffer resultStrBuf=new StringBuffer();
        List<Pair<String, String>> preElementList=synonymyStructure.getKey();
        List<Pair<String, String>> behElementList=synonymyStructure.getValue();
//        加前项
        for(int i=0;i<preElementList.size();i++){
            if(preElementList.get(i).getValue().equals("sets")){
//                resultStr+=preElementList.get(i).getKey();
                resultStrBuf.append(preElementList.get(i).getKey());
            }else if (preElementList.get(i).getValue().equals("Str")){
//                resultStr+=preElementList.get(i).getKey();
                resultStrBuf.append(preElementList.get(i).getKey());
            }else {
                resultStrBuf.append("(");
                resultStrBuf.append(preElementList.get(i).getValue());
                resultStrBuf.append("：");
                resultStrBuf.append(preElementList.get(i).getKey());
                resultStrBuf.append(")");
            }
        }
//        resultStr+="->";
        resultStrBuf.append("->");
//        加后项
        for(int i=0;i<behElementList.size();i++){
//            resultStr+=behElementList.get(i).getKey();
            resultStrBuf.append(behElementList.get(i).getKey());
        }
        resultStr=resultStrBuf.toString();
        return resultStr;
    }
}
