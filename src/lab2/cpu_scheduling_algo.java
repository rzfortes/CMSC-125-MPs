package lab2;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.Scanner;

public class cpu_scheduling_algo {
	
	private int[][] data = new int[20][7];
	private Queue<Integer> queue = new ArrayDeque<>();
	
	// public constructor
	cpu_scheduling_algo() {
		// do nothing
	}
	
	public void menu() {
		// make a menu
		System.out.println("You are about to see how CPU Scheduling Algorithms are implemented."
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
		readFile(filename);
		
	}
	
	public void readFile(String filename) {
		ArrayList<String> temp_storage = new ArrayList<>();
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
			choose_algo();
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
	
	public void choose_algo() throws InterruptedException, IOException {
		
		int choice = 0;
		while(choice != 7) {
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			System.out.print("Please choose an algorithm.\n\n"
					+ "1. First-Come-First-Serve (FCFS)\n"
					+ "2. Shortest Job First (SJF)\n"
					+ "3. Shortest Remaining Processing Time (SRPT)\n"
					+ "4. Non-preemptive Priority\n"
					+ "5. Round-Robin\n"
					+ "6. Choose a different data.\n"
					+ "7. Exit program.\n");
		
			choice = sc.nextInt();
			
			if(choice == 1) {
				System.out.println("You chose FCFS.");
				compute(1);
			} else if(choice == 2) {
				System.out.println("You chose SJF.");
				compute(2);
			} else if(choice == 3) {
				System.out.println("You chose SRPT.");
			} else if(choice == 4) {
				System.out.println("You chose Priority.");
				compute(3);
			} else if(choice == 5) {
				System.out.println("You chose Round-Robin.");
				compute2();
			} else if(choice == 6) {
				System.out.println("You chose to go to menu.");
				menu();
			} else if(choice == 7){
				System.out.println("Exit program.");
			}

			// to check, print the values in the 2D arr
//			printarr();
		}
	}
	
	public void compute(int column) {
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
		displayWT();
//		// compute for the average time
		double AT = computeAT();
		System.out.println("Average Waiting Time: " + AT);
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
		displayWT();
		// compute for the average time
		double AT = computeAT();
		System.out.println("Average Waiting Time: " + AT);
//		// check the values
//		printarr();
	}
	
	public void sort_arr(int column) {
		// because JAVA 8 -> sorts column two which is the Arrival
		Arrays.sort(data, (a, b) -> Integer.compare(a[column], b[column]));
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
	
	public double computeAT() {
		double sum = 0;
		for(int i = 0; i < data.length; i++) {
			sum += data[i][5]; // waiting time is in the 5th column
		}
		
		double AT = sum / data.length;
		return AT;
	}
	
	public void displayWT() {
		System.out.println();
		for(int i = 0; i < data.length; i++) {
			System.out.println("P" + (i+1) + " => " + data[i][5]);
		}
	}
	
	public void printarr() {
		System.out.println();
		for(int i = 0; i < data.length; i++) {
			System.out.println(Arrays.toString(data[i]));
		}
	}
	
	public static void main(String[] args) throws InterruptedException, IOException {
		cpu_scheduling_algo run = new cpu_scheduling_algo();
		
		// run menu
		run.menu();
	}
	
}
