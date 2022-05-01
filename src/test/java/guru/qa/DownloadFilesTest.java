package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.pdftest.matchers.ContainsExactText;
import com.codeborne.selenide.Selenide;
import com.codeborne.xlstest.XLS;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;


public class DownloadFilesTest {

    ClassLoader cl = DownloadFilesTest.class.getClassLoader();

    @Test
    void zipParsingTest() throws Exception {

        ZipFile zf = new ZipFile(new File("src/test/resources/filesfortest/files.zip"));
        ZipInputStream is = new ZipInputStream(Objects.requireNonNull(cl.getResourceAsStream("filesfortest/files.zip")));
        ZipEntry entry;
        while((entry = is.getNextEntry()) !=null) {
            if(entry.getName().contains("csvExample.csv")) {
                try(InputStream inputStream = zf.getInputStream(entry)) {
                    try (
                            CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                        List<String[]> content = reader.readAll();
                        assertThat(content).contains(
                                new String[]{"some line", "another line"},
                                new String[]{"some second line", "another second line"}
                        );
                    }

                }

            }
            if(entry.getName().contains("junit-user-guide-5.8.2.pdf")) {
                try(InputStream inputStream = zf.getInputStream(entry)) {
                    PDF pdf = new PDF(inputStream);
                    Assertions.assertEquals(166, pdf.numberOfPages);
                    assertThat(pdf, new ContainsExactText("123"));
                }
            }
            if(entry.getName().contains("example.xlsx")) {
                try(InputStream inputStream = zf.getInputStream(entry)) {
                    XLS xls = new XLS(inputStream);
                    String stringCellValue = xls.excel.getSheetAt(1).getRow(3).getCell(1).getStringCellValue();
                    assertThat(stringCellValue).contains("surname 4");
                }
            }

        }

    }

    @Test
    void downloadTest() throws Exception {
        Selenide.open("https://github.com/junit-team/junit5/blob/main/README.md");
        File textFile = $("#raw-url").download();
        try (InputStream is = new FileInputStream(textFile)) {
            byte[] fileContent = is.readAllBytes();
            String strContent = new String(fileContent, StandardCharsets.UTF_8);
            assertThat(strContent).contains("JUnit 5");
        }
    }

    @Test
    void pdfParsingTest() throws Exception {
        try (InputStream stream =cl.getResourceAsStream("filesfortest/junit-user-guide-5.8.2.pdf")) {
            assert stream != null;
            PDF pdf = new PDF(stream);
            Assertions.assertEquals(166, pdf.numberOfPages);
            assertThat(pdf, new ContainsExactText("123"));
        }
    }

    @Test
    void xlsParsingTest() throws Exception {
        try (InputStream stream = cl.getResourceAsStream("filesfortest/example.xlsx")) {
            assert stream != null;
            XLS xls = new XLS(stream);
            String stringCellValue = xls.excel.getSheetAt(1).getRow(3).getCell(1).getStringCellValue();
            assertThat(stringCellValue).contains("surname 4");

        }
    }

    @Test
    void csvParsingTest() throws Exception{
        try (InputStream stream = cl.getResourceAsStream("filesfortest/csvExample.csv")) {
            assert stream != null;
            try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {

                List<String[]> content = reader.readAll();
                assertThat(content).contains(
                        new String[]{"some line", "another line"},
                        new String[]{"some second line", "another second line"}
                );
            }
        }
    }

    @Test
    void jsonTest() throws Exception {
        Gson gson = new Gson();
        try (InputStream stream = cl.getResourceAsStream("filesfortest/sample.json")) {
            assert stream != null;
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
            assertThat(jsonObject.get("fruit").getAsString()).isEqualTo("Apple");
            assertThat(jsonObject.get("properties")
                    .getAsJsonObject()
                    .get("size").getAsString())
                    .isEqualTo("Large");

        }
    }
}



