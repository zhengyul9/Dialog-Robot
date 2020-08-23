package ai.hual.labrador.nlu.preprocessors;


import ai.hual.labrador.nlu.Preprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by Administrator on 2018/10/22.
 */
class AgeRewriterTest {


    @Test
    void testPreprocess() {

//        List list=new ArrrayList

        Preprocessor preprocessor = new AgeRewriter();
//        白盒
        assertEquals("泰康劳做成年小孩的净值是多少", preprocessor.preprocess("泰康劳做20岁小孩的净值是多少"));
        assertEquals("我成年了怎么买保险", preprocessor.preprocess("我20了怎么买保险"));
        assertEquals("今年未成年怎么买保险", preprocessor.preprocess("今年16怎么买保险"));
        assertEquals("投保人未成年了", preprocessor.preprocess("投保人16了"));
        assertEquals("我未成年了", preprocessor.preprocess("我13周岁了"));


        assertEquals("投保人怎么投保", preprocessor.preprocess("投保人怎么投保"));
        assertEquals("20个投保人怎么30个投保", preprocessor.preprocess("20个投保人怎么30个投保"));
        assertEquals("20个投保人怎么在成年投保", preprocessor.preprocess("20个投保人怎么在30岁投保"));
        assertEquals("成年的投保人怎么20天投保", preprocessor.preprocess("18岁的投保人怎么20天投保"));

        assertEquals("12154", preprocessor.preprocess("12154"));
        assertEquals("12154元", preprocessor.preprocess("12154元"));
        assertEquals("没有12154", preprocessor.preprocess("没有12154"));
        assertEquals("成年的投保人怎么在成年投保", preprocessor.preprocess("18岁的投保人怎么在20岁投保"));
        assertEquals("我家孩子未成年，但是成年的投保人怎么在成年投保", preprocessor.preprocess("我家孩子17岁，但是30周岁的投保人怎么在20岁投保"));

    }
}
