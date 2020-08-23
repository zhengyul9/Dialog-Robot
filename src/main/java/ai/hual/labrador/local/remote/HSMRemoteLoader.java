package ai.hual.labrador.local.remote;

import ai.hual.labrador.constants.Channel;
import ai.hual.labrador.dialog.AccessorRepositoryImpl;
import ai.hual.labrador.dialog.DialogSystem;
import ai.hual.labrador.dialog.accessors.DatabaseAccessor;
import ai.hual.labrador.dialog.accessors.DictAccessor;
import ai.hual.labrador.dialog.accessors.PropertyFrequencyAccessor;
import ai.hual.labrador.dialog.accessors.RelatedQuestionAccessor;
import ai.hual.labrador.dm.DM;
import ai.hual.labrador.dm.java.DialogConfig;
import ai.hual.labrador.faq.ChanneledFAQAccessor;
import ai.hual.labrador.faq.FAQAccessor;
import ai.hual.labrador.kg.KnowledgeAccessor;
import ai.hual.labrador.kg.KnowledgeStatusAccessor;
import ai.hual.labrador.local.DialogSystemLoader;
import ai.hual.labrador.nlg.NLG;
import ai.hual.labrador.nlu.NLU;
import ai.hual.labrador.nlu.matchers.IntentClassifierAccessor;

import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * A dialog system loader that use remote everything except for dm class loader
 */
public class HSMRemoteLoader implements DialogSystemLoader {

    private DialogSystem ds;

    /**
     * initialize an HSM dialog system loader
     *
     * @param classLoader a class loader from where extra dm classes are loaded
     * @param properties  labrador.local.remoteLoader.api     - the api url of labrador manage api.
     *                    labrador.local.remoteLoader.bot     - bot name.
     *                    labrador.local.remoteLoader.channel - channel name
     *                    labrador.knowledge.graph-prefix     - A prefix defined for graph in labrador.
     *                    labrador.knowledge.sparql.base-url  - The base URL for SPARQL endpoints, which will be
     *                    placed before sparql.query, sparql.update and sparql.graph-crud.
     *                    labrador.knowledge.sparql.query     - The endpoint for SPARQL query.
     *                    labrador.database.driver            - database driver
     *                    labrador.database.url               - database url
     *                    labrador.database.username          - database username
     *                    labrador.database.password          - database password
     *                    faq.matching.url                    - url of faq matching service
     *                    faq.matching.bot                    - bot of faq matching service
     *                    intent.classify.url                 - url of intent classification service
     *                    intent.classify.bot                 - bot of intent classification service
     */
    public HSMRemoteLoader(ClassLoader classLoader, Properties properties) {
        String manageAPIAddress = Objects.requireNonNull(properties.getProperty("labrador.local.remoteLoader.api"));
        String botName = Objects.requireNonNull(properties.getProperty("labrador.local.remoteLoader.bot"));
        String username = Objects.requireNonNull(properties.getProperty("labrador.local.remoteLoader.username"));
        String password = Objects.requireNonNull(properties.getProperty("labrador.local.remoteLoader.password"));
        properties.put(DialogConfig.BOT_CONFIGURATION_BOT_NAME_KEY, botName);

        // accessors
        String graphPrefix = properties.getProperty("labrador.knowledge.graph-prefix");
        String queryService = properties.getProperty("labrador.knowledge.sparql.base-url") +
                properties.getProperty("labrador.knowledge.sparql.query");
        KnowledgeContext knowledgeContext = new KnowledgeContext(
                graphPrefix, queryService, null, null,
                new KnowledgeAuth(null, -1, null, null, null, null));
        KnowledgeAccessor kgAccessor = new RemoteKnowledgeAccessor(
                new VirtuosoHTTPKnowledgeGraphDAO(knowledgeContext), botName);

        // client to labrador_manage_api
        ManageClient client = new ManageClient(manageAPIAddress, botName, username, password);

        String channel = Optional.ofNullable(properties.getProperty("labrador.local.remoteLoader.channel")).orElse(Channel.DEFAULT_CHANNEL);
        FAQAccessor faqAccessor = new ChanneledFAQAccessor(new RemoteFAQAccessor(properties), channel);
        FAQAccessor chatAccessor = new RemoteChatAccessor(properties);
        IntentClassifierAccessor intentClassifierAccessor = new RemoteIntentClassifierAccessor(properties);
        RelatedQuestionAccessor relatedQuestionAccessor = new ManageRelatedQuestionAccessor(client);
        KnowledgeStatusAccessor knowledgeStatusAccessor = new ManageKnowledgeStatusAccessor(client);
        DictAccessor dictAccessor = new ManageDictAccessor(client);
        PropertyFrequencyAccessor propertyFrequencyAccessor = new ManagePropertyFrequencyAccessor(client);

        String databaseDriver = properties.getProperty("labrador.database.driver");
        String databaseUrl = properties.getProperty("labrador.database.url");
        String databaseUsername = properties.getProperty("labrador.database.username");
        String databasePassword = properties.getProperty("labrador.database.password");
        DatabaseAccessor databaseAccessor = new RemoteDatabaseAccessor(
                databaseDriver, databaseUrl, databaseUsername, databasePassword);


        AccessorRepositoryImpl accessorRepository = new AccessorRepositoryImpl()
                .withFaqAccessor(faqAccessor)
                .withChatAccessor(chatAccessor)
                .withKnowledgeAccessor(kgAccessor)
                .withIntentAccessor(intentClassifierAccessor)
                .withRelatedQuestionAccessor(relatedQuestionAccessor)
                .withKnowledgeStatusAccessor(knowledgeStatusAccessor)
                .withDictAccessor(dictAccessor)
                .withDatabaseAccessor(databaseAccessor)
                .withPropertyFrequencyAccessor(propertyFrequencyAccessor);

        // dialog system
        NLU nlu = new ManageNLUClient(client);
        DM dm = new ManageDMClient(classLoader, client, accessorRepository, properties);
        NLG nlg = new ManageNLGClient(client);

        this.ds = new DialogSystem(nlu, dm, nlg, accessorRepository.withNLU(nlu).withNLG(nlg));
    }

    /**
     * Try loading resources. If any of the resources has been updated since the last time this method been called,
     * the resources that's been updated will reload and the dialog system will be remade.
     *
     * @return A DialogSystem object that's made from resources or cached in memory.
     * @throws Exception when unable to read resource
     */
    @Override
    public DialogSystem load() {
        return ds;
    }

}
