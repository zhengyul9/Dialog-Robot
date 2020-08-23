package ai.hual.labrador.kg.utils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;

public class ExcelUtils {

    private static DataFormatter df = new DataFormatter();

    /**
     * Get a cell's value as string
     *
     * @param cell A cell that may contain some value
     * @return String if the cell is not blank, or null if the cell is blank
     */
    public static String getCellString(final Cell cell) {
        if (cell == null || cell.getCellTypeEnum() == CellType.BLANK) {
            return null;
        }

        String content = df.formatCellValue(cell).trim();
        return content.isEmpty() ? null : content;
    }

}
