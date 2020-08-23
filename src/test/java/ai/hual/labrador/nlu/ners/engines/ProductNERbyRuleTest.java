package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.nlu.ners.NERResource;
import ai.hual.labrador.nlu.ners.NERResult;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProductNERbyRuleTest {
    private static final List<String> PRODUCTNER_WORDLIST = Arrays.asList("ambiguous_prefix","ambiguous_suffix","kernel","limited","prefix","suffix","properties","standard_entity","plans","skips");
    private static final List<String> PRODUCTNER_REGEXLIST = Arrays.asList("regex_limited","tail_minus_one_and_tail_cut_tail_patterns","tail_minus_one_and_tail_stop_patterns","tail_plus_one_patterns","adjust_segment_patterns");

    private Map<String,Map<String,NERResource>> loadResource() {
        Map<String, Map<String, NERResource>> resources = new HashMap<>();
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
    void getMapping() {
        ProductNERbyRule p = new ProductNERbyRule(loadResource().get("ProductNER"));
        String validation = "{time=[forward, backward, whole_without_kernel, invalid_single_word_as_entity]}";
        assertEquals(validation,p.getMapping().toString());
    }

    @Test
    void cut() {
        ProductNERbyRule p = new ProductNERbyRule(loadResource().get("ProductNER"));
        String validation = "[泰康, 金满仓, b3, 年交, 生存, 金, 怎么, 返]";
        assertEquals(validation,p.cut("泰康金满仓B3年交生存金怎么返").toString());
    }

    @Test
    void testAdjustSegments() {
        ProductNERbyRule p = new ProductNERbyRule(loadResource().get("ProductNER"));
        //String validation = "[泰康, 金满仓, b3, 年交, 生存, 金, 怎么, 返]";
        //assertEquals(validation,p.cut("泰康e顺铁路乘客意外险保什么").toString());
        String query = "泰康e顺铁路乘客意外险保什么";
        List<String> segments = p.cut(query);
        p.adjustSegments(query,segments, 0, 0, false);
        System.out.println(segments.toString());
    }

    @Test
    void recognizer() {
        String text = "我想请问一下卓越人生这个产品还有鑫享人生啊";
        ProductNERbyRule p = new ProductNERbyRule(loadResource().get("ProductNER"));
        //String validation = "[{standard=泰康健康尊享医疗保险【个险】【与鑫享人生关联销售】/泰康鑫享人生年金保险（分红型）, realEnd=20, pos=product, realStart=16, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}, {standard=泰康卓越人生终身寿险（万能型）/泰康附加卓越人生提前给付重大疾病保险, realEnd=10, pos=product, realStart=6, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}]";
        String validation = "[{realEnd=20, pos=人寿保险_产品, realStart=16, recognizer=ProductNER, text=鑫享人生, entity=泰康健康尊享医疗保险【个险】【与鑫享人生关联销售】/泰康鑫享人生年金保险（分红型）, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}, {realEnd=10, pos=人寿保险_产品, realStart=6, recognizer=ProductNER, text=卓越人生, entity=泰康卓越人生终身寿险（万能型）/泰康附加卓越人生提前给付重大疾病保险, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}]";
        List<NERResult.Candidate> result = p.recognizer(null,text);
        assertEquals(validation,result.toString());
    }



    @Test
    void recognizer1() {
        String text = "泰康千里马两全保险（分红型）B款的满期金额";
        ProductNERbyRule p = new ProductNERbyRule(loadResource().get("ProductNER"));
        //String validation = "[{standard=泰康健康尊享医疗保险【个险】【与鑫享人生关联销售】/泰康鑫享人生年金保险（分红型）, realEnd=20, pos=product, realStart=16, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}, {standard=泰康卓越人生终身寿险（万能型）/泰康附加卓越人生提前给付重大疾病保险, realEnd=10, pos=product, realStart=6, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}]";
        String validation = "泰康/千里马/两全/保险/（/分红型/）/b款/的/满期/金额";
        List<NERResult.Candidate> result = p.recognizer(null,text);
        System.out.println(result.get(0).getSegments());
        assertEquals(validation,result.get(0).getSegments());
    }


    @Test
    void recognizer2() {
        String text = "泰康e理财B款终身寿险（投资连结型）最低追加金额是多少";
        ProductNERbyRule p = new ProductNERbyRule(loadResource().get("ProductNER"));
        //String validation = "[{standard=泰康健康尊享医疗保险【个险】【与鑫享人生关联销售】/泰康鑫享人生年金保险（分红型）, realEnd=20, pos=product, realStart=16, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}, {standard=泰康卓越人生终身寿险（万能型）/泰康附加卓越人生提前给付重大疾病保险, realEnd=10, pos=product, realStart=6, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}]";
        String validation = "[{realEnd=18, pos=人寿保险_产品, realStart=0, recognizer=ProductNER, text=泰康e理财B款终身寿险（投资连结型）, entity=Not found, segments=泰康/e理财/b款/终身寿险/（/投资连结型/）/最低/追加/金额/是/多少}, {realEnd=17, pos=人寿保险_产品, realStart=0, recognizer=ProductNER, text=泰康e理财B款终身寿险（投资连结型, entity=Not found, segments=泰康/e理财/b款/终身寿险/（/投资连结型/）/最低/追加/金额/是/多少}]";
        List<NERResult.Candidate> result = p.recognizer(null,text);
        System.out.println(result);
        assertEquals(validation,result.toString());
    }

    @Test
    void recognizer3() {
        String text = "卓越财富B款有万能账户吗？";
        ProductNERbyRule p = new ProductNERbyRule(loadResource().get("ProductNER"));
        //String validation = "[{standard=泰康健康尊享医疗保险【个险】【与鑫享人生关联销售】/泰康鑫享人生年金保险（分红型）, realEnd=20, pos=product, realStart=16, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}, {standard=泰康卓越人生终身寿险（万能型）/泰康附加卓越人生提前给付重大疾病保险, realEnd=10, pos=product, realStart=6, recognizer=ProductNER, entity=null, segments=我/想/请问/一下/卓越人生/这个/产品/还有/鑫享人生/啊}]";
        String validation = "[{realEnd=6, pos=人寿保险_产品, realStart=0, recognizer=ProductNER, text=卓越财富B款, entity=Not found, segments=卓越财富/b款/有/万能账户/吗/？}]";
        List<NERResult.Candidate> result = p.recognizer(null,text);
        System.out.println(result);
        assertEquals(validation,result.toString());
    }

}