package ai.hual.labrador.nlu.pinyin.hanyupinyin;

public class ChinesePinyinTuple {

    public String chinese;
    public String pinyin;

    public ChinesePinyinTuple(String chinese, String pinyin) {
        this.chinese = chinese;
        this.pinyin = pinyin;
    }

    public String toString() {
        return this.chinese + "=" + this.pinyin;
    }
}
