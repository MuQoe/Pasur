package Score;

import ch.aplu.jcardgame.Hand;

import java.util.ArrayList;

public class CompositeStrategy implements IScoreStrategy, ICallback{

	private static CompositeStrategy _instance = null;
	private final ArrayList<IScoreStrategy> Strategies;
	public static CompositeStrategy getInstance() {
		if (_instance == null) {
			synchronized(CompositeStrategy.class) {
				if (_instance == null) {
					// Set the Default Level to DEBUG level
					_instance = new CompositeStrategy();
				}
			}
		}
		return _instance;
	}

	private CompositeStrategy() {
		this.Strategies = new ArrayList<>();
	}

	public void addStrategy(IScoreStrategy strategy){
		this.Strategies.add(strategy);
	}
	public void removeStrategy(IScoreStrategy strategy){
		this.Strategies.remove(strategy);
	}


	@Override
	public int CalcScore(Hand pickedUp, Hand Surs) {
		int finalPoint = 0;
		for(IScoreStrategy strategy: this.Strategies){
			finalPoint += strategy.CalcScore(pickedUp,Surs);
		}
		return finalPoint;
	}
}
