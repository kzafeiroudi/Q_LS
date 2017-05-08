package free917;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class Main {

    public static JSONArray to_write = new JSONArray();
    public static JSONArray to_read = new JSONArray();

    // Function handling simple case
    public static void json_simple(String utterance) {

        // Tokenize the logical form
        StringTokenizer multiTokenizer = new StringTokenizer(utterance, "!//(//) ");
        ArrayList<String> parts = new ArrayList<String>();
        int i = 0;
        while (multiTokenizer.hasMoreTokens())
        {
            parts.add(multiTokenizer.nextToken());
            i++;
        }

        // Generate the appropriate query
        String que = "PREFIX fb: <http://rdf.freebase.com/ns/> SELECT ?S FROM <http://localhost:8890/FB> WHERE {?S <" + parts.get(0) + "> <" + parts.get(1) + ">.}";

        // Create the JSON object and add it to the JSON array
        JSONObject obj = new JSONObject();
        obj.put("DCS", utterance);
        obj.put("SPARQL", que);

        to_write.add(obj);

    }

    // Function handling aggregation
    public static void json_aggregation(String utterance) {

        // Tokenize the logical form
        StringTokenizer multiTokenizer = new StringTokenizer(utterance, "!//(//) ");
        ArrayList<String> parts = new ArrayList<String>();
        int i = 0;
        while (multiTokenizer.hasMoreTokens())
        {
            parts.add(multiTokenizer.nextToken());
            i++;
        }

        // Generate the appropriate query
        String que = "PREFIX fb: <http://rdf.freebase.com/ns/> SELECT ?S (COUNT(DISTINCT ?S) AS ?COUNT FROM <http://localhost:8890/FB> WHERE {?S <" + parts.get(1) + "> <" + parts.get(2) + ">.}";

        // Create the JSON object and add it to the JSON array
        JSONObject obj = new JSONObject();
        obj.put("DCS", utterance);
        obj.put("SPARQL", que);

        to_write.add(obj);

    }

    // Function handling intersection
    public static void json_intersection(String utterance) {

        // Tokenize the logical form
        StringTokenizer multiTokenizer = new StringTokenizer(utterance, "!//(//) ");
        ArrayList<String> parts = new ArrayList<String>();
        int i = 0;
        while (multiTokenizer.hasMoreTokens())
        {
            parts.add(multiTokenizer.nextToken());
            i++;
        }

        // Generate the appropriate query
        String que = "PREFIX fb: <http://rdf.freebase.com/ns/> SELECT ?S FROM <http://localhost:8890/FB> WHERE {?S ?P <" + parts.get(1) + ">. ?S ?P ?O. ?O <" + parts.get(2) + "> <" + parts.get(3) + ">.}";

        // Create the JSON object and add it to the JSON array
        JSONObject obj = new JSONObject();
        obj.put("DCS", utterance);
        obj.put("SPARQL", que);

        to_write.add(obj);
    }

    // Function handling lambda
    public static void json_lambda(String utterance) {

        // Tokenize the logical form
        StringTokenizer multiTokenizer = new StringTokenizer(utterance, "!//(//) ");
        ArrayList<String> parts = new ArrayList<String>();
        int i = 0;
        while (multiTokenizer.hasMoreTokens())
        {
            parts.add(multiTokenizer.nextToken());
            i++;
        }

        // Generate the appropriate query
        String que = "PREFIX fb: <http://rdf.freebase.com/ns/> SELECT ?S FROM <http://localhost:8890/FB> WHERE {?S <" + parts.get(0) + "> ?O . ?O <" + parts.get(3) + "> <" + parts.get(6) + ">.}";

        // Create the JSON object and add it to the JSON array
        JSONObject obj = new JSONObject();
        obj.put("DCS", utterance);
        obj.put("SPARQL", que);

        to_write.add(obj);
    }

    public static void main() throws Exception {

        // The input file "free917.train.json" should be under the same folder as Q_LS class
        URL path = Main.class.getResource("free917.train.json");
        File selectedFile = new File(path.getFile());
        System.out.println("Input file: " + selectedFile.getAbsolutePath());

        // Parse the input file
        JSONParser parser = new JSONParser();
        JSONArray to_read = (JSONArray) parser.parse(new FileReader(selectedFile));

        // Initialize counters for each type of logical form
        int simple = 0;
        int aggregation = 0;
        int lambda = 0;
        int intersection = 0;
        int combination = 0;
        String target = "";

        for (Object o : to_read) {
            JSONObject item = (JSONObject) o;

            // Natural language question
            String strUtterance = (String) item.get("utterance");

            // Logical form query
            String strTarget = (String) item.get("targetFormula");

            int a = StringUtils.countMatches(strTarget, "(");

            // Recognizing logical forms, updating counters and calling the corresponding functions
            if (a == 1) {
                simple++;
                json_simple(strTarget);
            }
            else {
                int count = StringUtils.countMatches(strTarget, "(count ");
                int and = StringUtils.countMatches(strTarget, "(and ");
                int lam = StringUtils.countMatches(strTarget, "(lambda ");
                if (count == 1 && and == 0 && lam == 0) {
                    aggregation++;
                    json_aggregation(strTarget);
                }
                else if (count == 0 && and == 1 && lam == 0) {
                    intersection++;
                    json_intersection(strTarget);
                }
                else if (count == 0 && and == 0 && lam == 1) {
                    lambda++;
                    json_lambda(strTarget);
                }
                else {
                    combination++;
                }
            }
        }

        // Print the count of each different type
        System.out.println("SIMLE LOGICAL FORMS: " + simple);
        System.out.println("AGGREGATION: " + aggregation);
        System.out.println("INTERSECTION: " + intersection);
        System.out.println("LAMBDA: " + lambda);
        System.out.println("COMBINATION OF THE ABOVE: " + combination);

        // The output file "free917.output.json" will be under the same folder as Q_LS class
        URL path2 = Main.class.getResource("free917.output.json");
        File selectedFile2 = new File(path2.getFile());
        System.out.println("Output file: " + selectedFile2.getAbsolutePath());

        // Write JSON array to file
        try (FileWriter file = new FileWriter(selectedFile2)) {

            file.write(to_write.toJSONString());
            file.flush();

        } catch (IOException e) {

            e.printStackTrace();
        }

        System.exit(0);

    }
}
