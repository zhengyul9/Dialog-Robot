package ai.hual.labrador.nlu.ners.engines;

import ai.hual.labrador.exceptions.NLUException;
import ai.hual.labrador.nlu.Config;
import ai.hual.labrador.nlu.ners.NERResult;
import ai.hual.labrador.nlu.ners.subners.LocationNER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocationNERbyRule {
    private static final String BELONG_TO_NER_NAME = "LocationNER";
    private static final String RESOURCES_DIR = "Resources_LocationNERbyRule";
    //private static final String USED_REGEX_FILE = RESOURCES_DIR + "/" + "location_regex.txt";
    private static final String USED_TOTAL_Location_FILE = RESOURCES_DIR + "/" + "location_training_project_only.txt";
    private static final String USED_BLOCK_FILE = RESOURCES_DIR + "/" + "block.txt";
    private static final Logger logger = LoggerFactory.getLogger(LocationNER.class);
    List<String> allLocation = this.getWordList(USED_TOTAL_Location_FILE);
    List<String> block = this.getWordList(USED_BLOCK_FILE);
    //List<String> regex = this.getRegexList(USED_REGEX_FILE);
    String location = "(" + String.join("|", allLocation) + ")";

    //String patterns = "(" + String.join("|", regex) + ")";

    // regex starts here
    //String allPattern = location + "|" + patterns;
    String allPattern = location;
    Pattern p = Pattern.compile(allPattern);//地点词表

    // regex ends here

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
                if (line.charAt(0) == '#') // ignore comments
                    return;
                result.add(line);
            });
            br.close();
            logger.debug("getWordList finished in LocationNER: " + result.toString());
            return result;
        } catch (IOException ex) {
            logger.error("getWordList error in LocationNER.");
            throw new NLUException("Could not find file " + file);
        }
    }

    // getRegexList has not been used yet
    public List<String> getRegexList(String file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    Config.getLoader().getResourceAsStream(file), StandardCharsets.UTF_8));
            List<String> result = new ArrayList<>();
            br.lines().forEach(line -> {
                line = line.trim();
                if (line.length() == 0)
                    return;
                if (line.charAt(0) == '#')
                    return;
                result.add(line);
            });
            br.close();
            return result;
        } catch (IOException ex) {
            logger.error("getRegexList error in LocationNER.");
            throw new NLUException("Could not find file " + file);
        }
    }

    // recognize all location from text
    public List<NERResult.Candidate> locationRecognize(String text) {
        Matcher m = p.matcher(text);

        List<NERResult.Candidate> candidates = new ArrayList<>();
        while (m.find()) {  // get all spans of matches str
            candidates.add(new NERResult.Candidate(m.start(), m.end(), "地点", "LocationNER", text.substring(m.start(), m.end()), "", ""));
        }

        //合并连续槽位
        if(candidates.size() > 1) { //如果提取出两个以上槽位
            int flag = 0;
            List<NERResult.Candidate> new_candidates = new ArrayList<>();//新建temp槽位
            NERResult.Candidate temp_candidate = candidates.get(0);//temp初值是第一个槽值
            for (int i = 1; i < candidates.size(); i++) { //从第二个槽开始到最后一个
                //判断是否是连续地点槽
                if(candidates.get(i).getRealStart() == temp_candidate.getRealEnd())
                    flag = 1;
                else{
                    String temp = text.substring(temp_candidate.getRealEnd(),candidates.get(i).getRealStart());

                    for(int j=0; j<block.size(); j++){
                        if(text.substring(temp_candidate.getRealEnd(),candidates.get(i).getRealStart()).contains(block.get(j))){
                            temp = temp.replace(block.get(j),"*");
                        }
                    }
                    for(int j=0; j<temp.length(); j++){
                        if(!temp.substring(j,j+1).equals("*")){
                            flag = 0;
                            break;
                        }
                        else
                            flag = 1;
                    }
                }

                if(flag == 1){ //如果槽i与temp相邻
                    //合并text
                    String combine = temp_candidate.getText()+candidates.get(i).getText();
                    combine = text.substring(temp_candidate.getRealStart(),candidates.get(i).getRealEnd());
                    temp_candidate.setText(combine);
                    //合并index
                    int new_end = candidates.get(i).getRealEnd();
                    temp_candidate.setRealEnd(new_end);
                    //保持新temp不变，查看下一个槽，直到不相邻。
                }
                else{
                    //如果或直到不相邻，把temp加入新的槽位list
                    new_candidates.add(temp_candidate);
                    //temp重置为当前槽位
                    temp_candidate = candidates.get(i);
                }
            }
            //加入最后一个槽
            new_candidates.add(temp_candidate);
            candidates = new_candidates;
        }
        return candidates;
    }
}
