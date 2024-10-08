/*
 * This is an assignment piece for comp2240 (Operating Systems)
 * Semester 2 2024 
 * The purpose of this assignment is to demonstrate some of the fundamental cpu Task Scheduling algorithms
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


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
        List<Integer> Lottery = Process.getRandomLottery(importedFile);

        List<Float> FCFSAverages = Scheduler.FCFS(processList, Process.getDispatcher(importedFile));
        System.out.println("--------------------------------------------------");
        List<Float> SRTAverages = Scheduler.SRT(processList, Process.getDispatcher(importedFile));
        System.out.println("--------------------------------------------------");
        List<Float> FBVAverages = Scheduler.FBV(processList, Process.getDispatcher(importedFile));
        System.out.println("--------------------------------------------------");
        List<Float> LTRAverages = Scheduler.LTR(processList, Process.getDispatcher(importedFile), Lottery);
        System.out.println("--------------------------------------------------");

        System.out.println("Summary\nAlgorithm   Average Turnaround Time  Waiting Time");
        System.out.println("FCFS         " + FCFSAverages.get(0) + "                      " + FCFSAverages.get(1));
        System.out.println("SRT          " + SRTAverages.get(0) + "                       " + SRTAverages.get(1));
        System.out.println("FBV          " + FBVAverages.get(0) + "                      " + FBVAverages.get(1));
        System.out.println("LTR          " + LTRAverages.get(0) + "                      " + LTRAverages.get(1));

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
    private int TurnAroundTime = 0;

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

    public void updateTurnAroundTime(int currentTime){
        TurnAroundTime = currentTime - ArrTime;
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

    public int getTurnAroundTime(){
        return TurnAroundTime;
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

    public static List<Integer> getRandomLottery(List<List<String>> linesList){
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
    
    // Simple cloning function for creating deep copies of processLists
    public static List<Process> clone(List<Process> Original){
        List<Process> copy = new ArrayList<>();
        for (Process current : Original){
            Process temp = new Process(current.getPID(), current.getArrTime(), current.getSrvTime(), current.getTickets());
            copy.add(temp);
        }
        return copy;
    }

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




    public static List<Float> FCFS(List<Process> ProcessList, int Dispatcher) {
        List<Process> Processes = clone(ProcessList);
        List<Process> ReadyQueue = new ArrayList<>();
        List<Float> averages = new ArrayList<>();
        String Schedule = "FCFS:\n";
        String TurnAround = "\nProcess  Turnaround Time  Waiting Time\n";
        int i;
        int CurrentTime = 0;
        float TotalTurnAroundTime = 0;
        float TotalWaitTime = 0;
        int FinishedCount = 0;

        int processCount = Processes.size();

        while (FinishedCount < processCount){

            //Checking if new processes have arrived
            for (i = 0; i < Processes.size(); i++){
                if (Processes.get(i).getArrTime() <= CurrentTime) {
                    ReadyQueue.add(Processes.get(i));
                    Processes.remove(i);
                    i--;
                }
            }

            if (!ReadyQueue.isEmpty()){

                Schedule += "T" + CurrentTime + ": " + ReadyQueue.get(0).getPID() + "\n";

                CurrentTime += ReadyQueue.get(0).getSrvTime() + Dispatcher;

                for (i = 0; i < ReadyQueue.size(); i++){
                    ReadyQueue.get(i).updateTurnAroundTime(CurrentTime);
                }

                TotalTurnAroundTime += ReadyQueue.get(0).getTurnAroundTime();
                TotalWaitTime += (ReadyQueue.get(0).getTurnAroundTime()-ReadyQueue.get(0).getSrvTime());

                TurnAround += ReadyQueue.get(0).getPID() + "       "  + ReadyQueue.get(0).getTurnAroundTime() + "                "  + (ReadyQueue.get(0).getTurnAroundTime()-ReadyQueue.get(0).getSrvTime()) + "\n";

                ReadyQueue.remove(0);
                FinishedCount++;

            }
            else CurrentTime++;

        }


        System.out.println(Schedule + TurnAround);

        averages.add((TotalTurnAroundTime/FinishedCount));
        averages.add(TotalWaitTime/FinishedCount);

        return averages;
    }

    public static List<Float> SRT(List<Process> Processes, int Dispatcher) {
        int currentTime = 0;
        int totalProcesses = Processes.size();
        float TotalTurnAroundTime;
        float TotalWaitTime;
        List<Process> ReadyQueue = new ArrayList<>();
        List<Process> SortedProcesses = new ArrayList<>(sortListBySRT(clone(Processes)));
        List<Process> completedProcesses = new ArrayList<>();
        List<List<String>> TimeStamps = new ArrayList<>();
        List<Float> averages = new ArrayList<>();
        String Output = "\nSRT:\n";
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

                // Output Formatting
                if (ReadyQueue.isEmpty()) previousPID = "Idle";
                else {
                    previousPID = ReadyQueue.get(0).getPID();

                    for(int a = 0; a < ReadyQueue.size(); a++){
                        ReadyQueue.get(a).updateTurnAroundTime(currentTime);
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
                Output += completedProcesses.get(i).getPID() + "       " + Integer.toString(completedProcesses.get(i).getTurnAroundTime()) + "                " + (completedProcesses.get(i).getTurnAroundTime() - completedProcesses.get(i).getSrvTime()) + "\n";
            }

            System.out.println(Output);

            // Calculating Average Turnaround Time
            TotalTurnAroundTime = 0;
            for(Process Step : completedProcesses){
                TotalTurnAroundTime += Step.getTurnAroundTime();
            }
            averages.add(TotalTurnAroundTime/completedProcesses.size());

            //Calculating average wait Time
            TotalWaitTime = 0;
            for (Process Step : completedProcesses){
                TotalWaitTime += (Step.getTurnAroundTime()-Step.getSrvTime());
            }
            averages.add(TotalWaitTime/completedProcesses.size());
        
        }

        return averages;
    }

    public static List<Float> FBV(List<Process> Processes, int Dispatcher){
        String Output = "\nFBV:\n";
        List<Process> SortedProcesses = sortListByArrTime(clone(Processes));
        List<Process> HighPriority = new ArrayList<>();
        List<Process> MediumPriority = new ArrayList<>();
        List<Process> LowPriority = new ArrayList<>();
        List<Process> CompletedProcesses = new ArrayList<>();
        List<Float> averages = new ArrayList<>();
        int i;
        int currentTime = 0;

        while(CompletedProcesses.size() != Processes.size()){         
            // Checking if new processes arrived
            for (i = 0; i < SortedProcesses.size(); i++) {
                if (SortedProcesses.get(i).getArrTime() <= currentTime) {
                    HighPriority.add(SortedProcesses.get(i));
                    SortedProcesses.remove(i);
                    i--;
                }
            }

            // idle condition, no processes have arrived and all queues are empty
            if((HighPriority.isEmpty()) && (MediumPriority.isEmpty()) && (LowPriority.isEmpty())){
                currentTime++;
            }
            
            while(!HighPriority.isEmpty()){
                currentTime += Dispatcher;

                
                // Output Information Capturing
                Output += "T" + currentTime + ": " + HighPriority.get(0).getPID() + "\n";

                // Time slice has been completed. If the remaining time is less than the full slice, the full slice is not consumed, only the time remaining
                if(HighPriority.get(0).getTimeRemaining() >= 2){
                    currentTime += 2;
                }
                else currentTime += HighPriority.get(0).getTimeRemaining();
                

                // Time Slice removed from remaining time for the process
                HighPriority.get(0).lowerTimeRemaining(2);

                // Recording the Turnaround Time for the processes in queue
                for (i = 0; i < HighPriority.size(); i++){
                    HighPriority.get(i).updateTurnAroundTime(currentTime);
                }
                for (i = 0; i < MediumPriority.size(); i++){
                    MediumPriority.get(i).updateTurnAroundTime(currentTime);
                }
                for (i = 0; i < LowPriority.size(); i++){
                    LowPriority.get(i).updateTurnAroundTime(currentTime);
                }

                // Checking if the current process is now completed. If not, lowering Priority
                if(HighPriority.get(0).getTimeRemaining() == 0){
                    CompletedProcesses.add(HighPriority.get(0));
                }
                // Task is moved either to lower priority or to completed tasks, it will never remain in high prio
                else MediumPriority.add(HighPriority.get(0));
                HighPriority.remove(0);

                // Checking if new processes arrived during the time slice the high priority task was being executed
                for (i = 0; i < SortedProcesses.size(); i++) {
                    if (SortedProcesses.get(i).getArrTime() <= currentTime) {
                        HighPriority.add(SortedProcesses.get(i));
                        SortedProcesses.remove(i);
                        i--;
                    }
                }
                // Checking the Time Elapsed for any processes in Low Prio. If it exceeds 16ms, they are moved to high prio.
                if(!LowPriority.isEmpty()){
                    for(i = 0; i < LowPriority.size(); i++){
                        LowPriority.get(i).increaseLowPrioTicks(2);
                        if(LowPriority.get(i).getLowPrioTicks() > 16){
                            HighPriority.add(LowPriority.get(i));
                            LowPriority.remove(i);
                            i--;
                        }
                    }
                }
                
                

            }
          
            while(!MediumPriority.isEmpty() && HighPriority.isEmpty()){
                currentTime += Dispatcher;

                // Output Information Capturing
                Output += "T" + currentTime + ": " + MediumPriority.get(0).getPID() + "\n";

                // Time Slice is consumed. If time remaining is less than the full slice, the full slice is not used, only the time remaining
                if(MediumPriority.get(0).getTimeRemaining() >= 4){
                    currentTime += 4;
                }
                else currentTime += MediumPriority.get(0).getTimeRemaining();

                // Time slice for the process being executed
                MediumPriority.get(0).lowerTimeRemaining(4);

                // Tracking Turnaround time for all processes
                for (i = 0; i < MediumPriority.size(); i++){
                    MediumPriority.get(i).updateTurnAroundTime(currentTime);
                }
                for (i = 0; i < LowPriority.size(); i++){
                    LowPriority.get(i).updateTurnAroundTime(currentTime);
                }
                
                // Checking if the process is completed, if not then lowering priority
                if(MediumPriority.get(0).getTimeRemaining() == 0){
                    CompletedProcesses.add(MediumPriority.get(0));
                }
                else LowPriority.add(MediumPriority.get(0));
                // Task is moved either to lower priority or to completed tasks, it will never remain in Medium prio
                MediumPriority.remove(0);

                // Checking if new processes arrived during the time slice the medium priority task was being executed
                for (i = 0; i < SortedProcesses.size(); i++) {
                    if (SortedProcesses.get(i).getArrTime() <= currentTime) {
                        HighPriority.add(SortedProcesses.get(i));
                        SortedProcesses.remove(i);
                        i--;
                    }
                }

                // Checking the Time Elapsed for any processes in Low Prio. If it exceeds 16ms, they are moved to high prio.
                if(!LowPriority.isEmpty()){
                    for(i = 0; i < LowPriority.size(); i++){
                        LowPriority.get(i).increaseLowPrioTicks(4);
                        if(LowPriority.get(i).getLowPrioTicks() > 16){
                            HighPriority.add(LowPriority.get(i));
                            LowPriority.remove(i);
                            i--;
                        }
                    }
                } 
                
            }

            while(!LowPriority.isEmpty() && HighPriority.isEmpty() && MediumPriority.isEmpty()){

                currentTime += Dispatcher;

                // Output Information Capturing
                Output += "T" + currentTime + ": " + LowPriority.get(0).getPID() + "\n";

                // Time Slice is consumed. If time remaining is less than the full slice, the full slice is not used, only the time remaining
                if(LowPriority.get(0).getTimeRemaining() >= 4){
                    currentTime += 4;
                }
                else currentTime += LowPriority.get(0).getTimeRemaining();

                // Tracking the turnaround time for all tasks in the queue
                LowPriority.get(0).lowerTimeRemaining(4);
                for (i = 0; i < LowPriority.size(); i++){
                    LowPriority.get(i).updateTurnAroundTime(currentTime);
                }

                // If process is complete, it is moved to the completed processes list, otherwise it remains in low priority queue
                if(LowPriority.get(0).getTimeRemaining() == 0){
                    CompletedProcesses.add(LowPriority.get(0));
                    LowPriority.remove(0);
                }

                // Checking if new processes arrived during the time slice the low priority task was being executed
                for (i = 0; i < SortedProcesses.size(); i++) {
                    if (SortedProcesses.get(i).getArrTime() <= currentTime) {
                        HighPriority.add(SortedProcesses.get(i));
                        SortedProcesses.remove(i);
                        i--;
                    }
                }

                // Checking the Time Elapsed for any processes in Low Prio. If it exceeds 16ms, they are moved to high prio.
                if(!LowPriority.isEmpty()){
                    for(i = 0; i < LowPriority.size(); i++){
                        LowPriority.get(i).increaseLowPrioTicks(4);
                        if(LowPriority.get(i).getLowPrioTicks() > 16){
                            LowPriority.get(i).resetLowPrioTicks();
                            HighPriority.add(LowPriority.get(i));
                            LowPriority.remove(i);
                            i--;
                        }
                    }
                }
                
            } 

        }

        
        Output += "\nProcess  Turnaround Time  Waiting Time\n";
        i = 0;
        while (i < CompletedProcesses.size()){
            Output += CompletedProcesses.get(i).getPID() + "        " + CompletedProcesses.get(i).getTurnAroundTime() + "               " + (CompletedProcesses.get(i).getTurnAroundTime()-CompletedProcesses.get(i).getSrvTime()) + "\n";
            i++;
        }
        
        // Calculating Average Turnaround Time
        float TotalTurnAround = 0;
        for(Process Step : CompletedProcesses){
            TotalTurnAround += Step.getTurnAroundTime();
        }
        averages.add(TotalTurnAround/CompletedProcesses.size());

        //Calculating average wait Time
        float TotalWaitTime = 0;
        for (Process Step : CompletedProcesses){
            TotalWaitTime += (Step.getTurnAroundTime()-Step.getSrvTime());
        }
        averages.add(TotalWaitTime/CompletedProcesses.size());

        System.out.println(Output);

        return averages;
        
    } 

    public static List<Float> LTR(List<Process> Processes, int Dispatcher, List<Integer> RandomNumbers){
        List<Float> averages = new ArrayList<>(); 
        List<Integer> LotteryNumbers = RandomNumbers.stream().collect(Collectors.toList());
        List<Process> ProcessList = clone(Processes);
        List<Process> ReadyQueue = new ArrayList<>();
        List<Process> CompletedProcesses = new ArrayList<>();
        int CurrentTime = 0;
        int TotalTickets = 0;
        int LotteryCount = 0;
        int rollLottery; 
        int i;
        String Output = "\nLTR:\n";

        while (CompletedProcesses.size() != Processes.size()){
            // Checking if any processes have arrived
            for (i = 0; i < ProcessList.size(); i++) {
                if (ProcessList.get(i).getArrTime() <= CurrentTime) {
                    ReadyQueue.add(ProcessList.get(i));
                    TotalTickets += ProcessList.get(i).getTickets();
                    ProcessList.remove(i);
                    i--;
                }
            }

            if(ReadyQueue.isEmpty()){
                CurrentTime++;
                // Checking if any processes have arrived
                for (i = 0; i < ProcessList.size(); i++) {
                    if (ProcessList.get(i).getArrTime() <= CurrentTime) {
                        ReadyQueue.add(ProcessList.get(i));
                        TotalTickets += ProcessList.get(i).getTickets();
                        ProcessList.remove(i);
                        i--;
                    }
                }
            }
            
            if(!ReadyQueue.isEmpty()){

                CurrentTime += Dispatcher;

                rollLottery = (LotteryNumbers.get(LotteryCount)) % TotalTickets;

                // Finding the "winning" process
                int TicketNumber = 0;
                i = 0;
                while(rollLottery > TicketNumber){
                    TicketNumber += ReadyQueue.get(i).getTickets();
                    if (rollLottery < TicketNumber) break;
                    i++;
                    
                } 

                // Output Information Capturing
                Output += "T" + CurrentTime + ": " + ReadyQueue.get(i).getPID() + "\n";
                // Time Reduction & Wait time tracking Logic for each process
                if(ReadyQueue.get(i).getTimeRemaining() >= 3){
                    CurrentTime += 3;
                    ReadyQueue.get(i).lowerTimeRemaining(3);
                    ReadyQueue.get(i).updateTurnAroundTime(CurrentTime);
                }
                else{
                    CurrentTime += ReadyQueue.get(i).getTimeRemaining();
                    ReadyQueue.get(i).lowerTimeRemaining(3);
                    ReadyQueue.get(i).updateTurnAroundTime(CurrentTime);
                }

                // Checking if any processes have arrived during operation
                for (int z = 0; z < ProcessList.size(); z++) {
                    if (ProcessList.get(z).getArrTime() <= CurrentTime) {
                        ReadyQueue.add(ProcessList.get(z));
                        TotalTickets += ProcessList.get(z).getTickets();
                        ProcessList.remove(z);
                        z--;
                    }
                }
                
                // Checking to see if the process is finished, otherwise adding it to the back of the queue
                if (ReadyQueue.get(i).getTimeRemaining() == 0){
                    CompletedProcesses.add(ReadyQueue.get(i));
                    TotalTickets -= ReadyQueue.get(i).getTickets();
                    ReadyQueue.remove(i);
                }
                else{
                    Process temp = ReadyQueue.get(i);
                    ReadyQueue.remove(i);
                    ReadyQueue.add(temp);
                }

            }


            if(LotteryCount == LotteryNumbers.size()-1){
                LotteryCount = 0;
            }
            else LotteryCount++;
        }

        Output += "\nProcess  Turnaround Time  Waiting Time\n";
        i = 0;
        while (i < CompletedProcesses.size()){
            Output += CompletedProcesses.get(i).getPID() + "        " + CompletedProcesses.get(i).getTurnAroundTime() + "               " + (CompletedProcesses.get(i).getTurnAroundTime()-CompletedProcesses.get(i).getSrvTime()) + "\n";
            i++;
        }
        
        // Calculating Average Turnaround Time
        float TotalTurnAround = 0;
        for(Process Step : CompletedProcesses){
            TotalTurnAround += Step.getTurnAroundTime();
        }
        averages.add(TotalTurnAround/CompletedProcesses.size());

        //Calculating average wait Time
        float TotalWaitTime = 0;
        for (Process Step : CompletedProcesses){
            TotalWaitTime += (Step.getTurnAroundTime()-Step.getSrvTime());
        }
        averages.add(TotalWaitTime/CompletedProcesses.size());

        System.out.println(Output);

        return averages;
    }
}    