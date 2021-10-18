package Score;

import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;

public class CompositeStrategy implements IScoreStrategy, ICallback{

	// the ArrayList of Strategies
	private final ArrayList<IScoreStrategy> Strategies;

	/**
	 * initialise the ArrayList that used to store all strategies
	 */
	public CompositeStrategy() {
		this.Strategies = new ArrayList<>();
	}

	/**
	 * adding the strategy to the Store
	 * @param strategy the strategy that needs to add
	 */
	public void addStrategy(IScoreStrategy strategy){
		this.Strategies.add(strategy);
	}

	/**
	 * removing the strategy from the Store
	 * @param strategy the strategy that needs to remove
	 */
	public void removeStrategy(IScoreStrategy strategy){
		this.Strategies.remove(strategy);
	}

	/**
	 * Calculate the overall score from all stored strategies
	 * @param pickedUp the picked up card list
	 * @param Surs the picked up surs card list
	 * @return the total score of all stored strategies
	 */
	@Override
	public int CalcScore(Hand pickedUp, Hand Surs) {
		int finalPoint = 0;
		for(IScoreStrategy strategy: this.Strategies){
			finalPoint += strategy.CalcScore(pickedUp,Surs);
		}
		return finalPoint;
	}
}
