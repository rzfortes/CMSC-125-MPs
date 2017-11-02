package lab2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;
import java.util.Scanner;

public class cpu_scheduling_algo {
	
	ArrayList<String> temp_storage = new ArrayList<>();
	private int[][] data = new int[20][7];
	private Queue<Integer> queue = new ArrayDeque<>();
	private Queue<Integer> min = new ArrayDeque<>();
	private Queue<Integer> t = new ArrayDeque<>();
	private Queue<Integer> temp = new ArrayDeque<>();
	boolean fc = false;
	boolean sj = false;
	boolean sr = false;
	boolean p = false;
	boolean rr = false;
	
	private int[] fcfs = new int[20];
	private int[] sjf = new int[20];
	private int[] srpt = new int[20];
	private int[] priority = new int[20];
	private int[] round_robin = new int[20];
	private double[] at = new double[5];
	
	// public constructor
	cpu_scheduling_algo() {
		// do nothing
	}
	
	public void menu() {
		// make a menu
		System.out.println("\nYou are about to see how CPU Scheduling Algorithms are implemented."
				+ "\nThere are two sample data given."
				+ "\n\nPlease enter your choice."
				+ "\n1. process1.txt\n2. process2.txt");
		
		Scanner sc = new Scanner(System.in);
		String filename = "";
		int choice = sc.nextInt();
		
		// fill in the filename
		if(choice == 1) {
			filename = "process1.txt";
		} else if(choice == 2) {
			filename = "process2.txt";
		}
		
		// call readFile()
		readFile(filename, choice);
		
	}
	
