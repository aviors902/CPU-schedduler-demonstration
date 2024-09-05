import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class A1 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java OpenFile <file-path>");
            return;
        }
        String filePath = args[0];
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("File does not exist at given location: " + filePath);
            return;
        }

        List<List<String>> importedFile = importFile(filePath);
        List<Process> processList = Process.getProcessList(importedFile);
        // System.out.println(Scheduler.FCFS(processList, Process.getDispatcher(importedFile)));
        // System.out.println(Scheduler.SRT(processList, Process.getDispatcher(importedFile)));
        System.out.println(Scheduler.FBV(processList, Process.getDispatcher(importedFile)));

    }








    public static List<List<String>> importFile(String filePath){
        File file = new File(filePath);
        /* Initializing the list of lists outside of the try block allows the list to be used outside of the Try block. */
        List<List<String>> linesList = new ArrayList<>();            
        /* Readint the input file and making a list from each line in the file, to be converted into processes */
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()){
                    linesList.add(Arrays.asList(line.split(":\\s+")));
                }
            }
            /* Exception Handling */
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
        return linesList;
    }
}

class Process {
    private String PID;
    private int  ArrTime, SrvTime, Tickets, TimeRemaining, lowPrio;
    private int ElapsedTime = 0;

    public Process(String ID, int Time, int ServeTime, int num){
        PID = ID;
        ArrTime = Time;
        SrvTime = ServeTime;
        TimeRemaining = ServeTime;
        Tickets = num;
        
    }

    public void setPID(String ID){
        PID = ID;
    }

    public void setArrTime(int Time){
        ArrTime = Time;
    }

    public void setSrvTime(int ServeTime){
        SrvTime = ServeTime;
    }

    public void setTickets(int num){
        Tickets = num;
    }

    public void increaseElapsedTime(int increment){
        ElapsedTime += increment;
    }

    public void lowerTimeRemaining(int decrement){
        if((TimeRemaining - decrement) > 0){
            TimeRemaining = TimeRemaining - decrement;
        }
        else TimeRemaining = 0;
    }

    public void increaseLowPrioTicks(int increment){
        lowPrio =+ increment;
    }

    public void resetLowPrioTicks(){
        lowPrio = 0;
    }

    public String getPID(){
        return PID;
    }

    public int getArrTime(){
        return ArrTime;
    }

    public int getSrvTime(){
        return SrvTime;
    }

    public int getTickets(){
        return Tickets;
    }

    public int getElapsedTime(){
        return ElapsedTime;
    }

    public int getTimeRemaining(){
        return TimeRemaining;
    }

    public int getLowPrioTicks(){
        return lowPrio;
    }


    public static List<Process> getProcessList(List<List<String>> linesList){
        List<Process> ProcessList  = new ArrayList<>();
        int i = 0;

        while(i < linesList.size()){
            if(linesList.get(i).size() > 1){
                if((linesList.get(i)).get(0).equals("PID")){
                    // Process p creates a new Process object with the constructor Process(String PID, Int ArrTime, Int SrvTime, Int Tickets)
                    Process p = new Process(linesList.get(i).get(1), Integer.parseInt(linesList.get(i+1).get(1)),Integer.parseInt(linesList.get(i+2).get(1)),Integer.parseInt(linesList.get(i+3).get(1)));
                    ProcessList.add(p);
                    i += 3;
                }
                else i++;
            }
            else i++;
        }

        return ProcessList;
    }

    public static List<Integer> getRandom(List<List<String>> linesList){
        List<Integer> RandomList  = new ArrayList<>();
        int i = 0;
        while(!linesList.get(i).get(0).equals("BEGINRANDOM")){
            i++;
        }
        i++;
        while(!linesList.get(i).get(0).equals("ENDRANDOM")){
            int line = Integer.parseInt(linesList.get(i).get(0));
            RandomList.add(line);
            i++;
        }
        return RandomList;
    }

    public static int getDispatcher(List<List<String>> linesList){
        int i=0;
        while(!linesList.get(i).get(0).equals("DISP")){
            i++;
        }
        return Integer.parseInt(linesList.get(i).get(1));
    }
}

