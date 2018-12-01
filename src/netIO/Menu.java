package netIO;

import managers.Governor;
import neuralNet.Net;
import trainers.Trainer;
import trainers.TrainerAddOne;
import trainers.TrainerNeg;
import trainers.TrainerXOR;

import java.util.LinkedList;
import java.util.Scanner;

public class Menu {

    private Governor gov;

    public Menu(Governor g){
        gov = g;
    }

    public void main(Scanner input) {
        while (true) {
            System.out.println("enter the desired behaviour or \"exit\"");
            String line = input.nextLine();
            Trainer trainer = null;
            switch (line) {
                case "xor":
                    trainer = new TrainerXOR();
                    break;
                case "neg":
                    trainer = new TrainerNeg();
                    break;
                case "addone":
                    trainer = new TrainerAddOne();
                    break;
                case "exit":
                    return;
                default:
                    System.out.println("invalid behaviour");
                    break;
            }

            if(trainer !=null){
                System.out.println(trainer.getClass().getName());
                Actions(input, trainer);
            }
        }
    }

    public void Actions(Scanner input, Trainer trainer) {
        while (true) {
            System.out.println("enter an action or \"back\"");
            String line = input.nextLine();
            switch (line) {
                case "breed":
                    Breed(input, trainer);
                    break;
                case "run":
                    Run(input);
                    break;
                case "bkp":
                    Backprop(input, trainer);
                    break;
                case "test":
                    Test(input, trainer);
                    break;
                case "test pop":
                    gov.testPopulation(trainer, true);
                    break;
                case "print":
                    Print(input);
                    break;
//                case "save":
//                    save(trainer);
//                    break;
//                case "load":
//                    trainer = load();
//                    break;
                case "back":
                    return;
                default:
                    System.out.println("invalid action");
                    break;
            }
        }
    }

    public void Backprop(Scanner input, Trainer trainer) {
        while(true) {
            System.out.println("enter number of generations, \"back\" to back");
            String line = input.nextLine();

            if(line.equals("back"))
                return;

            int generations;
            try{
                generations = Integer.parseInt(line);
            }catch(Exception e) {
                System.out.println("invalid generation count");
                continue;
            }
            boolean shouldLog = false;
            if(generations == 1) {
                shouldLog = true;
            }
            gov.bkpByGenerations(trainer, generations, shouldLog);
            System.out.println("best:");
            trainer.testNet(gov.getNetByRank(1), true);
        }
    }

    public void Test(Scanner input, Trainer trainer) {
        while(true) {
            System.out.println("select net to test using rank, \"back\" to back");
            String line = input.nextLine();

            if(line.equals("back"))
                return;

            int selectedRank;
            try{
                selectedRank = Integer.parseInt(line);
            }catch(Exception e) {
                System.out.println("invalid id");
                continue;
            }
            Net staticNetToTest = gov.getNetByRank(selectedRank);
            trainer.testNet(staticNetToTest, true);
        }
    }

    public void Print(Scanner input) {
        while(true) {
            System.out.println("select net to print using rank, \"back\" to back");
            String line = input.nextLine();

            if(line.equals("back"))
                return;

            int selectedRank;
            try{
                selectedRank = Integer.parseInt(line);
                Net staticNetToPrint = gov.getNetByRank(selectedRank);
                System.out.println(staticNetToPrint.toString());
            }catch(Exception e) {
                System.out.println("invalid id");
            }
        }
    }

    public void Breed(Scanner input, Trainer trainer) {
        while(true) {
            System.out.println("how many generations?");
            String line = input.nextLine();

            if(line.equals("back")) {
                return;
            }

            int userIn;
            try{
                userIn = Integer.parseInt(line);
            }catch(Exception e) {
                e.printStackTrace();
                System.out.println("invalid generation count");
                continue;
            }

            gov.breedPopulation(trainer, userIn);
            break;
        }
    }

    public void Run(Scanner input) {
        while(true) {
            System.out.println("enter inputs, each on a new line, " +
                    "\";\" to end, \"back\" to back, \"clear\" to clear");
            LinkedList<Float> netInputList = new LinkedList<>();

            boolean dontClear = true;
            while(dontClear) {
                String line = input.nextLine();
                switch (line) {
                    case ";":
                        System.out.println("inputs received");
                        float[] netInputArray = new float[netInputList.size()];
                        for(int i=0; i<netInputList.size(); i++) {
                            netInputArray[i] = netInputList.get(i);
                        }
                        RunSelectNet(input, netInputArray);
                        dontClear = false;
                        break;
                    case "back":
                        return;
                    case "clear":
                        dontClear = false;
                        break;
                    default:
                        try{
                            float userIn = Float.parseFloat(line);
                            netInputList.add(userIn);
                        }catch(Exception e) {
                            System.out.println("invalid input");
                        }
                }
            }
        }
    }

    public void RunSelectNet(Scanner input, float[] netInputs) {
        while(true) {
            System.out.println("select net to run using id, \"back\" to back");
            String line = input.nextLine();

            if(line.equals("back"))
                return;

            int selectedId;
            try{
                selectedId = Integer.parseInt(line);
            }catch(Exception e) {
                System.out.println("invalid id");
                continue;
            }
            Net staticNetToRun = gov.getNetById(selectedId);
            if(staticNetToRun ==null) {
                continue;
            }
            gov.runNet(staticNetToRun, netInputs);
        }
    }
}
