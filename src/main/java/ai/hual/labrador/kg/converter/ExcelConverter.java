package ai.hual.labrador.kg.converter;

import ai.hual.labrador.exceptions.KnowledgeException;
import ai.hual.labrador.kg.UpdatableKnowledgeAccessor;
import ai.hual.labrador.kg.utils.ExcelUtils;
import ai.hual.labrador.kg.utils.KnowledgeUtils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;


public class ExcelConverter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private UpdatableKnowledgeAccessor knowledgeAccessor;

    /**
     * The prefix of entity iri. The iri of an entity is prefix + label.
     */
    private String prefix;

    private List<String> triples = new ArrayList<>();

    public ExcelConverter(UpdatableKnowledgeAccessor knowledgeAccessor, String prefix) {
        this.knowledgeAccessor = knowledgeAccessor;
        this.prefix = prefix;
    }

    /**
     * Convert an excel file into knowledge entities.
     *
     * @param excel An input stream of an excel file
     */
    public ExcelConverter convert(InputStream excel) {
        try {
            // load the entity excel file
            Workbook wb = WorkbookFactory.create(excel);
            Map<String, Table<String, String, List<String>>> entities = loadEntities(wb);
            wb.close();

            // convert entities in excel into triples in model
            convert(entities);
        } catch (IOException | InvalidFormatException e) {
            logger.info("Fail executing conversion.", e);
            throw new KnowledgeException(e);
        }
        return this;
    }

    /**
     * Execute update to make converted triples take effect.
     */
    public void update() {
        StringBuilder sb = new StringBuilder("INSERT DATA {\n");
        for (String triple : triples) {
            sb.append(triple).append("\n");
        }
        sb.append("}");
        System.out.println(sb.toString());
        knowledgeAccessor.update(sb.toString());
    }

    /**
     * Convert entity model according to the schema definition.
     *
     * @param entities A map of which the key is class label and the value is a table.
     *                 Each row of the table is an entity (with its label as key) and
     *                 each column is its property (with its label as key).
     */
    private void convert(Map<String, Table<String, String, List<String>>> entities) {
        Map<String, String> propertyLabelURIMap = new HashMap<>();
        Map<String, String> classLabelURIMap = new HashMap<>();

        entities.forEach((classLabel, entityTable) ->
                entityTable.rowMap().forEach((entity, properties) ->
                        addEntity(entity, classLabel, properties, propertyLabelURIMap, classLabelURIMap)));
    }

    /**
     * Load an entity excel file.
     *
     * @param wb The excel workbook that presents the entities
     * @return A map of which the key is sheet name and the value is a table.
     * Each row of the table is an entity and each column is its property.
     * @throws IOException            when fail reading the file
     * @throws InvalidFormatException when excel file is in invalid format
     */
    private Map<String, Table<String, String, List<String>>> loadEntities(Workbook wb) throws
            IOException, InvalidFormatException {
        Map<String, Table<String, String, List<String>>> result = new HashMap<>();
        for (Sheet sheet : wb) {
            result.put(sheet.getSheetName(), loadEntitySheet(sheet));
        }
        return result;
    }

    /**
     * Load an entity sheet into a table.
     *
     * @param sheet The excel sheet
     * @return Each row of the table is an entity and each column is its property.
     */
    private Table<String, String, List<String>> loadEntitySheet(Sheet sheet) {
        Table<String, String, List<String>> sheetTable = HashBasedTable.create();

        // The row that gives property labels
        Row propertyLabelRow = sheet.getRow(0);

        // iterate over rows from index 1
        Iterator<Row> rowIterator = sheet.rowIterator();
        if (rowIterator.hasNext()) {
            rowIterator.next();
        }
        rowIterator.forEachRemaining(row -> {
            Cell entityLabelCell = row.getCell(0);
            String entityLabel = ExcelUtils.getCellString(entityLabelCell);
            if (entityLabel != null) {
                // iterate over cells from index 1
                Iterator<Cell> cellIterator = row.cellIterator();
                if (cellIterator.hasNext()) {
                    cellIterator.next();
                }
                cellIterator.forEachRemaining(cell -> {
                    String propertyLabel = ExcelUtils.getCellString(propertyLabelRow.getCell(cell.getColumnIndex()));
                    String value = ExcelUtils.getCellString(cell);
                    if (propertyLabel != null && value != null) {
                        List<String> list = sheetTable.get(entityLabel, propertyLabel);
                        if (list == null) {
                            list = new ArrayList<>();
                            sheetTable.put(entityLabel, propertyLabel, list);
                        }
                        list.add(value);
                    }
                });
            }
        });
        return sheetTable;
    }

    /**
     * Add an entity into model.
     *
     * @param entityLabel         The name of the entity.
     * @param properties          A map that contains the properties of the entity of which the key is the label of the
     * @param propertyLabelURIMap A map that maintains the map from property label to uri
     * @param classLabelURIMap    A map that maintains the map from class label to uri
     */
    private void addEntity(String entityLabel, String classLabel, Map<String, List<String>> properties,
                           Map<String, String> propertyLabelURIMap, Map<String, String> classLabelURIMap) {
        String uuid = UUID.randomUUID().toString();
        String entity = prefix + uuid;
        addTriple("<" + entity + ">", "rdfs:label", "'''" + entityLabel + "'''");
        String type = classLabelURIMap.computeIfAbsent(classLabel,
                label -> KnowledgeUtils.findClassWithLabel(knowledgeAccessor, label));
        if (type == null) {
            throw new NoSuchElementException(String.format("No class defined with label %s", classLabel));
        }
        addTriple("<" + entity + ">", "rdf:type", "<" + type + ">");
        properties.forEach((propertyLabel, value) -> addProperty(entity, propertyLabel, value, propertyLabelURIMap));
    }

    /**
     * Add a property into model
     *
     * @param entity        The entity resource.
     * @param propertyLabel The property label.
     * @param values        The property value.
     */
    private void addProperty(String entity, String propertyLabel, List<String> values,
                             Map<String, String> propertyLabelURIMap) {
        for (String value : values) {
            if (!KnowledgeUtils.isNone(value)) {
                String property = propertyLabelURIMap.computeIfAbsent(propertyLabel,
                        label -> KnowledgeUtils.findPropertyWithLabel(knowledgeAccessor, label));
                if (property == null) {
                    throw new NoSuchElementException(String.format("No property defined with label %s", propertyLabel));
                }
                addTriple("<" + entity + ">", "<" + property + ">", "'''" + value + "'''");
            }
        }
    }

    private void addTriple(String s, String p, String o) {
        triples.add(s + " " + p + " " + o + " .");
    }

}
