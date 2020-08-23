package ai.hual.labrador.nlu.ners;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NERModInitiatorTest {
    private static final List<String> PRODUCTNER_WORDLIST = Arrays.asList("ambiguous_prefix","ambiguous_suffix","kernel","limited","prefix","suffix","properties","standard_entity","plans","skips");
    private static final List<String> PRODUCTNER_REGEXLIST = Arrays.asList("regex_limited","tail_minus_one_and_tail_cut_tail_patterns","tail_minus_one_and_tail_stop_patterns","tail_plus_one_patterns","adjust_segment_patterns");

    private Map<String,Map<String,NERResource>> loadResource() {
        Map<String,Map<String,NERResource>> resources = new HashMap<>();
        Map<String, NERResource> productner_resources = new HashMap<>();
        List<List<String>> total = Arrays.asList(PRODUCTNER_WORDLIST, PRODUCTNER_REGEXLIST);
        for (int i = 0; i < total.size(); ++i) {
            List<String> list = total.get(i);
            for (String wordlist : list) {
                try {
                    InputStream InputStream = getClass().getClassLoader().getResourceAsStream("nlu/Resources_ProductNERbyRule/" + wordlist + ".txt");
                    BufferedReader br = new BufferedReader(new InputStreamReader(InputStream, StandardCharsets.UTF_8));
                    List<String> result = new ArrayList<>();
                    br.lines().forEach(line -> {
                        line = line.trim();
                        if (line.length() == 0) //ignore blank line
                            return;
                        result.add(line);
                    });
                    br.close();
                    if(i == 0)
                        productner_resources.put(wordlist, new NERResource(wordlist, result));
                    if(i == 1)
                    {
                        List<Pattern> patterns = result.stream().map(x -> Pattern.compile(x)).collect(Collectors.toList());
                        productner_resources.put(wordlist, new NERResource(wordlist, patterns));
                    }

                } catch (IOException | NullPointerException e) {
                    //            throw new NLUException("Error, fail to read xlsx data file");
                }
            }
        }
        resources.put("ProductNER", productner_resources);

        return resources;
    }
    @Test
    void testTopo() {
        String text = "i'm a text";
        Properties properties = new Properties();
        properties.setProperty("nlu.ners","ProductNER,PersonNER,DeprecatedNER");
        //properties.setProperty("nlu.ners", "ProductNER,PersonNER");
        Map<String,Map<String,NERResource>> resources = loadResource();
        NERModInitiator n = new NERModInitiator("ProductNER,PersonNER,DeprecatedNER",new NERResources(resources));
        String validation = "[(DeprecatedNER,0), (PersonNER,0), (TimeNER,0), (ProductNER,1)]";
        assertEquals(validation,n.topological().toString());
    }


//    @Test
//    void testDynamicImport() {
//        Properties properties = new Properties();
//        properties.setProperty("nlu.ners", "ProductNER,TimeNER");
//        new NERModInitiator(properties);
//    }

    @Test
    void process() {
        //String text = "我想请问一下卓越人生二零零七年2月15号这个产品还有鑫享人生啊";
        String text = "我想请问一下卓越人生这个产品还有鑫享人生啊";
        Properties properties = new Properties();
        //properties.setProperty("nlu.ners","ProductNER,PersonNER,DeprecatedNER");
        properties.setProperty("nlu.ners", "ProductNER");
        Map<String,Map<String,NERResource>> resources = loadResource();
        NERModInitiator n = new NERModInitiator("ProductNER",new NERResources(resources));
        List<NERResult> r = n.process(text);
        StringBuilder rb = new StringBuilder();
        for (NERResult tmpres : r) {
            rb.append(tmpres.getCandidates());
        }
        //String validation = "[{standard=泰康健康尊享医疗保险【个险】【与鑫享人生关联销售】/泰康鑫享人生年金保险（分红型）, realEnd=20, pos=product, realStart=16, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}, {standard=泰康卓越人生终身寿险（万能型）/泰康附加卓越人生提前给付重大疾病保险, realEnd=10, pos=product, realStart=6, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}]";
        String validation = "[{realEnd=20, pos=人寿保险_产品, realStart=16, recognizer=ProductNER, text=鑫享人生, entity=泰康健康尊享医疗保险【个险】【与鑫享人生关联销售】/泰康鑫享人生年金保险（分红型）, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}, {realEnd=10, pos=人寿保险_产品, realStart=6, recognizer=ProductNER, text=卓越人生, entity=泰康卓越人生终身寿险（万能型）/泰康附加卓越人生提前给付重大疾病保险, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}]";
        assertEquals(validation,rb.toString());
    }
    @Test
    void testPersonNER() {
        //String text = "我想请问一下卓越人生二零零七年2月15号这个产品还有鑫享人生啊";
        String text = "15岁的男孩和女孩10岁能投鑫享人生吗";
        Properties properties = new Properties();
        //properties.setProperty("nlu.ners","ProductNER,PersonNER,DeprecatedNER");
        properties.setProperty("nlu.ners", "PersonNER,ProductNER");
        Map<String,Map<String,NERResource>> resources = loadResource();
        NERModInitiator n = new NERModInitiator("PersonNER", new NERResources(resources));
        List<NERResult> r = n.process(text);
        StringBuilder rb = new StringBuilder();
        for (NERResult tmpres : r) {
            rb.append(tmpres.getCandidates());
        }
        String validation = "[{realEnd=6, pos=人物, realStart=0, recognizer=PersonNER, text=15岁的男孩, entity=, segments=}, {realEnd=12, pos=人物, realStart=7, recognizer=PersonNER, text=女孩10岁, entity=, segments=}]";
        assertEquals(validation, rb.toString());
    }

    @Test
    void testOrganizationNER() {
        String text = "可以在工行或招商银行办理健康百分百吗?";
        Properties properties = new Properties();
        properties.setProperty("nlu.ners", "OrganizationNER,ProductNER");
        Map<String,Map<String,NERResource>> resources = loadResource();
        NERModInitiator n = new NERModInitiator("OrganizationNER", new NERResources(resources));
        List<NERResult> r = n.process(text);
        StringBuilder rb = new StringBuilder();
        for (NERResult tmpres: r) {
            rb.append(tmpres.getCandidates());
        }
        System.out.println(rb.toString());
        String validation = "[{realEnd=5, pos=机构, realStart=3, recognizer=OrganizationNER, text=工行, entity=, segments=}, {realEnd=10, pos=机构, realStart=6, recognizer=OrganizationNER, text=招商银行, entity=, segments=}]";
        assertEquals(validation, rb.toString());
    }

    @Test
    void testTimeNER() {
        String text = "产假可以休多少天?";
        Properties properties = new Properties();
        properties.setProperty("nlu.ners", "TimeNER,ProductNER");
        Map<String,Map<String,NERResource>> resources = loadResource();
        NERModInitiator n = new NERModInitiator("TimeNER", new NERResources(resources));
        List<NERResult> r = n.process(text);
        StringBuilder rb = new StringBuilder();
        for (NERResult tmpres: r) {
            rb.append(tmpres.getCandidates());
        }
        System.out.println(rb.toString());
        String validation = "[]";
        assertEquals(validation, rb.toString());
    }

}