class Scheduler{
    public static List<Process> sortListByArrTime(List<Process> Processes){
        List<Process> ProcessList = Processes;
        List<Process> ArrivalOrder = new ArrayList<>();
        // Sorting the list based on arrival time
        while (!ProcessList.isEmpty()) {
            int tempArr = Integer.MAX_VALUE;
            int ArrivalIndex = -1;
    
            for (int processIndex = 0; processIndex < ProcessList.size(); processIndex++) {
                if (ProcessList.get(processIndex).getArrTime() < tempArr) {
                    tempArr = ProcessList.get(processIndex).getArrTime();
                    ArrivalIndex = processIndex;
                }
            }
    
            if (ArrivalIndex != -1) {
                ArrivalOrder.add(ProcessList.get(ArrivalIndex));
                ProcessList.remove(ArrivalIndex);
            }
        }
        return ArrivalOrder;
    }
    public static List<Process> sortListBySRT(List<Process> Processes){
        List<Process> ProcessList = Processes;
        List<Process> TimeRemaining = new ArrayList<>();
        // Sorting the list based on arrival time
        while (!ProcessList.isEmpty()) {
            int tempArr = Integer.MAX_VALUE;
            int ArrivalIndex = -1;
    
            for (int processIndex = 0; processIndex < ProcessList.size(); processIndex++) {
                if (ProcessList.get(processIndex).getTimeRemaining() < tempArr) {
                    tempArr = ProcessList.get(processIndex).getTimeRemaining();
                    ArrivalIndex = processIndex;
                }
            }
    
            if (ArrivalIndex != -1) {
                TimeRemaining.add(ProcessList.get(ArrivalIndex));
                ProcessList.remove(ArrivalIndex);
            }
        }
        return TimeRemaining;
    }

    public static String FCFS(List<Process> ProcessList, int Dispatcher) {
        List<Process> Processes = new ArrayList<>(ProcessList);
        int WaitingTime = 0;
        int StartTime, TurnaroundTime;
        String Schedule = "FCFS:\n";
        String TurnAround = "\nProcess  Turnaround Time  Waiting Time\n";
        int i = 0;

        int processCount = Processes.size();
        while (i < processCount) {
            int ArrTime = Processes.get(i).getArrTime();
            int SrvTime = Processes.get(i).getSrvTime();
    
            StartTime = Math.max(WaitingTime, ArrTime) + Dispatcher;
            TurnaroundTime = StartTime + SrvTime - ArrTime;
            WaitingTime = StartTime - ArrTime;

            Schedule += "T" + StartTime + ": " + Processes.get(i).getPID() + "\n";
            TurnAround += Processes.get(i).getPID() + "       "  + TurnaroundTime + "               "  + WaitingTime + "\n";

            WaitingTime = StartTime + SrvTime;
            i++;
        }
        return Schedule + TurnAround;
    }

