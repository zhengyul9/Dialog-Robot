package ai.hual.labrador.nlg;

import ai.hual.labrador.exceptions.NLGException;
import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * The NLG implementation with template rendering.
 * Created by Dai Wentao on 2017/7/5.
 */
public class NLGImpl implements NLG {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 存储模板的multimap，key为模板的Acttype和slots，值为模板内容。<br>
     * key的格式为acttype:slot_name_1,slot_name_2,...,slot_name_n。<br>
     * 如inform:dish,taste
     */
    private Multimap<String, String> templateMap = HashMultimap.create();


    public NLGImpl(TemplateModel templateModel) {
        templateModel.getTemplates().forEach(this::addTemplate);
    }

    /**
     * 向templateMap中添加一条模板
     *
     * @param t The template to be added.
     */
    private void addTemplate(Template t) {
        // 提取句中的slots，slot在模板中为{{slot_name}}
        Set<String> slotSet = new HashSet<>();
        Pattern slotPattern = Pattern.compile("\\{\\{([^}]+)}}");
        Matcher matcher = slotPattern.matcher(t.getContent());
        int start = 0;
        while (matcher.find(start)) {
            slotSet.add(matcher.group(1));
            start = matcher.end();
        }

        List<String> slots = new ArrayList<>(slotSet);
        Collections.sort(slots);

        // 添加模板
        String key = t.getLabel() + ":" + Joiner.on(",").join(slots);
        templateMap.put(key, t.getContent());
    }

    /**
     * 将slots填入模板
     *
     * @param template 模板
     * @param slots    槽
     * @return rendered result as a natural language string
     */
    private String render(String template, ListMultimap<String, Object> slots) {
        logger.debug("answer with template {}", template);
        template = template.replaceAll("//.*", "");
        template = template.replaceAll("\\\\/", "/");

        for (String key : slots.keySet()) {
            String value;
            if (slots.get(key).get(0) == null)
                value = "";
            else
                value = renderSlot(slots.get(key));
            template = template.replace("{{" + key + "}}", value);
        }
        return template;
    }

    private String renderSlot(List<Object> values) {
        if (values.size() == 1) {
            return renderObject(values.get(0));
        }
        return values.toString();
    }

    private String renderObject(Object o) {
        return o.toString();
    }

    @Override
    public String generate(ResponseAct act) {
        String label = act.getLabel();
        ListMultimap<String, Object> slots = act.getSlots();

        // make key
        List<String> slotKeys = new ArrayList<>(slots.keySet());
        Collections.sort(slotKeys);
        slotKeys.removeIf(key -> slots.get(key).get(0) == null);
        String key = label + ":" + Joiner.on(",").join(slotKeys);

        // find templates
        Collection<String> templates = templateMap.get(key);
        if (null == templates || templates.isEmpty()) {
            return noAnswerTemplate(key, slots);
        }

        // render
        List<String> results = templates.stream()
                .map(x -> render(x, slots))
                .filter(r -> !r.isEmpty())
                .collect(Collectors.toList());

        if (results.isEmpty()) {
            return noAnswerTemplate(key, slots);
        }
        return results.get(new Random().nextInt(results.size()))
                .replaceAll("\\\\n", "\n");
    }

    private String noAnswerTemplate(String key, ListMultimap<String, Object> slots) {
        String msg = String.format("No answer template for %s with value %s", key, slots.toString());
        logger.warn(msg);
        throw new NLGException(msg);
    }

}
