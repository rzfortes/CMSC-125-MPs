package lab3;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class MemoryManagement {
	
	int processTimeCopy[] = new int[25];
	int processTime[] = new int[25];
	int processSize[] = new int[25];
	int processStatus[] = new int[25];
	int blockSize[] = new int[10];
	int blockStatus[] = new int[10];
	int blockUser[] = new int[10];
	ArrayList<Integer> waitingList = new ArrayList<>();
	int blocksUsed = 0; // out of ten, how many blocks were occupied?
	int spaceUsed = 0; // out of the overall space of blocks, how much space was used?
	int freeBlocks = 0; // how many blocks are not used?
	int freeSpace = 0; // overall, how many space are not used?
	int unusedBlockSpace[] = new int[10];
	
	// public constructor
	MemoryManagement() {
		// do nothing
	}
	
	public void readJobList(String joblist) {
		String line = "";
		ArrayList<String> temp_storage = new ArrayList<>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(joblist))) {
			
			line = br.readLine(); // read the first line
			
			while((line = br.readLine()) != null) {
//				System.out.println(line);
				temp_storage.add(line);
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		// 
		init_arr(temp_storage);
		
	}
	
	public void readMemoryList(String memorylist) {
		String line = "";
		int i = 0;
		try(BufferedReader br = new BufferedReader(new FileReader(memorylist))) {
			
			line = br.readLine();
			
			while((line = br.readLine()) != null) {
				String[] split = line.split("\\s+");
				blockSize[i] = Integer.valueOf(split[1]);
				blockStatus[i] = 0; // 0 if block is free, 1 if used
				i++;
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void first_fit() throws InterruptedException, IOException {
		
		// allot the first set of processes
		for(int i = 0; i < processSize.length; i++) {
			for(int j = 0; j < blockSize.length; j++) {
				if((processSize[i] <= blockSize[j]) && blockStatus[j] == 0) {
					blockStatus[j] = 1;
					blockUser[j] = (i + 1);
					processStatus[i] = 1;
					unusedBlockSpace[j] = blockSize[j] - processSize[blockUser[j] - 1];
					break;
				}
			} // end of inner loop
			// check if process is allotted in one of the blocks
			if(processStatus[i] == 0) {
				waitingList.add((i + 1));
			}
			
		} // end of outer loop
		// print waitingList
		printWaitingQueue(waitingList);
		
		// repeat until all processes are allotted
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		System.out.println("\nLoop starts here:\n");
		
		while(!waitingList.isEmpty()) {
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			printBlock();
			System.out.println("\nWaiting List: " + waitingList);
			Thread.sleep(1000);
			for(int i = 0; i < blockSize.length; i++) {
				// decrement time
				if(processTimeCopy[blockUser[i] - 1] > 0) {
					processTimeCopy[blockUser[i] - 1]--;
				}
				
				// if time = 0
				if(processTimeCopy[blockUser[i] - 1] == 0 && !waitingList.isEmpty()) {
					processStatus[blockUser[i] - 1] = 2;
					blockStatus[i] = 0;
					if(gotAFittedJob(i)) {
						blockStatus[i] = 1;
					}
				}
				
			}
			if(blocksUsed == 0) {
				break;
			}
			System.out.println();
		}
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		printBlock();
		
	}
	
	public void best_fit() throws InterruptedException, IOException {
		// sort the memory block first - ascending order
		int[] temp_mem = sort_arr();
		
//		System.out.println();
//		
//		for(int i = 0; i < temp_mem.length; i++) {
//			System.out.println(getIndex(temp_mem[i]));
//		}
		
		// allot the first processes
		for(int i = 0; i < processSize.length; i++) {
			for(int j = 0; j < temp_mem.length; j++) {
				int index = getIndex(temp_mem[j]);
				if((processSize[i] <= blockSize[index]) && blockStatus[index] == 0) {
					blockStatus[index] = 1;
					blockUser[index] = (i + 1);
					processStatus[i] = 1;
					unusedBlockSpace[index] = blockSize[index] - processSize[blockUser[index] - 1];
					break;
				}
			} // end of inner loop
			// check if process is allotted in one of the blocks
			if(processStatus[i] == 0) {
				waitingList.add((i + 1));
			}
		} // end of outer loop
		// print waiting list
		printWaitingQueue(waitingList);
		
		// repeat until all processes are allotted
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		System.out.println("\nLoop starts here:\n");
		
		
		// guide
		while(!waitingList.isEmpty()) {
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			printBlock();
			System.out.println("\nWaiting List: " + waitingList);
			Thread.sleep(1000);
			for(int i = 0; i < temp_mem.length; i++) {
				int index = getIndex(temp_mem[i]);
				// decrement time
				if(processTimeCopy[blockUser[index] - 1] > 0) {
					processTimeCopy[blockUser[index] - 1]--;
				}
				
				// if time = 0
				if(processTimeCopy[blockUser[index] - 1] == 0 && !waitingList.isEmpty()) {
					processStatus[blockUser[index] - 1] = 2;
					blockStatus[index] = 0;
					if(gotAFittedJob(index)) {
						blockStatus[index] = 1;
					}
				}
				
			}
			if(blocksUsed == 0) {
				break;
			}
			System.out.println();
		}
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		printBlock();
		
	}
	
	public void worst_fit() throws InterruptedException, IOException {
		
		// for computing waiting time
		int currentPT = 0; // current process time
		
		// sort mem array into descending order
		Integer[] temp_memo = Arrays.stream(blockSize).boxed().toArray(Integer[]::new);
		
		Arrays.sort(temp_memo, Collections.reverseOrder());
		
		int[] temp_mem = Arrays.stream(temp_memo).mapToInt(Integer::intValue).toArray();
		
//		System.out.println("\nTemp mem:\n");
//		for(int i = 0; i < temp_mem.length; i++) {
//			System.out.println((i + 1) + "\t" + temp_mem[i]);
//		}
		
		// allot the first processes
		for(int i = 0; i < processSize.length; i++) {
			for(int j = 0; j < temp_mem.length; j++) {
				int index = getIndex(temp_mem[j]);
				if((processSize[i] <= blockSize[index]) && blockStatus[index] == 0) {
					blockStatus[index] = 1;
					blockUser[index] = (i + 1);
					processStatus[i] = 1;
					unusedBlockSpace[index] = blockSize[index] - processSize[blockUser[index] - 1];
					break;
				}
			} // end of inner loop
			// check if process is allotted in one of the blocks
			if(processStatus[i] == 0) {
				waitingList.add((i + 1));
			}
		} // end of outer loop
		// print waiting list
		printWaitingQueue(waitingList);
		
		// repeat until all processes are allotted
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		System.out.println("\nLoop starts here:\n");
		
		// guide
		while(!waitingList.isEmpty()) {
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
			new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
			printBlock();
			System.out.println("\nWaiting List: " + waitingList);
			Thread.sleep(1000);
			for(int i = 0; i < temp_mem.length; i++) {
				int index = getIndex(temp_mem[i]);
				// decrement time
				if(((blockUser[index] - 1) >= 0) && processTimeCopy[blockUser[index] - 1] > 0) {
					processTimeCopy[blockUser[index] - 1]--;
				}
				
				// if time = 0
				if(((blockUser[index] - 1) >= 0) && processTimeCopy[blockUser[index] - 1] == 0 && !waitingList.isEmpty()) {
					processStatus[blockUser[index] - 1] = 2;
					blockStatus[index] = 0;
					if(gotAFittedJob(index)) {
						blockStatus[index] = 1;
					}
				}
				
			}
			if(blocksUsed == 0) {
				break;
			}
			System.out.println();
		}
		new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
		printBlock();
		
	}
	
	//------------------------------------------------------ OTHER FUNCTIONS ------------------------------------------------------------------//
	
	public int[] sort_arr() {
		int[] temp_mem = new int[10];
		
		temp_mem = blockSize.clone();
		Arrays.sort(temp_mem);
		
		// print the elements of temp_mem
		System.out.println("\nTemp Mem:\n");
		for(int i = 0; i < temp_mem.length; i++) {
			System.out.println((i + 1) + "\t" + temp_mem[i]);
		}
		return temp_mem;
	}
	
	public int getIndex(int size) {
		int index = 0;
		for(int i = 0; i < blockSize.length; i++) {
			if(blockSize[i] == size) {
				index = i;
			}
		}
		return index;
	}
	
	public void init_arr(ArrayList<String> temp_storage) {
		for(int i = 0; i < temp_storage.size(); i++) {
			// split string
			String[] split = temp_storage.get(i).split("\\s+");
			processTime[i] = Integer.valueOf(split[1]);
			processSize[i] = Integer.valueOf(split[2]);
			processStatus[i] = 0; // process is 0 if not allotted yet. otherwise, 1
		}
		processTimeCopy = processTime.clone();
	}
	
	public boolean gotAFittedJob(int i) {
		int jobNum = 0;
		Iterator<Integer> it = waitingList.iterator();
		while(it.hasNext()) {
			jobNum = it.next();
			if(processSize[jobNum - 1] <= blockSize[i]) {
				blockUser[i] = jobNum;
				unusedBlockSpace[i] = blockSize[i] - processSize[blockUser[i] - 1];
				it.remove();
				return true;
			}
		}
		return false;
	}
	
	public int getusedBlocks() {
		int ctr = 0;
		for(int i = 0; i < blockStatus.length; i++) {
			if(blockStatus[i] == 1) {
				ctr++;
			}
		}
		return ctr;
	}
	
	public float computeThroughput() {
		float ctr = 0;
		float sum = 0;
		for(int i = 0; i < processStatus.length; i++) {
//			System.out.println(processStatus[i]);
			if(processStatus[i] == 2) {
				ctr++;
				sum += processTime[i];
			}
		}
		System.out.print(ctr + "/" + sum + " = ");
		return (float) ctr / sum;
	}
	
	public float getWaitingTime() {
		float ave = 0;
		int jobNum = 0;
		
		Iterator<Integer> it = waitingList.iterator();
		while(it.hasNext()) {
			jobNum = it.next();
			ave += processTime[jobNum - 1];
		}
		
		return ave;
	}
	
	public float getTotalMemBlock() {
		float sum = 0;
		
		for(int i = 0; i < blockSize.length; i++) {
			sum += blockSize[i];
		}
		
		return sum;
	}
	
	public float getTotalUnusedBs() {
		float sum = 0;
		
		for(int i = 0; i < unusedBlockSpace.length; i++) {
			sum += unusedBlockSpace[i];
		}
		
		return sum;
		
	}
	
	public void print() {
		System.out.println("Processes:\n");
		for(int i = 0; i < processSize.length; i++) {
			System.out.println((i + 1) + "\t" + processTime[i] + "\t" + processSize[i] + "\t" + processStatus[i]);
		}
		
		System.out.println("\nBlocks:\n");
		for(int i = 0; i < blockSize.length; i++) {
			System.out.println((i + 1) + "\t" + blockSize[i] + "\t" + blockUser[i]);
		}
	}
	
	public void printBlock() {
		float percentage = 0;
		float aveInternalFrag = 0;
		System.out.println("\nMemory Block\tBlock Size\tJob Stream\tSize\t\tTime\t\tUnused Space");
		for (int i = 0; i < blockSize.length; i++) {
			if((blockUser[i] - 1) < 0) {
				unusedBlockSpace[i] = blockSize[i] - 0;
				System.out.print((i + 1) + "\t\t" + blockSize[i] + "\t\t" + blockUser[i] + "\t\t" 
						+ "N/A" + "\t\t" + "N/A" + "\t\t" + unusedBlockSpace[i] + " (");
			} else {
				System.out.print((i + 1) + "\t\t" + blockSize[i] + "\t\t" + blockUser[i] + "\t\t" 
						+ processSize[blockUser[i] - 1] + "\t\t" +processTimeCopy[blockUser[i] - 1] + "\t\t" + unusedBlockSpace[i] + " (");
			}
			percentage = (unusedBlockSpace[i] * 100.0f) / blockSize[i];
			System.out.printf("%.2f", percentage);
			System.out.print(" %)");
			System.out.println();
		}
		blocksUsed = getusedBlocks();
		System.out.println("\nBlocks used: " + blocksUsed);
		System.out.print("\nThroughput: ");
		System.out.print(computeThroughput());
		System.out.println();
		System.out.println("Waiting queue in length: " + waitingList.size());
		System.out.print("Waiting time in queue: ");
		System.out.printf("%.2f", getWaitingTime());
		System.out.print(" ms");
		aveInternalFrag = (getTotalUnusedBs() * 100.0f) / getTotalMemBlock();
		System.out.print("\nInternal fragmentation: ");
		System.out.printf("%.2f", aveInternalFrag);
		System.out.print(" %");
		System.out.println();
	}
	
	public void printWaitingQueue(ArrayList<Integer> waitingList) {
		System.out.println(waitingList);
	}
	
	public static void main(String[] args) throws InterruptedException, IOException {
		MemoryManagement run = new MemoryManagement();
		run.readJobList("joblist.txt");
		run.readMemoryList("memorylist.txt");
//		run.first_fit();
//		run.best_fit();
		run.worst_fit();
//		run.print();
		
	}

}
