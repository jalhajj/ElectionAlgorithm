import java.util.ArrayList;

public class Population {

	private Data theData;
	private boolean isCandidate;
	private double fitness;
	private int politicalParty;

	public Population(int n) {
		theData = new Data(n);
		isCandidate = false;
		fitness = 0.0;
		politicalParty = -1;
	}

	public Data getTheData() {
		return theData;
	}

	public void setTheData(Data theData) {
		this.theData = theData;
	}

	public boolean isCandidate() {
		return isCandidate;
	}

	public void setCandidate(boolean isCandidate) {
		this.isCandidate = isCandidate;
	}

	public double getFitness() {
		return fitness;
	}

	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	public int getPoliticalParty() {
		return politicalParty;
	}

	public void setPoliticalParty(int politicalParty) {
		this.politicalParty = politicalParty;
	}

}
