package distributedsystems;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Ringmain 
{
	
	static List<Process> processes = new ArrayList<Process>();
	
	public static void main(String[] args) throws InterruptedException 
	
	{
		Scanner sc = new Scanner(System.in);
		int choice =0;
		do {
			
			System.out.println(".........");
            System.out.println("1 Initialize ");
            
            
            System.out.println("2.kill process");
            System.out.println("3.send message");
        //    System.out.println("4.check coordinator");  todo waiting to see what happens if a processor is added with higher id
            System.out.println("99.Exit");
			
            choice = sc.nextInt();
            
            switch(choice)
            {
	            case 1:
	            {
	            	System.out.println("Enter the Number of processes");
	            	int numberOfProcesses = sc.nextInt();
	            	Initialize(numberOfProcesses);
	            	break;
	            }
	            case 2:
	            {
	            	System.out.println("Enter the Process you want to kill");
	            	int processNumber = sc.nextInt();
	            	Killprocess(processNumber);
	            	break;
	            }
	            case 3:
	            {
	            	System.out.println("Enter the Process you want to send the message from");
	            	int from = sc.nextInt();
	            	System.out.println("Enter the Process you want to send the message to");
	            	int to = sc.nextInt();
	            	
	            	ping(from,to);
	            	break;
	            }
	            case 99:
	            {
	            	break;
	            }
	            default:
	            	System.out.println("Please enter a valid number");
            }
			
		}while(choice !=99);
		
		
//		System.out.println("Enter the Number of processes");
//   	int numberOfProcesses = sc.nextInt();
//  	Initialize(numberOfProcesses);
//  	
//		while(true)
//		{
//			
//			Thread.sleep(5000);
//		            	start();
//		}
//		        
		
	}
	
	public static void electionMulti()
	{
		
	}
	private static void ping(int from, int to) {
		
		Process processFrom = processes.get(from-1);
		Process processTo = processes.get(to-1);



		if(processFrom.isActive())
		{
			System.out.println("Sending a ping message From process #"+from+" to process #"+to);
			Message msgType = processFrom.sendMessage(processTo, Message.ping);
			if(msgType !=null)
			{
				System.out.println("process # "+to+" replied with "+msgType);
			}
			else
			{
				if(processTo.isCoordinator())
				{
					System.out.println("process "+to+" is dead, process"+from+" initiated an election");
					InitiateElection(processFrom);
				}

				else
				{
					System.out.println("process "+to+" is dead");
				}
			}
		}
		else
		{
			System.out.println("Process is dead, can't send message");
		}
	}
	private static void InitiateElection(Process from) 
	{
		Message reply;
		int coordinatorId = from.getId();
		int tmpIndex;
		// in the algo we assume that the processes know who are the processes that have higher priorities
		for(int i=from.getId()+1;i<processes.size();i++ )
		{
			reply = from.sendMessage(processes.get(i), Message.electionMsg);			
			if(reply != null)
			{
				tmpIndex = i+1;
				System.out.println("Process # "+tmpIndex+" replied with "+ reply);
				coordinatorId = i;
			}
		}
		tmpIndex = coordinatorId+1;
		processes.get(coordinatorId).setCoordinator(true);	
		System.out.println("Process # "+tmpIndex+"won the election, he's the coordinator now");
		
		// setting the coordinator Id for the others 
		for(int i=0;i<processes.size();i++)
		{
			processes.get(i).setCoordinatorID(coordinatorId);
		}
	}
	private static void Killprocess(int processNumber) {

		processes.get(processNumber-1).setActive(false);
	}

	private static void Initialize(int numberOfProcesses) 
	
	{	
		
		List<Object> mylist = new ArrayList<Object>();
		
		
		
		for(int i=0;i<numberOfProcesses;i++)
		{
			processes.add(new Process(i,numberOfProcesses-1));
		}
		Process coordinator = processes.get(numberOfProcesses-1);
		coordinator.setCoordinator(true);
		

		
			
		
		
	}
	
	public static void start()
	{

			
			        		Random r = new Random();
			        		int from = r.nextInt(processes.size());
			        	
			        		int to = r.nextInt(processes.size());
			        	
			        			
			            	ping (from+1,to+1);
			       
			
		
		
		
		
	}
	
}