    public static String SRT(List<Process> Processes, int Dispatcher) {
        int currentTime = 0;
        int totalProcesses = Processes.size();
        List<Process> ReadyQueue = new ArrayList<>();
        List<Process> SortedProcesses = new ArrayList<>(sortListBySRT(Processes));
        List<Process> completedProcesses = new ArrayList<>();
        List<List<String>> TimeStamps = new ArrayList<>();
        String Output = "";
        String previousPID = "Idle";
        boolean minExecution = false;
        // Min execution is because a program must execute a minimum of 1 time unit if it is loaded in
    
        while (completedProcesses.size() != totalProcesses) {

                // Checking if new processes arrived
                for (int i = 0; i < SortedProcesses.size(); i++) {
                    if (SortedProcesses.get(i).getArrTime() < currentTime) {
                        ReadyQueue.add(SortedProcesses.get(i));
                        SortedProcesses.remove(i);
                        i--;
                    }
                }

            if(currentTime ==0){
                ReadyQueue = sortListBySRT(ReadyQueue);
            }

            if (!ReadyQueue.isEmpty()) {

                // Queue is only sorted if the process has executed more than 1 iteration
                if (minExecution) ReadyQueue = Scheduler.sortListBySRT(ReadyQueue);

                // Checks for context switching, if it occurred then minExecution is set to false
                if ((previousPID.equals(ReadyQueue.get(0).getPID())) && (!previousPID.equals("Idle"))) {
                    // "If program is complete, run dispatcher and move the program to the "Completed Programs"
                    if (ReadyQueue.get(0).getTimeRemaining() == 0) {
                        completedProcesses.add(ReadyQueue.get(0));
                        ReadyQueue.remove(0);
                        minExecution = false;
                    }
                    // If program is not finished, run for 1 unit of time
                    else {
                        ReadyQueue.get(0).lowerTimeRemaining(1);
                        // Sorting the ready queue
                        minExecution = true;

                    }
                }
                else {
                    minExecution = false;
                }

                if (ReadyQueue.isEmpty()) previousPID = "Idle";
                else {
                    previousPID = ReadyQueue.get(0).getPID();

                    for(int a = 0; a < ReadyQueue.size(); a++){
                        ReadyQueue.get(a).increaseElapsedTime(1);
                    }

                }

                // Output Information Capturing
                List<String> entry = new ArrayList<>();
                entry.add("T" + currentTime + ": ");
                entry.add(previousPID);
                TimeStamps.add(entry);
                
                



            }
            currentTime++;
        }

        if(!completedProcesses.isEmpty()){
            if(!TimeStamps.isEmpty()){
                for(int x = 0; x < TimeStamps.size(); x++){
                    if(x < TimeStamps.size()-1){
                        if((TimeStamps.get(x).get(1).equals(TimeStamps.get((x+1)).get(1))) || (TimeStamps.get(x+1).get(1).equals("Idle"))){
                            TimeStamps.remove(x+1);
                            x--;
                        }

                    }
                }
                for (int t = 0; t < TimeStamps.size(); t++){
                    Output += (TimeStamps.get(t).get(0) + TimeStamps.get(t).get(1) + "\n");
                }
            }

            completedProcesses = sortListByArrTime(completedProcesses);

            Output += "\nProcess  Turnaround Time  Waiting Time\n";
            
            for(int i = 0; i < completedProcesses.size(); i++){
                Output += completedProcesses.get(i).getPID() + "       " + Integer.toString(completedProcesses.get(i).getElapsedTime()) + "                " + (completedProcesses.get(i).getElapsedTime() - completedProcesses.get(i).getSrvTime()) + "\n";
            }
        
        }

        return Output; // Replace this with the actual return logic
    }

