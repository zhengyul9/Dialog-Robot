package ai.hual.labrador.nlg;

import ai.hual.labrador.exceptions.NLGException;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test {@link NLGImpl}
 * Created by Dai Wentao on 2017/7/5.
 */
public class NLGImplTest {

    @Test
    public void generatesResponseAct() {
        NLGImpl nlg = new NLGImpl(new TemplateModel(Arrays.asList(
                new Template("abc", "abcxyz"),
                new Template("d", "efghi"),
                new Template("d", "ef{{gh}}i"),
                new Template("d", "e{{f}}{{gh}}i"),
                new Template("d", "e{{f}}ghi"),
                new Template("e", "{{asdf}}")
        )));

        ResponseAct act = new ResponseAct("abc");
        assertEquals("abcxyz", nlg.generate(act));

        act.setLabel("d");
        assertEquals("efghi", nlg.generate(act));

        act.getSlots().put("gh", "xx");
        assertEquals("efxxi", nlg.generate(act));

        act.getSlots().put("f", "yy");
        assertEquals("eyyxxi", nlg.generate(act));

        act.getSlots().removeAll("gh");
        assertEquals("eyyghi", nlg.generate(act));

        act.getSlots().put("asdf", "");

        try {
            nlg.generate(act);
            fail();
        } catch (NLGException e) {
            assertThat(e.getMessage(), startsWith("No answer template"));
        }

        act.setLabel("e");
        act.getSlots().removeAll("f");
        try {
            nlg.generate(act);
            fail();
        } catch (NLGException e) {
            assertThat(e.getMessage(), startsWith("No answer template"));
        }
    }

    @Test
    public void generatesResponseActWithNullSlot() {
        NLGImpl nlg = new NLGImpl(new TemplateModel(Arrays.asList(
                new Template("d", "efghi"),
                new Template("d", "ef{{gh}}i")
        )));

        ResponseAct act = new ResponseAct("d");
        act.put("gh", null);
        assertEquals("efghi", nlg.generate(act));
    }
}
