import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.LinkedList;


public class Escalonadores {
	static LinkedList<Process> pList = new LinkedList<Process>();
	static LinkedList<Process> robinList = new LinkedList<Process>();
	static PriorityQueue<Process> queue = new PriorityQueue<Process>();

	static LinkedList<Process> robinQueue = new LinkedList<Process>();
	
	static int timeslice, n_process;
	
	static class Process implements Comparable<Process>{
		int arrival=0, execution = 0, priority = 0, id;
		float waiting=-1;
		public Process(int id, int a, int e, int p){
			arrival = a;
			execution = e;
			priority = p;
			this.id = id;
		}
		
		public int compareTo(Process p) {
			if(execution < p.execution)
				return -1;
			if(execution == p.execution && priority < p.priority )
				return -1;
			if(priority == p.priority && execution == p.execution)
				return 0;
			return 1;
		}
		
		public Process clone(){
			return new Process(id, arrival, execution, priority);
		}
	}
	
	static boolean readFile(String path){
		BufferedReader br;
		try{
	    	FileReader arq = new FileReader(path);
	    	br = new BufferedReader(arq);
	    	n_process = Integer.parseInt(br.readLine());
	    	timeslice = Integer.parseInt(br.readLine());
	    	String line;
	    	int count = 1;
	    	for(int i=0; i<n_process; i++){
	    		line = br.readLine();
	    		String aux[] = line.split(" ");
	    		int a = Integer.parseInt(aux[0]);
	    		int e = Integer.parseInt(aux[1]);
	    		int p = Integer.parseInt(aux[2]);
	    		Process pr = new Process(count,a,e,p);
	    		pList.add(pr);
	    		count++;
	    	}
	    	br.close();
	    	return true;
	    	
		}catch(IOException e){
			System.out.println("Arquivo inválido!\n");
			return false;
		}
	}
	
	static void getProcessToQueue(int pc, LinkedList<Process> pList){
		if(pList.isEmpty()) return;
		while(!pList.isEmpty() && pList.peek().arrival <= pc){
			Process p = pList.poll();
			queue.add(p.clone());
			robinList.add(p.clone());
		}
	}
	
	static void getProcessToQueuePP(int pc, LinkedList<Process> pList){
		if(pList.isEmpty()) return;
		while(!pList.isEmpty() && pList.peek().arrival <= pc){
			Process p = pList.poll();
			pushProcessToQueue(p);
			
			
		}
	}
	
	static void pushProcessToQueue(Process p){
		if(robinQueue.isEmpty()) robinQueue.add(p.clone());
		else
			for(int i=0; i<robinQueue.size(); i++){
				Process q = robinQueue.get(i);
				if(p.priority<q.priority){
					robinQueue.add(i, p.clone());
					break;
				}else if(i==(robinQueue.size()-1)){
					robinQueue.add(p.clone());
					break;
				}
			}
	}
	
	static String SJF(){
		StringBuffer sb = new StringBuffer();
		int pc = 0;
		float turnaround=0, waiting=0;
		queue.clear();
		LinkedList<Process> list = new LinkedList<Process>(pList);
		while(!list.isEmpty()||!queue.isEmpty()){
			getProcessToQueue(pc, list);
			if(queue.isEmpty()){
				sb.append("-");
				pc++;
			}else{
				sb.append("TC");
				pc+=2;
				Process p = queue.poll();
				waiting+= pc-p.arrival;
				while(p.execution!=0){
					sb.append(p.id);
					p.execution--;
					pc++;
				}
				turnaround+= pc-p.arrival;
			}
		}
		sb.append("\nTempo médio de resposta e espera: ").append(waiting/n_process);
		sb.append("\nTempo médio de turnaround: ").append(turnaround/n_process);
		return sb.toString();
	}
	
	static String RoundRobin(){
		StringBuffer sb = new StringBuffer();
		int pc = 0, ts, start;
		float turnaround=0, waiting=0, response =0;
		robinList.clear();
		Process curr = null;
		LinkedList<Process> list = new LinkedList<Process>(pList);
		while(!list.isEmpty()||!robinList.isEmpty()){
			getProcessToQueue(pc, list);
			if(robinList.isEmpty()){
				sb.append("-");
				pc++;
			}else{
				Process p = robinList.poll();
				ts = timeslice;
				if(!p.equals(curr)){
					sb.append("TC");
					pc+=2;
					curr = p;
					if(p.waiting==-1){
						p.waiting = pc-p.arrival;
						response += p.waiting;
						waiting += p.waiting;
					}else{
						waiting += pc-p.waiting;
					}
				}
				
				while(ts>0 && p.execution>0){
					sb.append(p.id);
					p.execution--;
					ts--;
					pc++;
				}
				
				if(p.execution!=0){
					p.waiting = pc;
					robinList.add(p);
				}else{
					turnaround+=pc-p.arrival;
				}
			}
		}
		sb.append("\nTempo médio de espera: ").append(waiting/n_process);
		sb.append("\nTempo médio de resposta: ").append(response/n_process);
		sb.append("\nTempo médio de turnaround: ").append(turnaround/n_process);
		return sb.toString();
	}
	
	static String RoundRobinPP(){
		StringBuffer sb = new StringBuffer();
		int pc = 0, ts;
		float turnaround=0, waiting=0, response =0;
		robinQueue.clear();
		LinkedList<Process> list = new LinkedList<Process>(pList);
		Process p = null, curr=null;
		while(!list.isEmpty()||!robinQueue.isEmpty()){
			getProcessToQueuePP(pc, list);
			if(robinQueue.isEmpty()){
				sb.append("-");
				pc++;
			}else{
				p = robinQueue.poll();
				if(curr == null || p.id!=curr.id){
					sb.append("TC");
					pc+=2;
					curr = p;
					if(p.waiting==-1){
						p.waiting = pc-p.arrival;
						response += p.waiting;
						waiting += p.waiting;
					}else{
						waiting += pc-p.waiting;
					}
				}
				ts = timeslice;
				while(ts>0 && p.execution>0){
					sb.append(p.id);
					p.execution--;
					ts--;
					pc++;
				}
				if(p.execution!=0){
					p.waiting = pc;
					pushProcessToQueue(p);
				}else{
					turnaround+=pc-p.arrival;
				}
			}
		}
		sb.append("\nTempo médio de espera: ").append(waiting/n_process);
		sb.append("\nTempo médio de resposta: ").append(response/n_process);
		sb.append("\nTempo médio de turnaround: ").append(turnaround/n_process);
		return sb.toString();
	}
	
	public static void main(String[] args) {
		readFile("trab-so1-teste2.txt");
		System.out.println(SJF());
		System.out.println(RoundRobin());
		System.out.println(RoundRobinPP());
	}

}
