package ai.hual.labrador.faq.utils;

/**
 * 用来描述两个int->int的映射关系
 * 用它当做key，所以需要重写hashcode和equals
 * 需要打印所以重写toString
 */
public class IntegerPair {

    private Integer former;
    private Integer latter;

    public IntegerPair(Integer former, Integer latter) {
        this.former = former;
        this.latter = latter;
    }

    public Integer getFormer() {
        return former;
    }

    public void setFormer(Integer former) {
        this.former = former;
    }

    public Integer getLatter() {
        return latter;
    }

    public void setLatter(Integer latter) {
        this.latter = latter;
    }

    @Override
    public int hashCode() {
        //reference http://blog.csdn.net/sunmenggmail/article/details/18660699
        final int prime = 31;
        int result = 1;
        result = prime * result;
        result = prime * result;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IntegerPair other = (IntegerPair) obj;
        return former.equals(other.former) && latter.equals(other.latter);
    }

    @Override
    public String toString() {
        return former + "->" + latter;
    }

}