    public static String FBV(List<Process> Processes, int Dispatcher){
        List<List<String>> TimeStamps = new ArrayList<>();
        List<Process> SortedProcesses = new ArrayList<>(Processes);
        List<Process> HighPriority = new ArrayList<>();
        List<Process> MediumPriority = new ArrayList<>();
        List<Process> LowPriority = new ArrayList<>();
        List<Process> CompletedProcesses = new ArrayList<>();
        int i;
        int currentTime = 0;
        String Output = "";

        while(CompletedProcesses.size() != Processes.size()){



            // Checking if new processes arrived
            for (i = 0; i < SortedProcesses.size(); i++) {
                if (SortedProcesses.get(i).getArrTime() < currentTime) {
                    HighPriority.add(SortedProcesses.get(i));
                    SortedProcesses.remove(i);
                    i--;
                }
            }

            if((HighPriority.isEmpty()) && (MediumPriority.isEmpty()) && (LowPriority.isEmpty())){
                currentTime++;

                List<String> entry = new ArrayList<>();
                entry.add("T" + currentTime + ": ");
                entry.add("Idle");
                TimeStamps.add(entry);
            }

            while(!HighPriority.isEmpty()){
                
                // Output Information Capturing
                List<String> entry = new ArrayList<>();
                entry.add("T" + currentTime + ": ");
                entry.add(HighPriority.get(0).getPID());
                TimeStamps.add(entry);

                HighPriority.get(0).lowerTimeRemaining(2);
                for (i = 0; i < HighPriority.size(); i++){
                    HighPriority.get(i).increaseElapsedTime(2);
                }

                if(HighPriority.get(0).getTimeRemaining() == 0){
                    CompletedProcesses.add(HighPriority.get(0));
                }
                else MediumPriority.add(HighPriority.get(0));

                HighPriority.remove(0);
                // 2 ms slice has been consumed
                currentTime += 2;

                // Checking if new processes arrived during the time slice the high priority task was being executed
                for (i = 0; i < SortedProcesses.size(); i++) {
                    if (SortedProcesses.get(i).getArrTime() < currentTime) {
                        HighPriority.add(SortedProcesses.get(i));
                        SortedProcesses.remove(i);
                        i--;
                    }
                }

                if(!LowPriority.isEmpty()){
                    for(i = 0; i < LowPriority.size(); i++){
                        LowPriority.get(i).increaseLowPrioTicks(2);
                        if(LowPriority.get(i).getLowPrioTicks() > 16){
                            HighPriority.add(LowPriority.get(i));
                            LowPriority.remove(i);
                        }
                    }
                }


            }


            while(!MediumPriority.isEmpty() && HighPriority.isEmpty()){

                // Output Information Capturing
                List<String> entry = new ArrayList<>();
                entry.add("T" + currentTime + ": ");
                entry.add(MediumPriority.get(0).getPID());
                TimeStamps.add(entry);

                MediumPriority.get(0).lowerTimeRemaining(4);
                for (i = 0; i < MediumPriority.size(); i++){
                    MediumPriority.get(i).increaseElapsedTime(4);
                }
                

                if(MediumPriority.get(0).getTimeRemaining() == 0){
                    CompletedProcesses.add(MediumPriority.get(0));
                }
                else LowPriority.add(MediumPriority.get(0));

                MediumPriority.remove(0);
                // 4 ms slice has been consumed
                currentTime += 4;

                // Checking if new processes arrived during the time slice the medium priority task was being executed
                for (i = 0; i < SortedProcesses.size(); i++) {
                    if (SortedProcesses.get(i).getArrTime() < currentTime) {
                        HighPriority.add(SortedProcesses.get(i));
                        SortedProcesses.remove(i);
                        i--;
                    }
                }

                if(!LowPriority.isEmpty()){
                    for(i = 0; i < LowPriority.size(); i++){
                        LowPriority.get(i).increaseLowPrioTicks(4);
                        if(LowPriority.get(i).getLowPrioTicks() > 16){
                            HighPriority.add(LowPriority.get(i));
                            LowPriority.remove(i);
                        }
                    }
                }

                
            }

            while(!LowPriority.isEmpty() && HighPriority.isEmpty() && MediumPriority.isEmpty()){

                // Output Information Capturing
                List<String> entry = new ArrayList<>();
                entry.add("T" + currentTime + ": ");
                entry.add(LowPriority.get(0).getPID());
                TimeStamps.add(entry);

                LowPriority.get(0).lowerTimeRemaining(4);
                for (i = 0; i < LowPriority.size(); i++){
                    LowPriority.get(i).increaseElapsedTime(4);
                }

                if(LowPriority.get(0).getTimeRemaining() == 0){
                    CompletedProcesses.add(LowPriority.get(0));
                    LowPriority.remove(0);
                }

                // 4 ms slice has been consumed
                currentTime += 4;

                // Checking if new processes arrived during the time slice the low priority task was being executed
                for (i = 0; i < SortedProcesses.size(); i++) {
                    if (SortedProcesses.get(i).getArrTime() < currentTime) {
                        HighPriority.add(SortedProcesses.get(i));
                        SortedProcesses.remove(i);
                        i--;
                    }
                }

                if(!LowPriority.isEmpty()){
                    for(i = 0; i < LowPriority.size(); i++){
                        LowPriority.get(i).increaseLowPrioTicks(4);
                        if(LowPriority.get(i).getLowPrioTicks() > 16){
                            HighPriority.add(LowPriority.get(i));
                            LowPriority.remove(i);
                        }
                    }
                }
            }

            
            

        }

        if(!TimeStamps.isEmpty()){
            for(int x = 0; x < TimeStamps.size(); x++){
                if(x < TimeStamps.size()-1){
                    if((TimeStamps.get(x).get(1).equals(TimeStamps.get((x+1)).get(1))) || (TimeStamps.get(x+1).get(1).equals("Idle"))){
                        TimeStamps.remove(x+1);
                        x--;
                    }

                }
            }
            for (int t = 0; t < TimeStamps.size(); t++){
                Output += (TimeStamps.get(t).get(0) + TimeStamps.get(t).get(1) + "\n");
            }
        }

        for (i = 0; i < CompletedProcesses.size(); i++){
            System.out.println(CompletedProcesses.get(i).getPID());
        }

        return Output;
    }
    
    
}    