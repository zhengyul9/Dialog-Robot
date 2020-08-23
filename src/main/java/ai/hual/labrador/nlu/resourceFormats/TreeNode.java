package ai.hual.labrador.nlu.resourceFormats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TreeNode implements Serializable {
//    子节点列表
    protected List<TreeNode> childList;
//本节点类别列表
    protected String elementClass = "";
//    本节点内容列表
    protected String elementStr = "";
//    本节点意图列表，本节点没有意图时，该节点不是叶子节点，但是，有意图时也不一定时叶子节点。需要queryElementList的长度与树的深度相等时，该节点才表示句子的末尾
    protected List<String> intent=new ArrayList<>();
//    树的层数
    protected int numOfLayer;


//    是否是叶子节点
//    叶子节点后面会跟着一个意图的字符串
//    如果不是叶子节点，那么intent就是空字符串


    public TreeNode(String elementClass, String elementStr) {
        this.elementClass = elementClass;
        this.elementStr = elementStr;
        this.childList = new ArrayList<>();
    }

    public TreeNode(){
        this.childList = new ArrayList<>();
    }

    public void setIntent(String intent) {
//        先判断意图集合中是否已经存在相同的意图，如果不存在，则需要添加意图

        if(this.intent.contains(intent)){

        }else{
            this.intent.add(intent);
        }
    }

    public void setNumOfLayer(int numOfLayer){
        this.numOfLayer=numOfLayer;
    }

    public String getElementClass() {
        return this.elementClass;
    }

    public String getElementStr() {
        return this.elementStr;
    }

    public List<String> getIntent() {
        return this.intent;
    }

    public int getNumOfLayer(){
        return this.numOfLayer;
    }

    public boolean matchElementClass(String exitedElementClass) {
        return this.elementClass.equals(exitedElementClass);
    }

    public boolean matchElementStr(String exitedElementStr) {
        return this.elementStr.equals(exitedElementStr);
    }

    public int addChildNode(String elementClass, String elementStr,SynonymyTable synonymyTable) {
        TreeNode treeNode = new TreeNode(elementClass, elementStr);

//        遍历这个节点下所有的子节点,判断是否有相同的元素,如果有,返回这个元素的索引,如果没有,就在最后添加元素,然后返回-1
        for (int i = 0; i < this.childList.size(); i++) {
//            System.out.println("匹配的结果:"+matchTemplateElement(elementClass,elementStr,i));
            if (matchTemplateElement(elementClass, elementStr, i,synonymyTable)) {
                return i;
            }
        }
        this.childList.add(treeNode);
        return this.childList.size() - 1;
    }

    public int matchTemplateElementAll(String elementClass, String elementStr,StopWordsTable stopWordsTable,SynonymyTable synonymyTable) {

//        看停用词表中是否有elementStr
//        System.out.println("这个元素是："+"elementClass："+elementClass+"elementStr："+elementStr);
        if(stopWordsTable.stopWordList.contains(elementStr)){
//            System.out.println("匹配到了停用词："+elementStr);
            return -2;
        }

        for (int i = 0; i < this.childList.size(); i++) {
//            例子：entity，人寿保险_产品
            if (matchTemplateElement(elementClass, elementStr, i,synonymyTable)) {
                return i;
            }
        }
        return -1;
    }


//    这个树的插入的逻辑是：如果有重复元素的话，就将这个重复元素作为当前节点，然后比较当前节点的子节点中与元素的下一个节点是否有重复元素。
//    取元素的逻辑：遍历

    //    判断当前节点的类别和字符串和模板元素中的类别和字符串是否相同
    public boolean matchTemplateElement(String elementClass, String elementStr, int i,SynonymyTable synonymyTable) {
//        System.out.println("当前节点的值："+this.childList.get(i).elementClass+"和"+this.childList.get(i).elementStr);
//        System.out.println("要比较的节点的值："+elementClass+"和"+elementStr);


//        str相同或者在同一个同义词典中都返回true
        boolean strEqual=this.childList.get(i).elementStr.equals(elementStr);
        String treeNodeSetsName="";
        treeNodeSetsName=synonymyTable.synonymyDictSet.get(this.childList.get(i).elementStr);
        String queryElementSetsName=" ";
        queryElementSetsName=synonymyTable.synonymyDictSet.get(elementStr);
//        System.out.println(treeNodeSetsName+"   "+queryElementSetsName);
        boolean setsNameEqual=false;
        if(treeNodeSetsName!=null&&queryElementSetsName!=null){
            setsNameEqual=treeNodeSetsName.equals(queryElementSetsName);
        }


        boolean result=strEqual||setsNameEqual;
//        System.out.println(result);
//        System.out.println();
        return result;
    }





    public void initChildList() {
        if (childList == null)
            childList = new ArrayList<TreeNode>();
    }

    /* 返回当前节点的孩子集合 */
    public List<TreeNode> getChildList() {
        return childList;
    }

    /* 遍历一棵树，层次遍历 */
    public void traverse() {
        for (int i = 0; i < this.childList.size(); i++) {
            System.out.println(i);
            System.out.print(this.childList.get(i).elementClass + "=" + this.childList.get(i).elementStr + "    ");
            System.out.println(this.childList.get(i).intent);
        }
        System.out.println();
        for (int i = 0; i < this.childList.size(); i++) {
            this.childList.get(i).traverse();
        }
    }
}