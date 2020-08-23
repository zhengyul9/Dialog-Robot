package ai.hual.labrador.nlu.pinyin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PinyinImplTest {

    private static PinyinImpl pinyin;

    @BeforeAll
    static void setup() {
        pinyin = new PinyinImpl();
    }

    @Test
    public void WordWithSpecialCharAppendSpaceTest() {

        String input = "* *% ";
        String result = pinyin.getPinyin(input);
        assertEquals("*  * %", result);
    }

    @Test
    public void WordWithArabicNumberAndCharacterTest() {

        String input = "ATV1%";
        String result = pinyin.getPinyin(input);
        assertEquals("A T V 1 %", result);
    }

    @Test
    public void ChineseToPinyinTest() {

        String input = "保险";
        String result = pinyin.getPinyin(input);
        assertEquals("bao3 xian3", result);
    }

    @Test
    void ChineseToPinyinWithPunctuationTest() {

        String input = "鱼香肉丝,好";
        String result = pinyin.getPinyin(input);
        assertEquals("yu2 xiang1 rou4 si1 , hao3", result);
    }

    @Test
    void getPinyinRobustMapTest() {

        ListMultimap<String, PinyinScoreTuple> pinyinMap;
        pinyinMap = PinyinImpl.getPinyinRobustMap("pinyin_prob_file");
        assertTrue(pinyinMap.size() != 0);
    }

    @Test
    void tryRemove() {
        ListMultimap<String, String> mm = ArrayListMultimap.create();
        mm.put("a", "aa");
        mm.put("a", "bb");

        mm.remove("a", "aa");
    }

//    /**
//     * Test example to convert a whole file to pinyin.
//     */
//    @Test
//    public void PinyinImplConvertToPinyinTest() {
//
//        PinyinImpl pinyin = new PinyinImpl();
//
//        List<String> writeList = new ArrayList<>();
//
//        String file = "out.txt";
//        try {
//            URI readUri = ClassLoader.getSystemResource(file).toURI();
//            Path readPath = Paths.get(readUri);
//            Stream<String> stream = Files.lines(readPath);
//            String writeFileName = readPath.toString()
//                    .replaceAll("\\.txt", "") + "-pinyin.txt";
//            System.out.println("write file name: " + writeFileName);
//
//            stream.forEach(line -> {
//                String word = line.split("\\t")[0];
//                System.out.println("computing pinyin ...");
//                String writeLine = word + "       " + pinyin.getPinyin(word);
//                System.out.println("done");
//                writeList.add(writeLine);
//                try {
//                    Files.write(Paths.get(writeFileName), Arrays.asList(writeLine), StandardCharsets.UTF_8,
//                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
////                    Files.write(Paths.get(writeFileName), writeLine.getBytes());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//        } catch(IOException | URISyntaxException ex) {
//            System.out.println(ex.toString());
//        }
//        writeList.forEach(System.out::println);
//    }
}