	public void readFile(String filename, int choice) {
		
		String line = "";
		
		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
			
			line = br.readLine();
			
			while((line = br.readLine()) != null) {
				temp_storage.add(line);
			} // end of while loop
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		// put data into 2D array
		init_arr(temp_storage);
		
		// prompt the user to select which algorithm does he want to run
		try {
			choose_algo(choice);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void init_arr(ArrayList<String> temp_storage) {
		for(int i = 0; i < temp_storage.size(); i++) {
			// split string
			String split[] = temp_storage.get(i).split("\\s+");
			for(int j = 0; j < 4; j++) {
				data[i][j] = Integer.parseInt(split[j]);
			}
		}
	}
	
	public void choose_algo(int choice) throws InterruptedException, IOException {
		
//		int choice = 0;
//		while(choice != 7) {
//			Scanner sc = new Scanner(System.in);
//			sc.nextLine();
//			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
//			System.out.print("Please choose an algorithm.\n\n"
//					+ "1. First-Come-First-Serve (FCFS)\n"
//					+ "2. Shortest Job First (SJF)\n"
//					+ "3. Shortest Remaining Processing Time (SRPT)\n"
//					+ "4. Non-preemptive Priority\n"
//					+ "5. Round-Robin\n"
//					+ "6. Choose a different data.\n"
//					+ "7. Exit program.\n");
//		
//			choice = sc.nextInt();
//			
//			if(choice == 1) {
//				System.out.println("You chose FCFS.");
//				compute(1);
//			} else if(choice == 2) {
//				System.out.println("You chose SJF.");
//				compute(2);
//			} else if(choice == 3) {
//				System.out.println("You chose SRPT.");
//				compute3();
//			} else if(choice == 4) {
//				System.out.println("You chose Priority.");
//				compute(3);
//			} else if(choice == 5) {
//				System.out.println("You chose Round-Robin.");
//				compute2();
//			} else if(choice == 6) {
//				System.out.println("You chose to go to menu.");
//				menu();
//			} else if(choice == 7){
//				System.out.println("Exit program.");
//			}
//
//			// to check, print the values in the 2D arr
////			printarr();
//		}
		compute(1);
		// put data into 2D array
		init_arr(temp_storage);
		compute(2);
		// put data into 2D array
		init_arr(temp_storage);
		compute3();
		// put data into 2D array
		init_arr(temp_storage);
		compute(3);
		// put data into 2D array
		init_arr(temp_storage);
		compute2();
		// put data into 2D array
		init_arr(temp_storage);
		
		if(choice == 1) {
			printall();			
		} else {
			printall2();
		}

	}
	
	public void compute(int column) {
		int i = 0;
		// for FCFS, sort the array in terms of arrival (column 2 - 1)
		// for SJF, sort the array based on burst time column (3 - 1)
		// for priority, sort the array based on priority (column 4 - 1)
		sort_arr(column);
		// after sorting, add the processes into the queue
		add_in_queue();
//		// do gantt chart
		computeGANTT();
//		// compute for the waiting time
		computeWT();
//		// display the waiting time of each process
		if(column == 1) {
			displayWT(fcfs);
		} else if(column == 2) {
			displayWT(sjf);
		} else if(column == 3) {
			displayWT(priority);
		}

//		// compute for the average time
		i = column - 1;
		double AT = computeAT();
		at[i] = AT;
//		System.out.println("Average Waiting Time: " + AT + " ms");
	}
	
	// for computing round robin
	public void compute2() {
		// add each process in the queue
		add_in_queue();
		// but first, copy the value of burst time to remaining time for subtracting later
		copy_burst_time();
		// do gantt chart version 2
		computeGANTT2();
		// check initial waiting time
		computeWT2();
		// display the waiting time of each process
		displayWT(round_robin);
		// compute for the average time
		double AT = computeAT();
		at[4] = AT;
//		System.out.println("Average Waiting Time: " + AT + " ms");
//		// check the values
//		printarr();
	}
	
	// for computing SRPT
	public void compute3() {
		// but first, copy the value of burst time to remaining time for subtracting later
		copy_burst_time();
		// order the job first of the SRPT and ADD IT IN THE QUEUE ALREADY
		orderSRPT();
		// compute gatt chart
		computeGANTT3();
		// compute waiting time
		computeWT3();
		// display WT
		displayWT(srpt);
		// average time
		double AT = computeAT();
		at[3] = AT;
//		System.out.println("Average Waiting Time: " + AT + " ms");
		// checking
//		printarr();
	}
	
	public void sort_arr(int column) {
		// because JAVA 8 -> sorts column two which is the Arrival
		Arrays.sort(data, (a, b) -> Integer.compare(a[column], b[column]));
	}
	
//	public void sort_arrt(int column) {
//		// because JAVA 8 -> sorts column two which is the Arrival
//		Arrays.sort(temp, (a, b) -> Integer.compare(a[column], b[column]));
//	}
//	
	// job order for SRPT
	public void orderSRPT() {
		int sec = 0;
		int currentP = 0;
		int last_in_queue = 0;
		int stack_flag = 0;
		
		while(sec <= data[19][1]) { // stop because all processes have arrived
//			System.out.println(sec);
			
			// get all processes that have the same arrival time
			ArrayList<Integer> currentPs = getP(sec);
			
//			System.out.println(sec + " sec: "); 
			for(int i = 0; i < currentPs.size(); i++) {
				currentP = currentPs.get(i);

				// if queue is empty
				if(queue.isEmpty()) {
					last_in_queue = currentP;
				} else {
					
					// if currentP is lower than the last in queue
					if(data[last_in_queue - 1][6] > data[currentP - 1][6]) {
						t.add(last_in_queue);
						last_in_queue = currentP;
					}
				}

			} // end of for loop
			
			if(data[last_in_queue - 1][6] != 0) {
				data[last_in_queue - 1][6] = data[last_in_queue - 1][6] - 1; // burst time minus 1
			} else {
				if(stack_flag == 0) {
					add_to_min();
					stack_flag = 1;
				}
				last_in_queue = min.peek();
				min.remove();
			}

			if(!queue.contains(last_in_queue)) {
//				System.out.println("last_in_queue: " + last_in_queue);
				queue.add(last_in_queue);
			}
			
			sec++; // increase seconds

		} // end of while loop
		add_remaining_p();
		as_one();
		to_Arr();
//		System.out.println("Current queue size: " + queue.size());
//		printarr();

	}
	
	public void add_remaining_p() {
//		System.out.println(t.toString());
		for(int i = 0; i < data.length; i++) {
			if(!t.contains(data[i][0]) && !queue.contains(data[i][0])) {
				temp.add(data[i][0]);
			}
		}
//		System.out.println(temp.toString());
	}
	
	public void as_one() {
		Iterator<Integer> it = temp.iterator(); // for loop does not copy all
		while(it.hasNext()) {
			t.add(it.next());
		}
//		System.out.println(t.toString());
	}
	
	public void add_to_min() {
		sort_arr(2);
		for(int i = 0; i < data.length; i++) {
			min.add(data[i][0]);
		}
	}
	
	public void to_Arr() {
		sort_arr(0);
//		printarr();
		// put the elements of p(t) in an array
//		System.out.println("t size: " + t.size());
		int[] a = new int[t.size()];
		int[][] arr = new int[t.size()][7];
		int i = 0;
		Iterator<Integer> it = t.iterator();
		
		while(it.hasNext()) {
			a[i] = it.next();
			i++;
		}
		
		for(i = 0; i < a.length; i++) {
			int j = a[i];
			arr[i] = data[j - 1];
		}
		
		sort_to_Arr(arr, 6);
//		System.out.println(queue.toString());		
	}
	
	public void sort_to_Arr(int[][] arr, int column) {
		Arrays.sort(arr, (a, b) -> Integer.compare(a[column], b[column]));
		for(int i = 0; i < arr.length; i++) {
//			System.out.println(Arrays.toString(arr[i]));
			queue.add(arr[i][0]);
		}
	}
	
	// function to use in getting all processes with the same arrival time (for SRPT algorithm)
	public ArrayList<Integer> getP(int sec) {
		ArrayList<Integer> currentPs = new ArrayList<>();
		for(int i = 0; i < data.length; i++) {
			if(data[i][1] == sec) {
				currentPs.add(i+1);
			}
		}
		return currentPs;
	}
	
	public void add_in_queue() {
		for(int i = 0; i < data.length; i++) {
			queue.add(data[i][0]);
			// for checking the values
//			System.out.println(queue.peek());
//			queue.remove();
		}
	}
	
	public void copy_burst_time() {
		// copy the value of burst time into remaining time
		for(int i = 0; i < data.length; i++) {
			data[i][6] = data[i][2];
		}
	}
	
	public void computeGANTT() {
		int currentPT = 0; // current process time
		int i = 0;
		
		while(!queue.isEmpty()) {
			data[i][4] = currentPT + data[i][2];
			currentPT += data[i][2];
			queue.remove();
			i++;
		}
	}
	
	// for computing the gantt chart of round-robin -- quantum is 4
	public void computeGANTT2() {
		int currentPT = 0; // current process time
//		int i = 0;
		
		while(!queue.isEmpty()) {
			int i = queue.peek() - 1;
//			System.out.println("current process: " + i);
			
			if((data[i][6] - 4) >= 0) {
				data[i][6] = data[i][6] - 4;
				currentPT += 4;
				data[i][4] = currentPT;
			} else {
				currentPT += data[i][6];
				data[i][4] = currentPT;
				data[i][6] -= data[i][6];
			}
			
			// if remaining time is not yet equal to 0, add it again to the queue
			if((data[i][6]) > 0) {
				queue.add(data[i][0]);
			}
			
//			System.out.println("remaining time: " + data[i][6]);
			
			queue.remove();
//			i++;
			
		}
		
	}
	
	// for computing the gantt chart of SRPT
	public void computeGANTT3() {
		int currentPT = 0;
		int taken_time = 0; // gets the time taken while preempting a process
		ArrayList<Integer> flagged = new ArrayList<>(); // this is where flagged processes are stored; meaning repeated in the queue
		while(!queue.isEmpty()) {
			int i = queue.peek() - 1;
//			System.out.println(i+1);
			// if the burst time is not equal sa remaining time, it means that while preempting a burst time was taken
			if((data[i][2] != data[i][6]) && !flagged.contains(data[i][0])) {
				taken_time = data[i][2] - data[i][6];
				flagged.add(data[i][0]);
			} else {
				taken_time = data[i][6]; // or data[i][2] ; it does not matter
			}
			
			data[i][4] = currentPT + taken_time;
			currentPT += taken_time;
			
			queue.remove();
		}
		
	}
	
	public void computeWT() {
		for(int i = 0; i < data.length; i++) {
			data[i][5] = data[i][4] - data[i][2]; // waiting time = time the process was completed - burst time
		}
	}
	
	public void computeWT2() {
		for(int i = 0; i < data.length; i++) {
			int modulo = data[i][2] % 4; // burst time / quantum
			int context_switch = data[i][2] / 4; // burst time divided by quantum
			
			// compute first for the initial waiting time
			if(modulo == 0) {
				data[i][5] = (data[i][4] - 4) - ((context_switch - 1) * 4); // process - quantum - (number of context switching * quantum)
			} else {
				data[i][5] = (data[i][4] - modulo) - (context_switch * 4); // process - modulo - (number of context switching * quantum)
			}
			
		}
	}
	
	public void computeWT3() {
		for(int i = 0; i < data.length; i++) {
			data[i][5] = (data[i][4] - data[i][2]) - data[i][1];
		}
	}
	
	public double computeAT() {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			sum += data[i][5]; // waiting time is in the 5th column
		}
		
		double AT = sum / data.length;
		return AT;
	}
	
	public void displayWT(int[] arr) {
		System.out.println();
		for(int i = 0; i < data.length; i++) {
//			System.out.println("P" + (i+1) + " => " + data[i][5] + " ms");
			arr[i] = data[i][5];
		}
	}
	
	public void printarr() {
		System.out.println();
		for(int i = 0; i < data.length; i++) {
			System.out.println(Arrays.toString(data[i]));
		}
	}
	
	public void printall() {
		System.out.println("Process\t\t\tFCFS\t\t\tSJF\t\tSRPT\t\t\tPriority\t\tRound-Robin");
		for(int i = 0; i < 20; i++) {
			System.out.print("P" + (i+1) + "\t\t\t" + fcfs[i] + " ms\t\t\t" + sjf[i] + " ms\t\t"+ srpt[i] + " ms\t\t\t"
					+ priority[i] + " ms\t\t\t"+ round_robin[i] + " ms\n");
		}
		System.out.print("\nAverage WT:\t\t");
		for(int i = 0; i < 5; i++) {
			System.out.print(at[i] + " ms\t\t");
		}
		System.out.println();
	}
	
	public void printall2() {
		System.out.println("Process\t\tFCFS\t\t\tSJF\t\tSRPT\t\tPriority\t\tRound-Robin");
		for(int i = 0; i < 20; i++) {
			System.out.print("P" + (i+1) + "\t\t" + fcfs[i] + " ms\t\t\t" + sjf[i] + " ms\t\t"+ srpt[i] + " ms\t\t"
					+ priority[i] + " ms\t\t\t"+ round_robin[i] + " ms\n");
		}
		System.out.print("\nAverage WT:\t");
		for(int i = 0; i < 5; i++) {
			System.out.print(at[i] + " ms\t\t");
		}
		System.out.println();
	}
	
	public static void main(String[] args) throws InterruptedException, IOException {
		cpu_scheduling_algo run = new cpu_scheduling_algo();
		
		// run menu
		run.menu();
	}
	
}
