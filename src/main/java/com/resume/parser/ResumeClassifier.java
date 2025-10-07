package com.resume.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class ResumeClassifier {

    private static final String INPUT_FOLDER = "C:\\Resumes";
    private static final String OUTPUT_CSV = "C:\\Resumes\\output\\ResumeData.csv";

    public static void main(String[] args) {
        try {
            File folder = new File(INPUT_FOLDER);
            File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

            if (files == null || files.length == 0) {
                System.out.println("No PDF files found in " + INPUT_FOLDER);
                return;
            }

            Files.createDirectories(Paths.get("C:\\Resumes\\output"));

            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_CSV));
                 CSVPrinter csvPrinter = new CSVPrinter(writer,
                         CSVFormat.DEFAULT.withHeader("FileName", "Name", "College", "Role", "SkillCategory"))) {

                for (File file : files) {
                    String text = extractTextFromPDF(file);
                    Map<String, String> extracted = extractDetails(file.getName(), text);
                    csvPrinter.printRecord(
                            extracted.get("FileName"),
                            extracted.get("Name"),
                            extracted.get("College"),
                            extracted.get("Role"),
                            extracted.get("SkillCategory")
                    );
                    csvPrinter.flush();
                    System.out.println("Processed: " + file.getName());
                }
            }

            System.out.println("\n✅ CSV generated at: " + OUTPUT_CSV);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractTextFromPDF(File file) {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            System.err.println("Error reading PDF: " + file.getName());
            return "";
        }
    }

    private static Map<String, String> extractDetails(String fileName, String text) {
        Map<String, String> details = new HashMap<>();
        details.put("FileName", fileName);

        details.put("Name", extractName(text));
        details.put("College", extractCollege(text));
        details.put("Role", extractRole(text));
        details.put("SkillCategory", classifySkills(text));

        return details;
    }

    private static String extractName(String text) {
        Pattern p = Pattern.compile("(?i)(name[:\\s]+)([A-Z][a-zA-Z ]+)");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(2).trim();

        String firstLine = text.split("\\n")[0].trim();
        if (firstLine.split(" ").length <= 4)
            return firstLine;

        return "Unknown";
    }

    private static String extractCollege(String text) {
        Pattern p = Pattern.compile("(?i)(college|university|institute)[:\\s-]+([A-Za-z0-9 &.,-]+)");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(2).trim();
        return "Unknown";
    }

    private static String extractRole(String text) {
        Pattern p = Pattern.compile("(?i)(role|designation|position)[:\\s-]+([A-Za-z ]+)");
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(2).trim();
        return "Unknown";
    }

    private static String classifySkills(String text) {
        String lower = text.toLowerCase();
        Set<String> words = new HashSet<>(Arrays.asList(lower.split("\\W+")));

        if (containsAny(words, "qa", "selenium", "test", "automation")) {
            return "QA";
        }

        if (containsAll(words, "python", "numpy", "pytest", "dataframe", "matlabplot", "pandas", "fast", "flask")
                && !containsAny(words, "javascript", "java", "react")) {
            return "Python Backend";
        }

        if (containsAll(words, "python", "numpy", "pytest", "dataframe", "matlabplot", "pandas", "react", "javascript", "typescript", "fast", "flask")
                && !containsAny(words, "java")) {
            return "Python Full Stack";
        }

        if (containsAny(words, "react", "java", "javascript") && !containsAny(words, "python")) {
            return "Java Full Stack";
        }

        if (containsAll(words, "java", "spring", "junit")
                && !containsAny(words, "net", "c", "react", "python")) {
            return "Java Backend";
        }

        if (containsAny(words, "c", "net", "csharp", "c#", "c-sharp")
                && !containsAny(words, "java", "python", "react")) {
            return ".Net Backend";
        }

        if (containsAny(words, "c", "net", "react", "javascript", "csharp", "c#")
                && !containsAny(words, "java", "python")) {
            return ".Net Full Stack";
        }

        return "Unclassified";
    }

    private static boolean containsAny(Set<String> words, String... keywords) {
        for (String keyword : keywords) {
            if (words.contains(keyword.toLowerCase())) return true;
        }
        return false;
    }

    private static boolean containsAll(Set<String> words, String... keywords) {
        for (String keyword : keywords) {
            if (!words.contains(keyword.toLowerCase())) return false;
        }
        return true;
    }
}
