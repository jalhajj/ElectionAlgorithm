import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EA {

	/*
	 * Election Algorithm (GA)
	 *
	 *
	 * 
	 * 
	 */

	private int n; // problem size (in bytes)
	private int np; // population size
	private static ArrayList<Population> population; // Array List containing all population

// hashmap containing the different politicalParties. the Key is the index of the leader in population ArrayList. the value is the array list of the supporters
	private static Map<Integer, ArrayList<Population>> partiesMap = new HashMap<Integer, ArrayList<Population>>();

	private int maxIt; // max number of iterations (default 1000)
	private double pbcan; // percentage of the population that are candidates(default 0.04)
	private Objective f; // objective function
	// the avg fitness of the population( every person that has > fitness than the
	// avg, will be eligible to be a candidate)
	private double fitnessAvg = 0.0;

	// Constructor (argument names coincide with corresponding attribute names)
	public EA(int n, int np, int maxIt, double pbcant, Objective f) {
		try {
			// initial checks
			if (n < 0)
				throw new Exception("EA: problem size cannot be negative");
			this.n = n;
			if (np <= 1)
				throw new Exception("EA: population size cannot be equal to " + np);
			this.np = np;
			if (maxIt <= 0)
				throw new Exception("EA: the specified maximal number of iterations is " + maxIt);
			this.maxIt = maxIt;
			if (pbcan < 0.0 || pbcan > 1.0)
				throw new Exception("EA: the probability for mutation needs to be a real number in [0.0,1.0]");
			this.pbcan = pbcan;

			// objective function
			if (f == null)
				throw new Exception("EA: reference to Objective function is null");
			this.f = f;
			double sum = 0.0;
			double fitnessValue = 0.0;

			// generating population 0
			this.population = new ArrayList<Population>(this.np);
			Population tmpPopulation;

			for (int i = 0; i < this.np; i++) {
				tmpPopulation = new Population(this.n);
				fitnessValue = this.f.value(tmpPopulation.getTheData());
				tmpPopulation.setFitness(fitnessValue);
				population.add(tmpPopulation);
				sum += fitnessValue; // we calculate the sum to calculate the avg of fitness later on.
			}

			// candidates having a fitness higher than this avg are eligible to lead a
			// political party
			fitnessAvg = sum / population.size();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// Constructor requiring less arguments (default values are used for the others)
	public EA(int n, int np, int maxIt, Objective f) {
		this(n, np, maxIt, 0.04, f);
	}

	// Constructor requiring less arguments (default values are used for the others)
	public EA(int n, int np, Objective f) {
		this(n, np, 1000, f);
	}

	// Constructor requiring less arguments (default values are used for the others)
	public EA(int n, Objective f) {
		this(n, n, f);
	}

	// Get the best element of the current population
	// (Data object for which the objective gives the largest value)
	public Data getBestElement() {
		int bestIndex = -1;
		double bestValue = Double.MIN_VALUE;
		for (int i = 0; i < this.np; i++) {
			double v = this.population.get(i).getFitness();
			if (v > bestValue) {
				bestIndex = i;
				bestValue = v;
			}
		}
		return this.population.get(bestIndex).getTheData();
	}

	// Select the elements of the population for reproduction
	// (works correctly only if function values are all nonnegative)
	public int select() {
		Random r = new Random();
		double bound = r.nextDouble();
		double sum = 0.0;
		for (int i = 0; i < this.np; i++)
			sum = sum + this.population.get(i).getFitness();

		int selIndex = 0;
		double psum = 0.0;
		while (selIndex < this.np - 1 && psum / sum < bound) {
			psum = psum + this.population.get(selIndex).getFitness();
			selIndex++;
		}

		return selIndex;
	}

	// Verifies whether all element of the current population have the same fitness
	public boolean isStable() {
		boolean stable = true;
		double ref = this.population.get(0).getFitness();
		for (int i = 1; stable && i < this.np; i++)
			if (ref != this.population.get(i).getFitness())
				stable = false;
		return stable;
	}

	/*
	 * the optimization algo has 2 steps 
	 * 1-  advertisement, where the leader
	 * of the party advertise his bits to his supporters
	 *  2-
	 * coalision, where the weak party join a stronger one
	 */
	public int optimize(int numberOfThread) {

		int it = 0;
		try {
			while (it < this.maxIt) {
				if (numberOfThread == 1)
				{
					advertise();
				}
				else
				{
					
					multiThreadAdvertise(numberOfThread);
				}

				if (partiesMap.size() > 1) {
					coalision();
				}

				it++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return it;
	}


	private void coalision() throws NoSuchMethodException {
		Iterator<Entry<Integer, ArrayList<Population>>> it = partiesMap.entrySet().iterator();
		double newFitness = 0.0;
		double oldFitness = 0.0;
		ArrayList<Integer> listKeys = new ArrayList<Integer>();
		Population leader;
		List<Population> removedSupporters = new ArrayList<Population>();
		Population old;
		double fitness = Byte.MAX_VALUE;
		int index = -1;
		while (it.hasNext()) {
			Map.Entry<Integer, ArrayList<Population>> mapEntry = (Entry<Integer, ArrayList<Population>>) it.next();

			listKeys.add(mapEntry.getKey());

			if (mapEntry.getValue().get(0).getFitness() < fitness) {
				fitness = mapEntry.getValue().get(0).getFitness();
				index = mapEntry.getKey();
			}
		}

		removedSupporters = partiesMap.get(index);
		Random r = new Random();
		int arrIndex = r.nextInt(listKeys.size() - 1);

		// to avoid selecting the index of the candidate will remove later on
		while (arrIndex == index) {
			arrIndex = r.nextInt(listKeys.size() - 1);
		}

		partiesMap.get(listKeys.get(arrIndex)).addAll(removedSupporters);
		partiesMap.remove(index);
	}

	private void advertise() throws NoSuchMethodException, InterruptedException {

		Random r = new Random();
		int subSetIndex;
		for (Entry<Integer, ArrayList<Population>> mapEntry : partiesMap.entrySet()) {

			subSetIndex = r.nextInt(n - 1);
			
			advertisingForPopulation(mapEntry.getKey() ,subSetIndex);
		}
	}
	
	private void multiThreadAdvertise(int numberOfThreads)
	
	{
		
		// generate a thread pool
		
		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
		ArrayList<Callable<Integer>> tasks = new ArrayList<>();
		
		Random r = new Random();
		final int subSetIndex = r.nextInt(n - 1);
		for (Entry<Integer, ArrayList<Population>> mapEntry : partiesMap.entrySet()) {
			tasks.add(() -> {
				return advertisingForPopulation(mapEntry.getKey(), subSetIndex);
			});
		}
		
		try {
			executor.invokeAll(tasks);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			tasks.clear();
		}
		
	}

	private int advertisingForPopulation(int candidateIndex,int subSetIndex) throws NoSuchMethodException, InterruptedException {
		
		ArrayList<Population> populationArr = partiesMap.get(candidateIndex);
		
		
		Population candidate;
		
		for (Population supporter : populationArr) {

			candidate = population.get(candidateIndex);
			// getting a random subset of the candidate data
			byte candidateByte = candidate.getTheData().getByte(subSetIndex);
			
			ArrayList<Object> tmpX = new ArrayList<Object>();
			tmpX.add(candidateByte);
			//creating a data from the bytes of the subset, to calculate the fitness later on 
			Data canData = new Data(tmpX);

			//getting a random subset of the supporter data
			byte supporterByte = supporter.getTheData().getByte(subSetIndex);
			ArrayList<Object> tmpY = new ArrayList<Object>();
			tmpY.add(supporterByte);
			Data supData = new Data(tmpY);

			// check if the fitness  of the supporter supset is bigger than the candidate 
			if (f.value(canData) >= f.value(supData)) {
				continue;
			}

			// replacing the subset with the new data (the data of the candidate)
			candidate.getTheData().replaceBits(supporterByte, subSetIndex);
			//updating the value of the fitness
			candidate.setFitness(f.value(candidate.getTheData()));
			
			//updating the party map
			partiesMap.get(candidateIndex).set(0, candidate);
			//updating the array
			population.set(candidateIndex, candidate);

		}
		
		return 0;
		
	}

	// toString
	public String toString() {
		String print = "GA\n[\n  ";
		print = print + "n = " + this.n + ", np = " + this.np + ", maxIt = " + this.maxIt + ", pbcan = " + this.pbcan
				+ ";\n  ";
		print = print + this.f.toString();
		print = print + "\n]\n";
		return print;
	}


	private void selectCandidates() {
		Random r = new Random();
		int index = 0;
		int candidates = 0;
		Population tmpPopulation;

		// as per the paper, the candidates number must be between 5-10 % of the
		// population
		for (int i = 0; i < np && candidates < np * 0.05; i++) {
			index = r.nextInt(np);
			tmpPopulation = population.get(i);
			// we check if the candidate has a fitness above the avg
			if (tmpPopulation.getFitness() < fitnessAvg) {
				continue;
			} else {
				tmpPopulation.setCandidate(true);
				tmpPopulation.setPoliticalParty(i);
				// the key of the map is the candidate's index in the population
				partiesMap.put(i, new ArrayList<Population>());

				// setting the leader of the campaign, his id in the list of the party is0
				partiesMap.get(i).add(tmpPopulation);
				candidates++;
			}
		}

		System.out.println("Number of candidates is "+partiesMap.size());
	}

	// putting the voters who are close to candidates in terms of fitness
	// can be implemented multi threading and even in sequential can be improved
	private void initializeParties() {
		Iterator it = partiesMap.entrySet().iterator();
		double absDiff;
		int index;
		double tmpFitness = 0.0;
		Population tmpVoter;
		index = 0; // the index of the candidate in the map
		for (int i = 0; i < np; i++) {
			absDiff = Double.MAX_VALUE;

			tmpVoter = population.get(i);
			if (tmpVoter.isCandidate()) {
				continue;
			}
			tmpFitness = tmpVoter.getFitness();

			while (it.hasNext()) {
				Map.Entry<Integer, ArrayList<Population>> mapEntry = (Entry<Integer, ArrayList<Population>>) it.next();
				if (mapEntry.getValue().get(0).getFitness() - tmpFitness < absDiff) {
					absDiff = Math.abs(mapEntry.getValue().get(0).getFitness() - tmpFitness);
					index = (int) mapEntry.getKey();

				}
			}
			partiesMap.get(index).add(population.get(i));
		}

	}
	
	// main -- example of use
	public static void main(String[] args) throws NoSuchMethodException {
		Objective f = new Objective(0);
		EA ea = new EA(10, 100, 25, f);

		// we pick randomelly members of the population that are eligeable(> than the avg
		// fitness) to lead a party
		ea.selectCandidates();
		// we assign voters to a party
		ea.initializeParties();
		
		// if optimize > 1, then the program will run the multithreading function
		System.out.println("Number of iterations: " + ea.optimize(1));
		System.out.println("Solution: " + ea.getBestElement());
		System.out.println("and its fitness: " + f.value(ea.getBestElement()));
	}


}
