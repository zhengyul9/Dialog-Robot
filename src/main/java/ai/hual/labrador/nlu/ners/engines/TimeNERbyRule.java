package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.subners.TimeNER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeNERbyRule {
    private static final String BELONG_TO_NER_NAME = "TimeNER";
    private static final String RESOURCES_DIR = "Resources_TimeNERbyRule";
    private static final String USED_REGEX_FILE = RESOURCES_DIR + "/" + "complex_time_regex.txt";
    private static final String USED_DURATION_REGEX_FILE = RESOURCES_DIR + "/" + "complex_duration_regex.txt";
    private static final String USED_TOTAL_Time_FILE = RESOURCES_DIR + "/" + "total_time.txt";
    private static final String USED_TOTAL_Duration_FILE = RESOURCES_DIR + "/" + "total_duration.txt";
    private static final Logger logger = LoggerFactory.getLogger(TimeNER.class);
    List<String> complex_time_regex = this.getWordList(USED_REGEX_FILE);
    List<String> complex_duration_regex = this.getWordList(USED_DURATION_REGEX_FILE);
    List<String> allTime = this.getWordList(USED_TOTAL_Time_FILE);
    List<String> allDuration = this.getWordList(USED_TOTAL_Duration_FILE);

    String time_regex = "(" + String.join("|", complex_time_regex) + ")";
    String duration_regex = "(" + String.join("|", complex_duration_regex) + ")";
    String time = "(" + String.join("|", allTime) + ")";
    String duration = "(" + String.join("|", allDuration) + ")";

    // regex starts here

    Pattern p = Pattern.compile(duration_regex);
    Pattern p1 = Pattern.compile(time_regex);
    Pattern p2 = Pattern.compile(time);
    Pattern p3 = Pattern.compile(duration);
    // regex ends here

    //获取词表内容，并返回一个List<String []>
    public List<String> getWordList_TwoWordsEachLine(String file) { // not being used yet
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8))) {
            List<String> result = new ArrayList<>();
            String line,regex,type;
            int split_location;
            while ((line = br.readLine()) != null ) {
                line = line.trim();
                if(line.length() == 0)
                    continue;
                if(line.charAt(0) == '#')
                    continue;
                split_location = line.indexOf(" ");
                regex = line.substring(0,split_location);
                type = line.substring(split_location + 1, line.length());
                System.out.println(line + "1" + regex + "2" + type);
                result.add(line);
            }
            logger.debug("getWordList finished in TimeNER: " + result.toString());
            return result;
        } catch (IOException ex) {
            logger.error("getWordList error in TimeNER.");
            throw new NLUException("Could not find file " + file);
        }
    }

    //获取词表内容, 并返回一个List<String>
    public List<String> getWordList(String file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8));
            List<String> result = new ArrayList<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0) // ignore blank line
                    return;
                if (line.charAt(0) == '#') // ignore
                    return;
                result.add(line);
            });
            br.close();
            logger.debug("getWordList finished in PersonNER: " + result.toString());
            return result;
        } catch (IOException ex) {
            logger.error("getWordList error in PersonNER.");
            throw new NLUException("Could not find file " + file);
        }
    }

    // recognize all time from text
    public List<NERResult.Candidate> timeRecognize(String text) {
        Matcher m = p.matcher(text);
        Matcher m1;
        Matcher m2;
        Matcher m3;
        String star_char = "";
        List<NERResult.Candidate> candidates = new ArrayList<>();
            while(m.find()){    // get all spans of matches str
             candidates.add(new NERResult.Candidate(m.start(), m.end(), "时间段", "TimeNER", text.substring(m.start(), m.end()), "", ""));
             //System.out.println(text.substring(m.start(),m.end()));
             star_char = getStarChar(text.substring(m.start(),m.end()).length());
             text  = text.replace(text.substring(m.start(), m.end()),star_char);
             //System.out.println("text: "+ text);
             m = p.matcher(text);
            }
            m1 = p1.matcher(text);
            while(m1.find()) {
                candidates.add(new NERResult.Candidate(m1.start(), m1.end(), "时间点", "TimeNER", text.substring(m1.start(), m1.end()), "", ""));
                star_char = getStarChar(text.substring(m1.start(),m1.end()).length());
                text  = text.replace(text.substring(m1.start(), m1.end()),star_char);
                m1 = p1.matcher(text);
            }
            m2 = p2.matcher(text);
            while(m2.find()) {
                candidates.add(new NERResult.Candidate(m2.start(), m2.end(), "时间点", "TimeNER", text.substring(m2.start(), m2.end()), "", ""));
                star_char = getStarChar(text.substring(m2.start(),m2.end()).length());
                text  = text.replace(text.substring(m2.start(), m2.end()),star_char);
                m2 = p2.matcher(text);
            }
            m3 = p3.matcher(text);
            while(m3.find()) {
                candidates.add(new NERResult.Candidate(m3.start(), m3.end(), "时间段", "TimeNER", text.substring(m3.start(), m3.end()), "", ""));
                star_char = getStarChar(text.substring(m3.start(),m3.end()).length());
                text  = text.replaceFirst(text.substring(m3.start(), m3.end()),star_char);
                m3 = p3.matcher(text);
            }
        candidates = candidatesNormalize(candidates);
        return candidates;
    }

    String getStarChar(int length) {
        String[] starArr={"*","**","***","****","*****","******","*******","********","*********",
                "**********","***********"};
        if (length <= 0) {
            return "";
        }
        //大部分敏感词汇在10个以内，直接返回缓存的字符串
        if (length <= 11) {
            return starArr[length - 1];
        }
        //生成n个星号的字符串
        char[] arr = new char[length];
        for (int i = 0; i < length; i++) {
            arr[i] = '*';
        }
        return new String(arr);
    }

    List<NERResult.Candidate> candidatesNormalize(List<NERResult.Candidate> candidates){
        for(int i = 0; i < candidates.size(); i++){
            //处理中文年 e.g. 二零一九
            candidates.get(i).setEntity(candidates.get(i).getText());
            candidates.get(i).setText(yearNum(candidates.get(i).getText()));
            //处理特殊时间（xx:xx时间、零点）
            if(candidates.get(i).getText().contains(":00") || candidates.get(i).getText().contains("：00") ||
                    candidates.get(i).getText().contains("零点") || candidates.get(i).getText().contains("零时")){
                candidates.get(i).setText(candidates.get(i).getText().replaceAll(":00","点"));
                candidates.get(i).setText(candidates.get(i).getText().replaceAll("：00","点"));
                candidates.get(i).setText(candidates.get(i).getText().replaceAll("零时","0时"));
                candidates.get(i).setText(candidates.get(i).getText().replaceAll("零点","0时"));
            }
            //处理符号隔开的时间
            if(candidates.get(i).getText().contains("、"))
                continue;
            if(candidates.get(i).getText().contains("-"))
                continue;
            //年月日
            candidates.set(i,year_month_day(candidates.get(i)));
            //日时分秒
            candidates.set(i,day_time(candidates.get(i)));
            //时间段
            candidates.set(i,duration(candidates.get(i)));
            //去中间符号
            candidates.get(i).setText(candidates.get(i).getText().replace(",",""));
            candidates.get(i).setText(toNum(candidates.get(i).getText()));
        }
        return candidates;
    }
    // below functions are used for normalization
    NERResult.Candidate duration (NERResult.Candidate candidates) {
        if(candidates.getText().contains("到")){
            candidates.setText(candidates.getText().replace("到","～"));
        }
        return candidates;
    }

    NERResult.Candidate year_month_day (NERResult.Candidate candidates) {
        String year, month, day, temp;
        Date d1 = null;
        Pattern p = Pattern.compile("[0-9零一二两仨三四五六七八九十]+年[0-9零一二两仨三四五六七八九十]+(月份|月)[0-9零一二两仨三四五六七八九十]+(日|号)");
        Matcher m = p.matcher(candidates.getText());
        while(m.find()){
            temp = candidates.getText().substring(m.start(),m.end());
            temp = temp.replaceAll("月份","月");
            temp = temp.replaceAll("号","日");
            temp = toNum(temp);
                try {
                    d1 = new SimpleDateFormat("yyyy年MM月dd日").parse(temp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy");
                SimpleDateFormat sdf1 = new SimpleDateFormat("MM");
                SimpleDateFormat sdf2 = new SimpleDateFormat("dd");
                year = sdf0.format(d1); //以下的if为确保年份正常显示
                month = sdf1.format(d1);
                day = sdf2.format(d1);
                if (candidates.getText().contains("00年") || candidates.getText().contains("2000年"))
                    year = "2000";
                if (Integer.parseInt(year) < 50 && Integer.parseInt(year) >= 0) {
                    year = year.replaceFirst("0", "2");
                }
                if (Integer.parseInt(year) > 50 && Integer.parseInt(year) < 100) {
                    year = year.replace("00", "19");
                }
                temp = year + "-" + month + "-" + day;
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m.start(),m.end()),temp));
            m = p.matcher(candidates.getText());
            }
        Pattern p1 = Pattern.compile("[0-9零一二两仨三四五六七八九十]+年[0-9零一二两仨三四五六七八九十]+(个月|月份|月)");
        Matcher m1 = p1.matcher(candidates.getText());
        while(m1.find()){
            temp = candidates.getText().substring(m1.start(),m1.end());
            temp = temp.replaceAll("个月","月");
            temp = temp.replaceAll("月份","月");
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("yyyy年MM月").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy");
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM");
            year = sdf0.format(d1); //以下的if为确保年份正常显示
            month = sdf1.format(d1);
            if (candidates.getText().contains("00年") || candidates.getText().contains("2000年"))
                year = "2000";
            if (Integer.parseInt(year) < 50 && Integer.parseInt(year) >= 0) {
                year = year.replaceFirst("0", "2");
            }
            if (Integer.parseInt(year) > 50 && Integer.parseInt(year) < 100) {
                year = year.replace("00", "19");
            }
            temp = year + "-" + month + "-" + "00";
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m1.start(),m1.end()),temp));
            m1 = p1.matcher(candidates.getText());
        }
        Pattern p2 = Pattern.compile("[0-9零一二两仨三四五六七八九十]+(个月|月份|月)[0-9零一二两仨三四五六七八九十]+(日|号)");
        Matcher m2 = p2.matcher(candidates.getText());
        while(m2.find()){
            temp = candidates.getText().substring(m2.start(),m2.end());
            temp = temp.replaceAll("个月","月");
            temp = temp.replaceAll("月份","月");
            temp = temp.replaceAll("号","日");
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("MM月dd日").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM");
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd");
            month = sdf1.format(d1);
            day = sdf2.format(d1);
            temp = "0000" + "-" + month + "-" + day;
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m2.start(),m2.end()),temp));
            m2 = p2.matcher(candidates.getText());
        }
        Pattern p3 = Pattern.compile("[0-9零一二两仨三四五六七八九十]+年");
        Matcher m3 = p3.matcher(candidates.getText());
        while(m3.find()){
            temp = candidates.getText().substring(m3.start(),m3.end());
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("yyyy年").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy");
            year = sdf0.format(d1); //以下的if为确保年份正常显示
            if((candidates.getText().substring(0,temp.indexOf("年")).length()) ==1 ||
                    ((Integer.parseInt(temp.substring(0,temp.indexOf("年")))%10 ==0) && candidates.getEntity().contains("零")== false) ||
                    (candidates.getText().contains("十") && Integer.parseInt(temp.substring(0,temp.indexOf("年")))%10 !=0)){
                temp = year + "-" + "00" + "-" + "00";
                candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m3.start(),m3.end()),temp));
                m3 = p3.matcher(candidates.getText());
            }
            else{
                if (candidates.getText().matches("00年") || candidates.getText().contains("2000年"))
                    year = "2000";
                if (Integer.parseInt(year) < 50 && Integer.parseInt(year) >= 0) {
                    year = year.replaceFirst("0", "2");
                }
                if (Integer.parseInt(year) > 50 && Integer.parseInt(year) < 100) {
                    year = year.replace("00", "19");
                }
                temp = year + "-" + "00" + "-" + "00";
                candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m3.start(),m3.end()),temp));
                m3 = p3.matcher(candidates.getText());
            }
        }
        Pattern p4 = Pattern.compile("[0-9零一二两仨三四五六七八九十]+(个月|月份|月)");
        Matcher m4 = p4.matcher(candidates.getText());
        while(m4.find()){
            temp = candidates.getText().substring(m4.start(),m4.end());
            temp = temp.replaceAll("个月","月");
            temp = temp.replaceAll("月份","月");
            temp = temp.replaceAll("号","日");
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("MM月").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM");
            month = sdf1.format(d1);
            temp = "0000" + "-" + month + "-" + "00";
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m4.start(),m4.end()),temp));
            m4 = p4.matcher(candidates.getText());
        }
        Pattern p5 = Pattern.compile("[0-9零一二两仨三四五六七八九十]+(日|号|天)");
        Matcher m5 = p5.matcher(candidates.getText());
        while(m5.find()){
            temp = candidates.getText().substring(m5.start(),m5.end());
            temp = temp.replaceAll("号","日");
            temp = temp.replaceAll("天","日");
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("dd日").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf2 = new SimpleDateFormat("dd");
            day = sdf2.format(d1);
            temp = "0000" + "-" + "00" + "-" + day;
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m5.start(),m5.end()),temp));
            m5 = p5.matcher(candidates.getText());
        }
        return candidates;
    }

    NERResult.Candidate day_time (NERResult.Candidate candidates) {
        String hour, minute, second, temp;
        Date d1 = null;
        Pattern p0 = Pattern.compile("[这下星期周]+(一|二|三|四|五|六|天|日|1|2|3|4|5|6|7)|[前昨今当本这明后]+(日|天)(凌晨|早上|早晨|上午|中午|下午|白天|晚上)");
        Matcher m0 = p0.matcher(candidates.getText());
        while(m0.find()) {
            candidates.setText(candidates.getText().replace(m0.group(), m0.group() + ","));
        }
        Pattern p2 = Pattern.compile("[0-9零一二两仨三四五六七八九十]+(点|小时|时)[0-9零一二三四五六七八九十]+(分钟|分)[0-9零一二三四五六七八九十]+(秒钟|秒)");
        Matcher m2 = p2.matcher(candidates.getText());
        while(m2.find()){
            temp = candidates.getText().substring(m2.start(),m2.end());
            temp = temp.replaceAll("(点|小时)","时");
            temp = temp.replaceAll("分钟","分");
            temp = temp.replaceAll("秒钟","秒");
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("HH时mm分ss秒").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf0 = new SimpleDateFormat("HH");
            SimpleDateFormat sdf1 = new SimpleDateFormat("mm");
            SimpleDateFormat sdf2= new SimpleDateFormat("ss");
            hour = sdf0.format(d1);
            minute = sdf1.format(d1);
            second = sdf2.format(d1);
            temp = (hour + ":" + minute + ":" + second);
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m2.start(),m2.end()),temp));
            m2 = p2.matcher(candidates.getText());
        }

        Pattern p = Pattern.compile("[0-9零一二两仨三四五六七八九十]+(点|小时|时)(([0-9零一二三四五六七八九十]+(分钟|分))|半)");
        Matcher m = p.matcher(candidates.getText());
        while(m.find()){
            temp = candidates.getText().substring(m.start(),m.end());
            temp = temp.replaceAll("(点|小时)","时");
            temp = temp.replaceAll("分钟","分");
            temp = temp.replaceAll("半","30分");
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("HH时mm分").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf0 = new SimpleDateFormat("HH");
            SimpleDateFormat sdf1 = new SimpleDateFormat("mm");
            hour = sdf0.format(d1);
            minute = sdf1.format(d1);
            temp = hour + ":" + minute + ":" + "00";
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m.start(),m.end()),temp));
            m = p.matcher(candidates.getText());
        }
        Pattern p1 = Pattern.compile("[0-9零一二两仨三四五六七八九十]+(点|小时|时)");
        Matcher m1 = p1.matcher(candidates.getText());
        while(m1.find()) {
            temp = candidates.getText().substring(m1.start(), m1.end());
            temp = temp.replaceAll("(点|小时)", "时");
            temp = toNum(temp);
            System.out.println(temp);
            try {
                d1 = new SimpleDateFormat("HH时").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf0 = new SimpleDateFormat("HH");
            hour = sdf0.format(d1);
            temp = hour + ":" + "00" + ":" + "00";
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m1.start(),m1.end()),temp));
            m1 = p1.matcher(candidates.getText());
        }
        Pattern p3 = Pattern.compile("[0-9零一二两三四五六七八九十]+(分钟|分)");
        Matcher m3 = p3.matcher(candidates.getText());
        while(m3.find()){
            temp = candidates.getText().substring(m3.start(),m3.end());
            temp = temp.replaceAll("分钟","分");
            temp = toNum(temp);
            try {
                d1 = new SimpleDateFormat("mm分").parse(temp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            SimpleDateFormat sdf1 = new SimpleDateFormat("mm");
            minute = sdf1.format(d1);
            temp = "00" + ":" + minute + ":" + "00";
            candidates.setText(candidates.getText().replaceAll(candidates.getText().substring(m3.start(),m3.end()),temp));
            m3 = p3.matcher(candidates.getText());
        }
        candidates.setText(candidates.getText().replace(",",""));
        return candidates;
    }

    String toNum(String chinaNumberStr) { //
        Map<Character, String> numberMap = new HashMap<Character, String>();
        numberMap.put('零', "0");
        numberMap.put('一', "1");
        numberMap.put('二', "2");
        numberMap.put('两', "2");
        numberMap.put('三', "3");
        numberMap.put('仨', "3");
        numberMap.put('四', "4");
        numberMap.put('五', "5");
        numberMap.put('六', "6");
        numberMap.put('七', "7");
        numberMap.put('八', "8");
        numberMap.put('九', "9");
        Map<Character, String> numberBit = new HashMap<Character, String>();
        numberBit.put('十', "10");
        numberBit.put('百', "100");
        numberBit.put('千', "1000");
        numberBit.put('万', "10000");
        long number = 0;
        char[] arrNumber = chinaNumberStr.toCharArray();
        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < arrNumber.length; i++) {
            if(numberMap.get(arrNumber[i])== null && numberBit.get(arrNumber[i])== null ) {
                if(number == 0){
                    result.append(arrNumber[i]);
                    continue;
                }
                else{
                    result.append(number);
                    result.append(arrNumber[i]);
                    number = 0; // reset number for obtaining the next number
                    continue;
                }
            }
            char num = arrNumber[i];
            if (i + 1 < arrNumber.length && numberBit.containsKey(arrNumber[i + 1])) {// 判断后面的汉字是否是位数(十百千万)
                if (numberMap.containsKey(num)) {// 如果当前的汉字不是位数
                    if (i + 2 < arrNumber.length && arrNumber[i + 1] == '十' && numberMap.containsKey(arrNumber[i + 2])) {
                        number = number + Integer.parseInt(numberMap.get(num)) * Integer.parseInt(numberBit.get(arrNumber[i + 1]));
                        number = number + Integer.parseInt(numberMap.get(arrNumber[i + 2]));
                        // number = number * Integer.parseInt(numberBit.get(arrNumber[i + 3]));
                        i = i + 2;
                    } else {// 取出当前对应的数字*位数对应倍数累加在number上
                        number = number + Integer.parseInt(numberMap.get(num)) * Integer.parseInt(numberBit.get(arrNumber[i + 1]));
                        i = i + 1;
                    }
                } else if (numberBit.containsKey(num)) {// 如果当前的汉字是位数，即当前的汉字和后一个汉字都是位数(比如百万)
                    //则用number直接*后一个位数
                    number = number * Integer.parseInt(numberBit.get(arrNumber[i + 1]));
                }
            } else if (numberMap.containsKey(num)) {
                number = number + Integer.parseInt(numberMap.get(num));
            } else if ((num == '十' )){//“十”特殊处理 当出现十八之类的 十在前面的情况
                number = Integer.parseInt(numberBit.get('十'));
            }
        }
        if(number != 0){
            result.append(number);
        }
        return result.toString();
    }

    String yearNum(String chinese) { //
        if(chinese.contains("十") || chinese.contains("百") || chinese.contains("千") || chinese.contains("万")){
            return chinese;
        }
        Map<Character, String> numberMap = new HashMap<Character, String>();
        numberMap.put('零', "0");
        numberMap.put('一', "1");
        numberMap.put('二', "2");
        numberMap.put('两', "2");
        numberMap.put('三', "3");
        numberMap.put('四', "4");
        numberMap.put('五', "5");
        numberMap.put('六', "6");
        numberMap.put('七', "7");
        numberMap.put('八', "8");
        numberMap.put('九', "9");
        String temp;
        Pattern p = Pattern.compile("[零一二两三四五六七八九]+(年)");
        Matcher m = p.matcher(chinese);
        StringBuffer num = new StringBuffer("");
        if(m.find()){
            temp = chinese.substring(0,chinese.indexOf('年'));
            for(int i=0; i<temp.length(); i++) {
                    if(numberMap.containsKey(chinese.charAt(i)) )
                            num.append(numberMap.get(chinese.charAt(i)));
                    else
                        num.append(chinese.charAt(i));
            }
        }
        else
            return chinese;
        num.append(chinese.substring(chinese.indexOf('年'),chinese.length()));
        return num.toString();
    }
}

