package ai.hual.labrador.nlu.annotators;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.*;
import ai.hual.labrador.nlu.ners.NERResource;
import ai.hual.labrador.nlu.ners.NERResult;
import javafx.util.Pair;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NERAnnotatorTest {

    private static DictModel dictModel;
    private static GrammarModel grammarModel;
    private static final List<String> PRODUCTNER_WORDLIST = Arrays.asList("ambiguous_prefix","ambiguous_suffix","kernel","limited","prefix","suffix","properties","standard_entity","plans","skips");
    private static final List<String> PRODUCTNER_REGEXLIST = Arrays.asList("regex_limited","tail_minus_one_and_tail_cut_tail_patterns","tail_minus_one_and_tail_stop_patterns","tail_plus_one_patterns","adjust_segment_patterns");

    private void genDictModelAndGrammarModel() {
//        dictModel = new DictModel(Arrays.asList(
//                new Dict("__kernel__", "a")
//        ));
//
//        grammarModel = new GrammarModel(Arrays.asList(
//                new Grammar(GrammarType.INTENT_REGEX, "intent",
//                        "ct", 1.0f)
//        ));
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
        Map<String, NERResource> productner_resources = new HashMap<>();
        List<List<String>> total = Arrays.asList(PRODUCTNER_WORDLIST, PRODUCTNER_REGEXLIST);
        List<Dict> dicts = new ArrayList<>();
        List<Grammar> grammars = new ArrayList<>();
        for (int i = 0; i < total.size(); ++i) {
            List<String> list = total.get(i);
            for (String wordlist : list) {
                try {
                    InputStream inputstream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_ProductNERbyRule/" + wordlist + ".txt");
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                    if(i==0){
                        br.lines().forEach(line -> {
                            line = line.trim();
                            if (line.length() == 0) //ignore blank line
                                return;
                            dicts.add(new Dict("__"+wordlist+"__",line));
                        });
                        br.close();
                    }
                    if(i == 1)
                    {
                        br.lines().forEach(line -> {
                            line = line.trim();
                            if (line.length() == 0) //ignore blank line
                                return;
                            grammars.add(new Grammar(GrammarType.INTENT_REGEX, wordlist,
                                    line, 1.0f));
                        });
                        br.close();
                    }

                } catch (IOException | NullPointerException e) {
                    //            throw new NLUException("Error, fail to read xlsx data file");
                }
            }
        }
        dictModel = new DictModel(dicts);
        grammarModel = new GrammarModel(grammars);
    }

    // TODO test from excel is a bad practice
    /**
     * @deprecated
     */
//    @Test
//    void annotateFromExcel() {
//        genDictModelAndGrammarModel();
//        List<String> queries = new ArrayList<>();
//        List<String> validations = new ArrayList<>();
//        try {
//            InputStream inputstream = getClass().getResourceAsStream("/nlu/exception.xlsx");
//            XSSFWorkbook wb = new XSSFWorkbook(inputstream);
//            XSSFSheet sheet = wb.getSheetAt(0);
//            int rowlength = sheet.getLastRowNum();
//            for (int j = 1; j <= rowlength; j++) {
//                XSSFRow row = sheet.getRow(j);
//                queries.add(row.getCell(0) == null ? "":row.getCell(0).toString());
//                validations.add(row.getCell(2).toString());
//            }
//            inputstream.close();
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            //throw new NLUException("Error, fail to read xlsx data file");
//        }
//        Properties properties = new Properties();
//        properties.setProperty("nlu.ners","ProductNER");
//        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
//        for(int i=0;i<queries.size();++i){
//            if (i != 142)
//                continue;
//            String query = queries.get(i);
//            QueryAct queryAct = new QueryAct();
//            queryAct.setQuery(query);
//            try{
//                List<QueryAct> queryActList = n.annotate(queryAct);
//                assertEquals(queryActList.toString().trim().replaceAll("\n",""),validations.get(i).trim());
//            }catch(Exception e){
//                System.out.println(e.getMessage());
//                assertEquals("here is an error","");
//            }
//
//        }
//
//    }

    @Test
    void annotate() {
        //String text = "卓越人生二零零七年2月15号";
        genDictModelAndGrammarModel();
        String text = "我想请问一下卓越人生这个产品还有鑫享人生啊";
        Properties properties = new Properties();
        properties.setProperty("nlu.ners","ProductNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        QueryAct queryAct = new QueryAct();
        queryAct.setQuery(text);
        List<QueryAct> queryActList = n.annotate(queryAct);
        System.out.println(queryActList.toString());
        String validation = "[{query:我想请问一下卓越人生这个产品还有鑫享人生啊,pQuery:我想请问一下{{人寿保险_产品}}这个产品还有鑫享人生啊,intent:null,slots:[人寿保险_产品=(卓越人生),6-10,6-17, ],score:1.2690587062858836}, {query:我想请问一下卓越人生这个产品还有鑫享人生啊,pQuery:我想请问一下卓越人生这个产品还有{{人寿保险_产品}}啊,intent:null,slots:[人寿保险_产品=(鑫享人生),16-20,16-27, ],score:1.2690587062858836}, {query:我想请问一下卓越人生这个产品还有鑫享人生啊,pQuery:我想请问一下{{人寿保险_产品}}这个产品还有{{人寿保险_产品}}啊,intent:null,slots:[人寿保险_产品=(卓越人生),6-10,6-17, 人寿保险_产品=(鑫享人生),16-20,23-34, ],score:1.6105100000000006}]";
        assertEquals(validation,queryActList.toString());
    }


    @Test
    void annotate1() {
        //String text = "卓越人生二零零七年2月15号";
        genDictModelAndGrammarModel();
        String text = "卓越财富2007保险责任";
        //String text = "附如意宝意外伤害(2014)的附加险不想要了可以吗？";
        Properties properties = new Properties();
        properties.setProperty("nlu.ners","ProductNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        QueryAct queryAct = new QueryAct();
        queryAct.setQuery(text);
        List<QueryAct> queryActList = n.annotate(queryAct);
        //String validation = "[{query:卓越财富2007保险责任,pQuery:{{product}}责任,intent:null,slots:[product=(卓越财富2007保险),0-10,0-10, ],score:1.0}, {query:卓越财富2007保险责任,pQuery:{{product}}保险责任,intent:null,slots:[product=(卓越财富2007),0-8,0-8, ],score:1.0}]";
        String validation = "[{query:卓越财富2007保险责任,pQuery:{{人寿保险_产品}}责任,intent:null,slots:[人寿保险_产品=(卓越财富2007保险),0-10,0-11, ],score:2.2482149107665275}, {query:卓越财富2007保险责任,pQuery:{{人寿保险_产品}}保险责任,intent:null,slots:[人寿保险_产品=(卓越财富2007),0-8,0-11, ],score:1.858028851873163}]";
        assertEquals(validation,queryActList.toString());
    }

    @Test
    void annotate2() {
        //String text = "卓越人生二零零七年2月15号";
        genDictModelAndGrammarModel();
        String text = "保险责任";
        Properties properties = new Properties();
        properties.setProperty("nlu.ners","ProductNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        QueryAct queryAct = new QueryAct();
        queryAct.setQuery(text);
        List<QueryAct> queryActList = n.annotate(queryAct);
        //String validation = "[{query:卓越财富2007保险责任,pQuery:{{product}}责任,intent:null,slots:[product=(卓越财富2007保险),0-10,0-10, ],score:1.0}, {query:卓越财富2007保险责任,pQuery:{{product}}保险责任,intent:null,slots:[product=(卓越财富2007),0-8,0-8, ],score:1.0}]";
        String validation = "[{query:保险责任,pQuery:保险责任,intent:null,slots:[],score:1.0}]";
        assertEquals(validation,queryActList.toString());
    }

  @Test
	void annotate3() {
	//String text = "卓越人生二零零七年2月15号";
        genDictModelAndGrammarModel();
        String text = "15岁的男孩和司机怎么买鑫享人生?";
        Properties properties = new Properties();
        properties.setProperty(NERAnnotator.SUBNERS_PROP_NAME,"ProductNER,PersonNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        QueryAct queryAct = new QueryAct();
        queryAct.setQuery(text);
        List<QueryAct> queryActList = n.annotate(queryAct);
//        System.out.println(queryActList.toString());
        String validation = "[{query:15岁的男孩和司机怎么买鑫享人生?,pQuery:{{人物}}和司机怎么买{{人寿保险_产品}}?,intent:null,slots:[人物=(15岁的男孩),0-6,0-6, 人寿保险_产品=(鑫享人生),12-16,12-23, ],score:1.948717100000001}, {query:15岁的男孩和司机怎么买鑫享人生?,pQuery:15岁的男孩和{{人物}}怎么买{{人寿保险_产品}}?,intent:null,slots:[人物=(司机),7-9,7-13, 人寿保险_产品=(鑫享人生),12-16,16-27, ],score:1.3310000000000004}, {query:15岁的男孩和司机怎么买鑫享人生?,pQuery:{{人物}}和{{人物}}怎么买{{人寿保险_产品}}?,intent:null,slots:[人物=(15岁的男孩),0-6,0-6, 人物=(司机),7-9,7-13, 人寿保险_产品=(鑫享人生),12-16,16-27, ],score:2.0438317370604793}]";
        assertEquals(validation, queryActList.toString());
    }

  @Test  
  void annotate4() {
        NumAnnotator na = new NumAnnotator();
        String text = "我想请问20下卓越人生这个20产品鑫享人生";
        List<QueryAct> queryActs = na.annotate(new QueryAct(text));
        genDictModelAndGrammarModel();

        Properties properties = new Properties();
        properties.setProperty("nlu.ners","ProductNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        List<QueryAct> result = new ArrayList<>();
        for(QueryAct q: queryActs){
            List<QueryAct> x = n.annotate(q);
            result.addAll(x);
        }
        String validation = "[{query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问{{数字}}下{{人寿保险_产品}}这个{{数字}}产品{{人寿保险_产品}},intent:null,slots:[人寿保险_产品=(卓越人生),7-11,11-22, 人寿保险_产品=(鑫享人生),17-21,32-43, 数字=(20.0),4-6,4-10, 数字=(20.0),13-15,24-30, ],score:1.7715610000000008}, {query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问{{数字}}下{{人寿保险_产品}}这个20产品鑫享人生,intent:null,slots:[人寿保险_产品=(卓越人生),7-11,11-22, 数字=(20.0),4-6,4-10, ],score:1.3310000000000004}, {query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问{{数字}}下卓越人生这个20产品{{人寿保险_产品}},intent:null,slots:[人寿保险_产品=(鑫享人生),17-21,21-32, 数字=(20.0),4-6,4-10, ],score:1.3310000000000004}, {query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问{{数字}}下{{人寿保险_产品}}这个20产品{{人寿保险_产品}},intent:null,slots:[人寿保险_产品=(卓越人生),7-11,11-22, 人寿保险_产品=(鑫享人生),17-21,28-39, 数字=(20.0),4-6,4-10, ],score:1.6891171380665115}, {query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问20下{{人寿保险_产品}}这个{{数字}}产品{{人寿保险_产品}},intent:null,slots:[人寿保险_产品=(卓越人生),7-11,7-18, 人寿保险_产品=(鑫享人生),17-21,28-39, 数字=(20.0),13-15,20-26, ],score:1.6891171380665115}, {query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问20下{{人寿保险_产品}}这个20产品鑫享人生,intent:null,slots:[人寿保险_产品=(卓越人生),7-11,7-18, ],score:1.2690587062858836}, {query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问20下卓越人生这个20产品{{人寿保险_产品}},intent:null,slots:[人寿保险_产品=(鑫享人生),17-21,17-28, ],score:1.2690587062858836}, {query:我想请问20下卓越人生这个20产品鑫享人生,pQuery:我想请问20下{{人寿保险_产品}}这个20产品{{人寿保险_产品}},intent:null,slots:[人寿保险_产品=(卓越人生),7-11,7-18, 人寿保险_产品=(鑫享人生),17-21,24-35, ],score:1.6105100000000006}]";

        assertEquals(validation,result.toString());
    }

    @Test
    void annotate5() {
        NumAnnotator na = new NumAnnotator();
        String text = "男孩";
        List<QueryAct> queryActs = na.annotate(new QueryAct(text));
        genDictModelAndGrammarModel();

        Properties properties = new Properties();
        properties.setProperty("nlu.ners","PersonNER,ProductNER,OrganizationNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        List<QueryAct> qs = n.annotate(new QueryAct(text));
        String validation = "[{query:男孩,pQuery:{{人物}},intent:null,slots:[人物=(男孩),0-2,0-6, ],score:1.0488088481701516}]";
        assertEquals(validation,qs.toString());
    }

    @Test
    void annotate6() {
        NumAnnotator na = new NumAnnotator();
        String text = "男孩  天坛医院可以报销吗";
        List<QueryAct> queryActs = na.annotate(new QueryAct(text));
        genDictModelAndGrammarModel();

        Properties properties = new Properties();
        properties.setProperty("nlu.ners","PersonNER,ProductNER,OrganizationNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        List<QueryAct> qs = n.annotate(new QueryAct(text));
        String validation = "[{query:男孩  天坛医院可以报销吗,pQuery:{{人物}}  {{机构}}可以报销吗,intent:null,slots:[人物=(男孩),0-2,0-6, 机构=(天坛医院),4-8,8-14, ],score:1.3310000000000004}]";
        assertEquals(validation,qs.toString());
    }


    @Test
    void testAnnotateWithOtherAnnotator() {
        NumAnnotator na = new NumAnnotator();
        String text = "有一个男孩  天坛医院可以报销吗";
        QueryAct queryAct = na.annotate(new QueryAct(text)).get(0);
        genDictModelAndGrammarModel();

        Properties properties = new Properties();
        properties.setProperty("nlu.ners","OrganizationNER,PersonNER");
        NERAnnotator n = new NERAnnotator(dictModel,grammarModel,properties);
        List<QueryAct> qs = n.annotate(queryAct);
        String validation = "[{query:有一个男孩  天坛医院可以报销吗,pQuery:有{{数字}}个{{人物}}  {{机构}}可以报销吗,intent:null,slots:[人物=(男孩),3-5,8-14, 机构=(天坛医院),7-11,16-22, 数字=(1.0),1-2,1-7, ],score:1.3576200000000005}]";
        assertEquals(validation,qs.toString());
    }

    @Test
    void testAnnotateWithOtherAnnotatorWithNoResult() {
            NumAnnotator na = new NumAnnotator();
            String text = "有一个可以报销吗";
            QueryAct queryAct = na.annotate(new QueryAct(text)).get(0);
            genDictModelAndGrammarModel();

            Properties properties = new Properties();
            properties.setProperty("nlu.ners", "OrganizationNER,PersonNER");
            NERAnnotator n = new NERAnnotator(dictModel, grammarModel, properties);
            List<QueryAct> qs = n.annotate(queryAct);
            System.out.println(text + "&" + qs.toString());
            String validation = "[{query:有一个可以报销吗,pQuery:有{{数字}}个可以报销吗,intent:null,slots:[数字=(1.0),1-2,1-7, ],score:1.02}]";
            assertEquals(validation, qs.toString());
    }

    @Test
    void testAnnotateWithOtherAnnotatorWithSingleTextOnly() {

        String text = "您是今天下午三点二十分方便，还是明天上午十点方便？";
        NumAnnotator na = new NumAnnotator();
        List<QueryAct> queryAct = na.annotate(new QueryAct(text));
        genDictModelAndGrammarModel();
        List<QueryAct> qs = null;

        Properties properties = new Properties();
        properties.setProperty("nlu.ners", "TimeNER");//,OrganizationNER,PersonNER");
        //properties.setProperty("nlu.ners", "ProductNER");//,OrganizationNER,PersonNER");
        NERAnnotator n = new NERAnnotator(dictModel, grammarModel, properties);
        //for(QueryAct q: queryAct){ // to avoid numAnnotate changing the query
            qs = n.annotate(queryAct.get(queryAct.size()-1));
        //}
        Collections.reverse(qs);
        for(QueryAct i : qs)
            System.out.println(i);
            System.out.println(text + "&" + qs.toString());
            String validation = "[{query:您是今天下午三点二十分方便，还是明天上午十点方便？,pQuery:您是{{时间点}}方便，还是{{时间点}}方便？,intent:null,slots:[时间点=(今天下午03:20:00),2-11,2-9, 时间点=(明天上午10:00:00),16-22,14-21, ],score:3.138428376721003}, {query:您是今天下午三点二十分方便，还是明天上午十点方便？,pQuery:您是今天下午三点二十分方便，还是{{时间点}}方便？,intent:null,slots:[时间点=(明天上午10:00:00),16-22,16-23, ],score:1.5355610346059194}, {query:您是今天下午三点二十分方便，还是明天上午十点方便？,pQuery:您是{{时间点}}方便，还是明天上午十点方便？,intent:null,slots:[时间点=(今天下午03:20:00),2-11,2-9, ],score:2.0438317370604793}]";
            assertEquals(validation, qs.toString());
    }
//   @Test
//    void testAnnotateWithOtherAnnotatorWithAllNERs() {
//        final String RESOURCES_DIR = "Resources_TimeNERbyRule";
//        final String USED_TEST_FILE = RESOURCES_DIR + "/" + "test.txt";
//        List<String> test_stream = this.getTestList(USED_TEST_FILE);
//        int counter = 1;
//        for(String text:test_stream) {
//            NumAnnotator na = new NumAnnotator();
//            List<QueryAct> queryAct = na.annotate(new QueryAct(text));
//            genDictModelAndGrammarModel();
//            List<QueryAct> qs = null;
//
//            Properties properties = new Properties();
//            properties.setProperty("nlu.ners", "TimeNER");//,OrganizationNER,PersonNER");
//            NERAnnotator n = new NERAnnotator(dictModel, grammarModel, properties);
//            //for(QueryAct q: queryAct){  // to avoid numAnnotate changing the query
//                qs = n.annotate(queryAct.get(queryAct.size()-1));
//            //}
//            Collections.reverse(qs);
//             //write into a file line by line, replace with the correct file path please
//            writeFile("D:\\北京实习\\NER\\我的code\\NER测试/Test.txt",  text + "&" + qs.toString());
//            // track the writing progress
//            System.out.println(counter++);
//            //System.out.println(text + "&" + qs.toString());
//
//        }
//    }

    @Test
    void getValidResultsStruct() {
        Properties properties = new Properties();
        NERAnnotator n = new NERAnnotator(null,null,properties);
        List<List<Integer>> x = new ArrayList<List<Integer>>(){{
            add(new ArrayList<Integer>(){{add(0);add(2);}});
            add(new ArrayList<Integer>(){{add(0);add(1);}});
            add(new ArrayList<Integer>(){{add(1);add(4);}});
            add(new ArrayList<Integer>(){{add(1);add(5);}});
        }};
//        List<List<Integer>> x = new ArrayList<List<Integer>>(){{
//            add(new ArrayList<Integer>(){{add(6);add(10);}});
//            add(new ArrayList<Integer>(){{add(16);add(20);}});
//        }};
        List<NERResult.Candidate> candidates = new ArrayList<>();
        for(List<Integer> l: x){
            NERResult.Candidate candidate = new NERResult.Candidate(l.get(0),l.get(1),"","","","","");
            candidates.add(candidate);
        }
        NERResult ne = new NERResult("ABCDE",candidates);
        List<Pair<List<Integer>, Integer>> valids = n.getValidResultsStruct(ne);
        String validation = "[[0]=2, [1]=1, [2]=4, [1, 2]=4, [3]=5, [1, 3]=5]";
        assertEquals(validation,valids.toString());
    }

    public List<String> getTestList(String file) {
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
            //logger.debug("getWordList finished in PersonNER: " + result.toString());
            return result;
        } catch (IOException ex) {
            //logger.error("getWordList error in PersonNER.");
            throw new NLUException("Could not find file " + file);
        }
    }

    public static void writeFile(String fileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
