package com.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import com.opencsv.*;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
public class ExcelWriter {

    static void writeToExcel(ResultSet rs, String fileName) throws IOException {
        try {
            //reading all retrieved columns from result set
            ResultSetMetaData rsmd = rs.getMetaData();
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                rsmd.getColumnLabel(i);
            }
            String[] stringArray = columns.toArray(new String[0]);

            //writing data from result set to csv
            CSVWriter writer = new CSVWriter(new FileWriter(fileName));
            writer.writeNext(stringArray);
            writer.writeAll(rs, true);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
