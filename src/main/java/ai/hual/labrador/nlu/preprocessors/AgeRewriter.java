package ai.hual.labrador.nlu.preprocessors;


import ai.hual.labrador.nlu.Preprocessor;
import ai.hual.labrador.nlu.QueryAct;
import ai.hual.labrador.nlu.SlotValue;
import ai.hual.labrador.nlu.annotators.NumAnnotator;
import org.springframework.stereotype.Component;

import java.util.List;


@Component("ageRewriter")
public class AgeRewriter implements Preprocessor {

    private NumAnnotator numAnnotator = new NumAnnotator();

    @Override
    public String preprocess(String query) {
        //这个字典是用来存放 年龄和名的对应关系的，
        String standardAgeName1 = "未成年";
        String standardSgeName2 = "成年";
        int ageRangeStart = 0;
        int ageRangeEnd = 18;

        List<QueryAct> acts = numAnnotator.annotate(new QueryAct(query));
        if (acts.isEmpty()) {
            return query;
        }
        QueryAct act = acts.get(0);

        List<SlotValue> numberList = act.getSlots().get("数字");
        if (numberList.isEmpty()) {
            return query;
        }
        //        在后年的表示是年龄的准确标志
        String ageMarkString1 = "岁";
        String ageMarkString2 = "周";
        //        在前面的是年龄的准确标志
        String ageMarkStringFront1 = "我";
        String ageMarkStringFront2 = "今年";
        String ageMarkStringFront3 = "投保人";

        StringBuffer newString = new StringBuffer(query);
        int numberListLength = numberList.size();

        for (int i = numberListLength - 1; i >= 0; i--) {

//            获取年龄数据
            int ageStart = numberList.get(i).getRealStart();
            int ageEnd = numberList.get(i).getRealEnd();
            double age = (double) numberList.get(i).getMatched();
            int ageNumber = (int) age;


//	    可能是年龄的标识符
//        在后面的标识符
            String ageMarkStringPossibleBehind = "";
            if (query.length() > ageEnd) {
                ageMarkStringPossibleBehind = String.valueOf(query.charAt(ageEnd));
            }

//        在前面的标识符
            String ageMarkStringPossibleFront1 = "";
            String ageMarkStringPossibleFront2 = "";
            String ageMarkStringPossibleFront3 = "";

            if (ageStart > 2) {
                ageMarkStringPossibleFront3 = String.valueOf(query.substring(ageStart - 3, ageStart));
            }
            if (ageStart > 1) {
                ageMarkStringPossibleFront2 = String.valueOf(query.substring(ageStart - 2, ageStart));
            }
            if (ageStart > 0) {
                ageMarkStringPossibleFront1 = String.valueOf(query.substring(ageStart - 1, ageStart));
            }

            if (ageMarkStringPossibleBehind.equals(ageMarkString1)) {
//            判断数字的区间，是否在未成年人的区间
                if (ageNumber < ageRangeEnd && ageNumber > ageRangeStart) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd + 1, standardAgeName1);
//                    前面+未成年人+后面的部分
                } else if (ageNumber < 120 && ageNumber >= 18) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd + 1, standardSgeName2);
//                    前面+未成年人+后面的部分
                }
//            判断“周”是否存在
            } else if (ageMarkStringPossibleBehind.equals(ageMarkString2)) {
//            判断数字的区间，是否在未成年人的区间
                if (ageNumber < ageRangeEnd && ageNumber > ageRangeStart) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd + 2, standardAgeName1);
//                    前面+未成年人+后面的部分
                } else if (ageNumber < 120 && ageNumber >= 18) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd + 2, standardSgeName2);
//                    前面+未成年人+后面的部分
                }
//            判断前面的字符
//            判断一个字符
            } else if (ageMarkStringPossibleFront1.equals(ageMarkStringFront1)) {
//            判断数字的区间，是否在未成年人的区间
                if (ageNumber < ageRangeEnd && ageNumber > ageRangeStart) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd, standardAgeName1);
//                    前面+未成年人+后面的部分
                } else if (ageNumber < 120 && ageNumber >= 18) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd, standardSgeName2);
//                    前面+未成年人+后面的部分
                }
//            判断两个字符
            } else if (ageMarkStringPossibleFront2.equals(ageMarkStringFront2)) {
//            判断数字的区间，是否在未成年人的区间
                if (ageNumber < ageRangeEnd && ageNumber > ageRangeStart) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd, standardAgeName1);
//                    前面+未成年人+后面的部分
                } else if (ageNumber < 120 && ageNumber >= 18) {
//                是未成年，将2岁替换成未成
                    newString.replace(ageStart, ageEnd, standardSgeName2);
//                    前面+未成年人+后面的部分
                }
//            判断三个字符
            } else if (ageMarkStringPossibleFront3.equals(ageMarkStringFront3)) {
//            判断数字的区间，是否在未成年人的区间
                if (ageNumber < ageRangeEnd && ageNumber > ageRangeStart) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd, standardAgeName1);
//                    前面+未成年人+后面的部分
                } else if (ageNumber < 120 && ageNumber >= 18) {
//                是未成年，将2岁替换成未成年
                    newString.replace(ageStart, ageEnd, standardSgeName2);
//                    前面+未成年人+后面的部分
                }
            }
        }
        if (newString.length() == 0) {
            return query;
        }
        return String.valueOf(newString);
    }
}
