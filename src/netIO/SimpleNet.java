package netIO;

import managers.Governor;
import managers.Log;
import neuralNet.Net;
import neuralNet.staticTopologyNet.StaticNet;
import trainers.*;

import java.io.*;
import java.util.LinkedList;
import java.util.Scanner;

public class SimpleNet {
    final String DEFAULT = "def";
    final String DEFAULT_SINGLE = "single"; //makes single default net
    final String SAVE_FILE = "SimpleNet.sav";
    private static final boolean SKIP_MENU = true;
//    final String BREED = "breed";
//    final String TEST = "test";
//    final String RUN = "run";
//    final String XOR = "xor";
//    final String END = "end";
    final int[] DEFAULT_NET_COUNTS = {3, 3, 3};
//    final int[] DEFAULT_NET_COUNTS = {2, 2, 1};
    final int POPULATION_COUNT = 99;
    final int INITIAL_GENERATIONS = 9;
    final int TRY_GENERATIONS = 99;

    private Net[] nets;
    private Governor gov;

    public SimpleNet() {
        Log.print( this, "game created");
        Scanner input = new Scanner(System.in);
        userCreateNets(input);
        gov = new Governor(nets);
        input.close();
    }

    private void start(){
        Scanner input = new Scanner(System.in);
        Menu m = new Menu(gov);
        m.main(input);
    }

    private void straightToRun() {
        long start = System.currentTimeMillis();

        nets = new StaticNet[POPULATION_COUNT];
        for (int i = 0; i< POPULATION_COUNT; i++){
            nets[i] = buildNet(i);
        }

        gov = new Governor(nets);

        Trainer trainer = new TrainerAddOne();
        gov.bkpByThreshold(trainer, 0.05f, 1.7f, INITIAL_GENERATIONS,TRY_GENERATIONS);

        Log.print( this, "runtime: "+(System.currentTimeMillis() - start));

        //save(trainer);
        //load();

        Governor g = new Governor();
        Menu m = new Menu(g);
        m.Actions(new Scanner(System.in), trainer);
    }

    private void userCreateNets(Scanner input) {
        LinkedList<Integer> neuronCounts = new LinkedList<>();
        int currIndex = 0;
        while(true)
        {
            System.out.println("! default or number of neurons for layer "+(currIndex+1)
                    +" or enter \"end\"");
            String inputLine = input.nextLine();
            if(currIndex==0 && inputLine.equals(DEFAULT_SINGLE)) {
                nets = new StaticNet[1];
                nets[0] = buildNet(1);
                return;
            }else if (currIndex==0 && inputLine.equals(DEFAULT)) {
                nets = new StaticNet[POPULATION_COUNT];
                for (int i = 0; i< POPULATION_COUNT; i++){
                    nets[i] = buildNet(i);
                }
                return;
            }else if (currIndex!=0 && inputLine.equals("end")) {
                break;
            }else{
                try{
                    int count = Integer.parseInt(inputLine);
                    neuronCounts.add(count);
                }catch (Exception e) {
                    Log.print( this, "that was not an integer, please try again");
                    currIndex--;
                }
            }
            currIndex++;
        }
        nets = new StaticNet[1];
        nets[0] = buildNet(0, neuronCounts.stream()
                .mapToInt(Integer::intValue)
                .toArray());
    }

    private StaticNet buildNet(int id, int[] neuronCounts) {
        return new StaticNet(id, neuronCounts);
    }

    private StaticNet buildNet(int id) {
        return new StaticNet(id, DEFAULT_NET_COUNTS);
    }

    private void save(Trainer trainer){
        Log.print(this,"saving");

        StringBuilder saveData = new StringBuilder();
        File saveFile = new File(System.getProperty("user.dir")+"\\"+SAVE_FILE);
        try {
            FileWriter writer = new FileWriter(saveFile);

            if(trainer != null){
                String trainerType = trainer.getClass().getName();
                saveData.append(trainerType).append("\n");
            }else{
                saveData.append("null").append("\n");
            }

            saveData.append(gov.save());

            String toSave = saveData.toString();
            toSave = toSave.replace("\n", "\r\n");

            writer.write(toSave);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.print( this, "done");
    }

    private Trainer load(){
        File saveFile = new File(System.getProperty("user.dir")+"\\"+SAVE_FILE);
        Trainer loadedTrainer = null;
        try {
            Scanner saveReader = new Scanner(saveFile);

            String line = saveReader.nextLine();
            line = line.replace("trainer.", "").replace("Trainer", "");
            switch (line){
                case "AddOne":
                    loadedTrainer = new TrainerAddOne();
                    break;
                case "XOR":
                    loadedTrainer = new TrainerXOR();
                    break;
                case "Neg":
                    loadedTrainer = new TrainerNeg();
                    break;
                    default:
                        loadedTrainer = new TrainerAddOne();
            }

            line = saveReader.nextLine();
            int populationCount = Integer.parseInt(line);
            StaticNet[] newPop = new StaticNet[populationCount];

            int popIndex = 0;
            while(saveReader.hasNextLine()){
                line = saveReader.nextLine();

                StringBuilder savedNet = new StringBuilder();
                while(!line.contains("end")){
                    savedNet.append(line);
                    line = saveReader.nextLine();
                }

                newPop[popIndex] = new StaticNet(savedNet.toString());
            }

        } catch (FileNotFoundException e) {
            Log.print( this, "no save to load!");
            return new TrainerAddOne();
        }

        return loadedTrainer;
    }

    public static void main (String args[]) {
        System.out.println("SimpleNet init...");
        SimpleNet simpleNet = new SimpleNet();
        if (SKIP_MENU) {
            simpleNet.straightToRun();
        } else {
            simpleNet.start();
        }
    }
}
