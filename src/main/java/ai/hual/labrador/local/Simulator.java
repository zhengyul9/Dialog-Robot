package ai.hual.labrador.local;

import ai.hual.labrador.dialog.DialogResult;
import ai.hual.labrador.dialog.DialogSystem;
import ai.hual.labrador.local.remote.HSMRemoteLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

/**
 * A local simulator
 * Created by Dai Wentao on 2017/7/3.
 */
public class Simulator {

    public static final String COMMAND_RESET = "reset";
    public static final String COMMAND_EXIT = "exit";

    public static final String PARAMS_SPLITTER = "@@";

    public static final String HSM_DEFAULT_CONFIG_FILE_NAME = "labrador.properties";

    private DialogSystemLoader loader;
    private String state = "{}";

    public static Simulator hsmRemoteDevelop(String[] args, ClassLoader classLoader) throws IOException {
        String propFile = args.length == 0 ? HSM_DEFAULT_CONFIG_FILE_NAME : args[0];
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(propFile)) {
            properties.load(fis);
        }
        return new Simulator(new HSMRemoteLoader(classLoader, properties));
    }

    public Simulator(DialogSystemLoader loader) {
        this.loader = loader;
    }

    /**
     * API for starting simulator in command line.
     */
    public void start() {
        System.out.print("Q: ");
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name());
        while (scanner.hasNextLine()) {
            String query = scanner.nextLine();
            if (query.equals(COMMAND_RESET)) {
                state = "{}";
                System.out.println("Reset state.");
                System.out.print("Q: ");
                continue;
            }
            if (query.equals(COMMAND_EXIT)) {
                System.out.println("Exit.");
                break;
            }
            List<String> queryAndParams = Splitter.on(PARAMS_SPLITTER).splitToList(query);
            query = queryAndParams.get(0);
            String params = queryAndParams.size() > 1 ? queryAndParams.get(1) : null;

            try {
                // dialog
                DialogSystem dialogSystem = loader.load();
                DialogResult result = dialogSystem.process("local", query, params, state);
                dialogSystem.close();

                // process answer
                state = result.getState();
                System.out.print("A: ");
                System.out.println(result.getAnswer());
                if (!result.getInstructions().isEmpty()) {
                    System.out.println(String.format("Instructions: %s",
                            new ObjectMapper().writeValueAsString(result.getInstructions())));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("An error has occurred in last input. Fix it and try again.");
            }
            System.out.print("Q: ");
        }
    }

}
