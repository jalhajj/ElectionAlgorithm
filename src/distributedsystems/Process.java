package distributedsystems;

public class Process

{
	private boolean active;
	private boolean electionFlag;
	private boolean isCoordinator;
	private int id;
	private int coordinatorID;

	public Process(int id, int coordId) {
		this.id = id;
		active = true;
		electionFlag = false;
		isCoordinator = false;
		coordinatorID = coordId;

	}

	public Message sendMessage(Process destination, Message messageType) {
		if (destination.active) 
		{
			if(messageType == Message.ping)
			{
				System.out.println("ping received");
				return Message.pong;
			}
			else if (messageType == Message.electionMsg)
			{
				System.out.println("Election msg received");
				return Message.okMsg;
			}
			else 
			{
				System.out.println("ok");
				return Message.okMsg;
			}
		} 
		
		//maybe we can change it to timeout later on
		else 
		{
			return null;
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isElectionFlag() {
		return electionFlag;
	}

	public void setElectionFlag(boolean electionFlag) {
		this.electionFlag = electionFlag;
	}

	public boolean isCoordinator() {
		return isCoordinator;
	}

	public void setCoordinator(boolean isCoordinator) {
		this.isCoordinator = isCoordinator;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getCoordinatorID() {
		return coordinatorID;
	}

	public void setCoordinatorID(int coordinatorID) {
		this.coordinatorID = coordinatorID;
	}

}
