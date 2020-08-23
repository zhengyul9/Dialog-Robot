package ai.hual.labrador.faq;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FAQDetectionMethodTest {
    @Test
    void countChineseAndEnglistStrInput1() throws Exception {
        String content = "hello,kitty !";
        assertEquals(2, FAQDetectionMethod.countChineseAndEnglistStrInput(content));
    }

    @Test
    void countChineseAndEnglistStrInput2() throws Exception {
        String content = "什么叫健康检查异常？";
        assertEquals(10, FAQDetectionMethod.countChineseAndEnglistStrInput(content));
    }

    @Test
    void countChineseAndEnglistStrInput3() throws Exception {
        String content = "关于“企业版资料未上传或审核未通过”的说明";
        assertEquals(21, FAQDetectionMethod.countChineseAndEnglistStrInput(content));
    }

    @Test
    void countChineseAndEnglistStrInput4() throws Exception {
        String content = "老板和小姨子跑了全场9.9";
        assertEquals(11, FAQDetectionMethod.countChineseAndEnglistStrInput(content));
    }

    @Test
    void countChineseAndEnglistStrInput5() throws Exception {
        String content = "请拨打客服电话01014395948275";
        assertEquals(8, FAQDetectionMethod.countChineseAndEnglistStrInput(content));
    }

    @Test
    void countChineseAndEnglistStrInput6() throws Exception {
        String content = "<p><span style=\"font-family: 微软雅黑, &quot;" +
                "Microsoft YaHei&quot;;\"></span></p ><p><strong><spa" +
                "n style=\"font-family: 微软雅黑, &quot;Microsoft YaH" +
                "ei&quot;;\">填写路径：</span></strong></p ><p><span " +
                "style=\"font-family: 微软雅黑, &quot;Microsoft YaHei" +
                "&quot;;\">认证中心—文书送达地址。&nbsp;</span></p ><p" +
                "><strong><span style=\"font-family: 微软雅黑, &quot;" +
                "Microsoft YaHei&quot;;\">说明：</span></strong></p >" +
                "<p><span style=\"font-family: 微软雅黑, &quot;Micros" +
                "oft YaHei&quot;;\">1</span><span style=\"font-family" +
                ": 微软雅黑, &quot;Microsoft YaHei&quot;;\">.为了更好地" +
                "保障法律权利，建议填写方便接收函件的邮寄地址，如您只填写了" +
                "电子邮箱，发生诉讼时，将以户籍地址作为法律文书邮寄地址；送" +
                "达地址可更改。</span></p ><p><span style=\"font-family:" +
                " 微软雅黑, &quot;Microsoft YaHei&quot;;\">2.电子邮箱里需" +
                "要有@符号，最多可输入50个字符；邮寄地址最多输入60字。</span></p >";
        assertEquals(125, FAQDetectionMethod.countChineseAndEnglistStrInput(FAQDetectionMethod.replaceStripHtml(content)));
    }

    @Test
    void countChineseAndEnglistStrInput7() throws Exception {
        String content = "<p><span style=\"font-family: 微软雅黑, &quot;" +
                "Microsoft YaHei&quot;;\">支持【余额】支付和【银行卡】支付。&nbsp</span></p >";
        assertEquals(18, FAQDetectionMethod.countChineseAndEnglistStrInput(FAQDetectionMethod.replaceStripHtml(content)));

    }

    @Test
    void countChineseAndEnglistStrInput8() throws Exception {
        String content = null;
        assertEquals(0, FAQDetectionMethod.countChineseAndEnglistStrInput(content));
    }
}