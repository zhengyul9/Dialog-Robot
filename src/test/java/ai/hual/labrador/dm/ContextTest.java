package ai.hual.labrador.dm;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContextTest {

    private static Context context;

    @BeforeAll
    static void setUp() {
        context = new Context();
    }

    @Test
    void testInsideState() {
        CurrentState currentState = new CurrentState();
        currentState.setCurrentState("a");
        Map<String, CurrentState> thirdLayerA = new HashMap<>();
        thirdLayerA.put("a1", new CurrentState("a1", new HashMap<>()));
        thirdLayerA.put("a2", new CurrentState("a2", new HashMap<>()));
        Map<String, CurrentState> thirdLayerB = new HashMap<>();
        thirdLayerB.put("b1", new CurrentState("b1", new HashMap<>()));
        thirdLayerB.put("b2", new CurrentState("b2", new HashMap<>()));
        CurrentState secondCurrentStateA = new CurrentState("a1", thirdLayerA);
        CurrentState secondCurrentStateB = new CurrentState("b1", thirdLayerB);
        Map<String, CurrentState> secondLayer = new HashMap<>();
        secondLayer.put("a", secondCurrentStateA);
        secondLayer.put("b", secondCurrentStateB);
        currentState.setSubStates(secondLayer);
        context.setCurrentState(currentState);

        assertTrue(context.insideState("a"));
        assertTrue(context.insideState("a1"));
        assertFalse(context.insideState("a2"));
        assertFalse(context.insideState("b"));
        assertFalse(context.insideState("b1"));
        assertFalse(context.insideState("b2"));
    }
}