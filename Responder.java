import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

/**
 * The responder class represents a response generator object.
 * It is used to generate an automatic response, based on specified input.
 * Input is presented to the responder as a set of words, and based on those
 * words the responder will generate a String that represents the response.
 *
 * Internally, the reponder uses a HashMap to associate words with response
 * strings and a list of default responses. If any of the input words is found
 * in the HashMap, the corresponding response is returned. If none of the input
 * words is recognized, one of the default responses is randomly chosen.
 * 
 * @author David J. Barnes and Michael Kölling.
 * @version 2016.02.29
 */
public class Responder
{
    // Used to map key words to responses.
    private HashMap<String, String> responseMap;
    // Default responses to use if we don't recognise a word.
    private ArrayList<String> defaultResponses;
    // The name of the file containing the default responses.
    private static final String FILE_OF_DEFAULT_RESPONSES = "default.txt";
    private Random randomGenerator;

    /**
     * Construct a Responder
     */
    public Responder()
    {
        responseMap = new HashMap<>();
        defaultResponses = new ArrayList<>();
        fillResponseMap();
        fillDefaultResponses();
        randomGenerator = new Random();
    }

    /**
     * Generate a response from a given set of input words.
     * 
     * @param words  A set of words entered by the user
     * @return       A string that should be displayed as the response
     */
    public String generateResponse(HashSet<String> words)
    {
        Iterator<String> it = words.iterator();
        while(it.hasNext()) {
            String word = it.next();
            String response = responseMap.get(word);
            if(response != null) {
                return response;
            }
        }
        // If we get here, none of the words from the input line was recognized.
        // In this case we pick one of our default responses (what we say when
        // we cannot think of anything else to say...)
        return pickDefaultResponse();
    }

    /**
     * Enter all the known keywords and their associated responses
     * into our response map.
     * reads text from responses file
     */
    private void fillResponseMap()
    {
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get("responses.txt");

        try (BufferedReader reader = Files.newBufferedReader(path, charset)) 
        {
            String line;
            List<String> keywords = new ArrayList<>();
            StringBuilder responseBuilder = new StringBuilder();
            boolean lastLineBlank = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (lastLineBlank) {
                        throw new IOException("Two or more blank lines in a row in responses.txt");
                    }
                    if (!keywords.isEmpty() && responseBuilder.length() > 0) {
                        String response = responseBuilder.toString().trim();
                        for (String keyword : keywords) {
                            responseMap.put(keyword.toLowerCase(), response);
                        }
                    }
                    keywords.clear();
                    responseBuilder.setLength(0);
                    lastLineBlank = true;
                }
                else {
                    if (keywords.isEmpty()) {
                        String[] keys = line.split(",");
                        for (String key : keys) {
                            keywords.add(key.trim());
                        }
                    } else {
                        if (responseBuilder.length() > 0) {
                            responseBuilder.append(" ");
                        }
                        responseBuilder.append(line.trim());
                    }
                    lastLineBlank = false;
                }
            }

            // End of file check
            if (!keywords.isEmpty() && responseBuilder.length() > 0) {
                String response = responseBuilder.toString().trim();
                for (String keyword : keywords) {
                    responseMap.put(keyword.toLowerCase(), response);
                }
            }
        }
        catch (IOException e) {
            System.err.println("Problem reading responses.txt: " + e.getMessage());
        }
    }



    /**
     * Build up a list of default responses from which we can pick
     * if we don't know what else to say.
     * reads text from default file
     */
    private void fillDefaultResponses()
    {
        Charset charset = Charset.forName("US-ASCII");
        Path path = Paths.get(FILE_OF_DEFAULT_RESPONSES);
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) 
        {
            String line;
            StringBuilder entryBuilder = new StringBuilder();
            boolean lastLineBlank = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    if (lastLineBlank) {
                        throw new IOException("Two or more blank lines in a row in " + FILE_OF_DEFAULT_RESPONSES);
                    }
                    if (entryBuilder.length() > 0) {
                        defaultResponses.add(entryBuilder.toString().trim());
                        entryBuilder.setLength(0); // Reset builder
                    }
                    lastLineBlank = true;
                } else {
                    if (entryBuilder.length() > 0) {
                        entryBuilder.append(" ");
                    }
                    entryBuilder.append(line.trim());
                    lastLineBlank = false;
                }
            }

            if (entryBuilder.length() > 0) {
                defaultResponses.add(entryBuilder.toString().trim());
            }
        }
        catch (IOException e) {
            System.err.println("Problem reading " + FILE_OF_DEFAULT_RESPONSES + ": " + e.getMessage());
        }
        //Last resort
        if (defaultResponses.size() == 0) {
            defaultResponses.add("Could you elaborate on that?");
        }
    }

    /**
     * Randomly select and return one of the default responses.
     * @return     A random default response
     */
    private String pickDefaultResponse()
    {
        // Pick a random number for the index in the default response list.
        // The number will be between 0 (inclusive) and the size of the list (exclusive).
        int index = randomGenerator.nextInt(defaultResponses.size());
        return defaultResponses.get(index);
    }
}
