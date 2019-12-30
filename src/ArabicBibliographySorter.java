import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArabicBibliographySorter {

  public static void main(String[] args) {
    if (args == null || args.length < 1) {
      System.err.println(
          "Error: no inputFile\nUsage: java ArabicBibliographySorter inputFile");
      return;
    }

    File inputFile;
    Scanner scanner;
    try {
      inputFile = new File(args[0]);
      scanner = new Scanner(inputFile);
    } catch (Exception e) {
      System.err.println("Error: reading inputFile:\t" + args[0]);
      return;
    }

    ArrayList<Reference> references = new ArrayList<>();
    while (scanner.hasNext()) {
      String line = scanner.nextLine();
      references.add(new Reference(line.trim(), normalizeCitation(line.trim())));
    }

    references.sort(Comparator.comparing(o -> o.normalizedCitation));

    saveToFile(references, "sorted-" + args[0]);
  }

  public static void saveToFile(ArrayList<Reference> references, String outputFileName) {
    PrintWriter pw;
    try {
      pw = new PrintWriter(new FileOutputStream(outputFileName));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      System.err.println("Error: could not save outputFile:\t" + outputFileName);
      return;
    }
    for (Reference ref : references) {
      pw.println(ref.citation);
    }
    pw.close();
  }

  private static String normalizeCitation(String citation) {
    if (citation.startsWith("http")) {
      return citation;
    }
    citation = removeArabicDiacritics(citation);
    citation = removePunctuation(citation);
    citation = normalizeQutationMarks(citation);
    citation = citation.replaceAll("[ٱآإأ]", "ا");
    citation = citation.replaceAll("اب ", "");
    citation = citation.replaceAll("ام ", "");
    citation = citation.replaceAll("ابن ", "");
    citation = citation.replaceAll("بن ", "");
    citation = citation.replaceAll("بنت ", "");
    citation = citation.replaceAll("ابنة ", "");
    citation = citation.replaceAll("ابو ", "");
    citation = citation.replaceAll("ابي ", "");
    citation = removeAl(citation);
    return citation;
  }

  public static String normalizeQutationMarks(String input) {
    input = input.replaceAll("[`’‘]", "'");
    input = input.replaceAll("[”]", "\"");
    return input;
  }

  public static String removeArabicDiacritics(String input) {
    return input.replaceAll("[\\u064E\\u064F\\u0650\\u0652\\u064B\\u064C\\u064D\\u0651]*", "");
  }

  public static String removePunctuation(String input) {
//        normalize uncommon hyphen
    input = input.replaceAll("–", "-");
    input = input.replaceAll("(\\p{P})", " ").replaceAll("\\s+", " ");
    return input;
  }

  public static String removeAl(String input) {
    String patternStr = "^(ال)([ءآأؤإئابةتثجحخدذرزسشصضطظعغـفقكلمنهوىي]{2,}\\s*)";
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher;
    String[] words = input.split("\\s+");
    for (int i = 0; i < words.length; i++) {
      matcher = pattern.matcher(words[i]);
      if (matcher.find()) {
        words[i] = matcher.replaceFirst(matcher.group(2));
      }
    }
    return String.join(" ", words);
  }
}

class Reference {

  String citation;
  String normalizedCitation;

  public Reference(String citation, String normalizedCitation) {
    this.citation = citation;
    this.normalizedCitation = normalizedCitation;
  }
}
