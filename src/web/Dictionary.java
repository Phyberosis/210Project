package web;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Scanner;

public class Dictionary {

    private final String OXFORD_DICTIONARY_EN = "https://od-api.oxforddictionaries.Com:443/api/v1/entries/en/";
    private final String CMD_EXIT = "x()";

    private final String APP_ID = "51c48693";
    private final String APP_KEY = "e6fc2848dc5ce3973b94205c5538e2bf";

    public static void main(String[] args) {
        Dictionary d = new Dictionary();
        d.run();
    }

    private String getWebData(String entry) throws FileNotFoundException{
        BufferedReader br = null;

        try {
            URL url = new URL(OXFORD_DICTIONARY_EN+entry);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept","application/json");
            urlConnection.setRequestProperty("app_id",APP_ID);
            urlConnection.setRequestProperty("app_key",APP_KEY);

            br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            String line;

            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {

                sb.append(line);
                sb.append(System.lineSeparator());
            }

            return sb.toString();

        } catch (MalformedURLException e) {
            System.err.println("bad url");
            //e.printStackTrace();
        } catch (FileNotFoundException e){
            throw e;
        } catch (IOException e) {
            System.err.println("could not understand: "+entry+"\nquery must be single English word");
        } finally {

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return "ERR0R";
    }

    private void run(){
        Scanner in = new Scanner(System.in);

        while (true){

            System.out.println("\n=======================================\nenter word to search:\n");

            String entry = in.nextLine();
            if(entry.equals(""))
                continue;

            if(entry.toLowerCase().equals(CMD_EXIT)){
                in.close();
                return;
            }

            System.out.println(entry + ", registered...");
            String raw = "";
            try{
                raw = getWebData(entry);
            }catch (FileNotFoundException e){
                System.err.println("that was not a word");
                continue;
            }


            String[] results = getResults(raw);

            int i = 1;
            int count = results.length;
            for(String res : results){
                System.out.println();
                System.out.println("result "+i+" of "+count);
                System.out.println(res);
                i++;
            }
        }
    }

    private String[] getResults(String raw){
        Scanner scn = new Scanner(raw);
        final String DEF_TAG = "                            \"senses\": ";
        final String LEX_CAT_TAG = "                    \"lexicalCategory\": ";

        LinkedList<String> results = new LinkedList<>();

        while (true){
            String def = null;
            String lexCat = "";
            while(scn.hasNextLine()){
                String line = scn.nextLine();
                if(line.contains(DEF_TAG)){
                    scn.nextLine();
                    scn.nextLine();
                    def = scn.nextLine();
                    def = def.trim().replace("\"", "");
                    break;
                }
            }

            while(scn.hasNextLine()){
                String line = scn.nextLine();
                if(line.contains(LEX_CAT_TAG)){
                    lexCat = line.substring(LEX_CAT_TAG.length()+1, line.length()-2);
                    break;
                }
            }

            if(def == null) {
                String[] ret = new String[results.size()];
                return results.toArray(ret);
            }else{
                results.add(lexCat+"\n"+def);
            }
        }
    }
}
