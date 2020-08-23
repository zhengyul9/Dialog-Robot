package ai.hual.labrador.nlu.resourceFormats;


import org.ansj.domain.Result;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.nlpcn.commons.lang.tire.domain.Forest;

import java.util.ArrayList;
import java.util.List;



public class QueryElement {

    public QueryElement() {
    }

    public List<List> wipeRepetite(List<List> queryElementList){
        for(int i=queryElementList.size()-1;i>=0;i--){
            List queryElementFirst=queryElementList.get(i);
            for(int j=i-1;j>0;j--){
                List queryElementSecend=queryElementList.get(j);
                if(queryElementFirst.get(0).equals(queryElementSecend.get(0))&&
                        queryElementFirst.get(1).equals(queryElementSecend.get(1))&&
                        queryElementFirst.get(2).equals(queryElementSecend.get(2))&&
                        queryElementFirst.get(3).equals(queryElementSecend.get(3)))

                {
                    queryElementList.remove(i);
                    break;
                }
            }
        }
        return queryElementList;
    }

    public boolean isContain(List<List> elementList,List Element){
        for(int i=0;i<elementList.size();i++){
            if(elementList.get(i).get(0).equals(Element.get(0))&&
                    elementList.get(i).get(1).equals(Element.get(1))&&
                    elementList.get(i).get(2).equals(Element.get(2))&&
                    elementList.get(i).get(3).equals(Element.get(3)))
            {
                return true;
            }
        }
        return false;
    }

    public List<List> getQuerySplitElement(String query, Forest forest,int startIndex){
        List<List> querySplitElementList=new ArrayList<>();
        Result result = ToAnalysis.parse(query,forest);

        List<Term> segList=result.getTerms();
        List querySplitElement;
        int indexStart=startIndex;
        int indexEnd=0;
        for(int i=0;i<segList.size();i++){
            String singleWord=segList.get(i).toString();
            String wordStr="";
            String wordPro="";
            if(singleWord.contains("/")){
                wordStr = singleWord.substring(0, singleWord.indexOf("/"));
                wordPro=singleWord.substring(singleWord.indexOf("/")+1,singleWord.length());
            }else{
                wordStr=singleWord;
            }
            querySplitElement=new ArrayList();
            querySplitElement.add(indexStart);
            indexEnd=indexStart+wordStr.length();
            querySplitElement.add(indexEnd);
            indexStart=indexEnd;
            querySplitElement.add(wordStr);
            querySplitElement.add(wordPro);
            querySplitElementList.add(querySplitElement);
        }
        return querySplitElementList;
    }
}
