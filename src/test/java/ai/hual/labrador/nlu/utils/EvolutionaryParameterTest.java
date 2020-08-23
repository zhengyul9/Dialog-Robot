package ai.hual.labrador.nlu.utils;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EvolutionaryParameterTest {
    public static final int paramsNum = 4;
    private static double rawX[][];
    private static int correctResult[][];
    private static boolean withRefused;
    private static int row;
    XSSFWorkbook wb;

    @Test
    void setWithoutRefused() {
        try {
            InputStream InputStream = getClass().getClassLoader().getResourceAsStream("nlu/EvolutionaryTest.xlsx");
            wb = new XSSFWorkbook(InputStream);
            InputStream.close();
            XSSFSheet sheet = wb.getSheetAt(0);
            row = sheet.getLastRowNum();
            rawX = new double[row][paramsNum];
//            alphaX = new double[row][paramsNum];
            correctResult = new int[row][paramsNum];
            for (int j = 1; j <= row; j++) {
                //check whether all data read well
//                System.out.println(j);
                for (int i = 0; i < paramsNum * 2; i++) {
                    XSSFRow row = sheet.getRow(j);
                    if (i < paramsNum) {
                        if (row.getCell(i) == null || row.getCell(i).toString() == "") {
                            rawX[j - 1][i] = 0;
//                            alphaX[j-1][i] = 0;
                        } else {
                            rawX[j - 1][i] = Double.parseDouble(row.getCell(i).toString());
                        }
                    } else {
                        if (row.getCell(i) == null)
                            correctResult[j - 1][i - paramsNum] = 0;
                        else
                            correctResult[j - 1][i - paramsNum] = (int) Double.parseDouble(row.getCell(i).toString());
                    }
                }

            }
        } catch (IOException | NullPointerException e) {
//            throw new NLUException("Error, fail to read xlsx data file");
        }
        EvolutionaryParameter ep = new EvolutionaryParameter();
        OptimizedParameters result = ep.computeParam(rawX, correctResult);
        double exponent[] = result.getAlpha();
        double refuseScore[] = result.getRefuseScore();
        double percentage = result.getPercentage();
        assertEquals(4, exponent.length);
        assertEquals(4, refuseScore.length);
        assert (0.0 < percentage && percentage < 1.0);
        for (double e : exponent) {
            assert (0.0 < e && e < 1.0);
        }
        for (double r : refuseScore) {
            assert (0.0 < r && r < 1.0);
        }
    }

    @Test
    void setWithRefused() {
        try {
            InputStream InputStream = getClass().getClassLoader().getResourceAsStream("nlu/EvolutionaryTest.xlsx");
            wb = new XSSFWorkbook(InputStream);
            InputStream.close();
            XSSFSheet sheet = wb.getSheetAt(0);
            row = sheet.getLastRowNum();
            rawX = new double[row][paramsNum];
//            alphaX = new double[row][paramsNum];
            correctResult = new int[row][paramsNum];
            for (int j = 1; j <= row; j++) {
                //check whether all data read well
//                System.out.println(j);
                for (int i = 0; i < paramsNum * 2; i++) {
                    XSSFRow row = sheet.getRow(j);
                    if (i < paramsNum) {
                        if (row.getCell(i) == null || row.getCell(i).toString() == "") {
                            rawX[j - 1][i] = 0;
//                            alphaX[j-1][i] = 0;
                        } else {
                            rawX[j - 1][i] = Double.parseDouble(row.getCell(i).toString());
                        }
                    } else {
                        if (row.getCell(i) == null)
                            correctResult[j - 1][i - paramsNum] = 0;
                        else
                            correctResult[j - 1][i - paramsNum] = (int) Double.parseDouble(row.getCell(i).toString());
                    }
                }

            }
        } catch (IOException | NullPointerException e) {
//            throw new NLUException("Error, fail to read xlsx data file");
        }
        EvolutionaryParameter ep = new EvolutionaryParameter();
        OptimizedParameters result = ep.computeParam(rawX, correctResult, true);
        double exponent[] = result.getAlpha();
        double refuseScore[] = result.getRefuseScore();
        double percentage = result.getPercentage();
        assertEquals(4, exponent.length);
        assertEquals(4, refuseScore.length);
        assert (0.0 < percentage && percentage < 1.0);
        for (double e : exponent) {
            assert (0.0 < e && e < 1.0);
        }
        for (double r : refuseScore) {
            assert (0.0 < r && r < 1.0);
        }
    }
}