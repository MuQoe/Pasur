package Score;

import java.util.ArrayList;

public class StrategyHandler {


	/**
	 * the functions to manage which strategy that need to add the the overall compositeStrategies
	 * @param callback the callback function that used to add the strategy to the compositeStrategies
	 */
	public static void allAllStrategy(ICallback callback){
		ArrayList<IScoreStrategy> arr = new ArrayList<>();
		arr.add(new AceStrategy());
		arr.add(new JackStrategy());
		arr.add(new SursStrategy());
		arr.add(new SevenClubsStrategy());
		arr.add(new TenOfDiamondStrategy());
		arr.add(new TwoOfClubsStrategy());
		for (IScoreStrategy strategy: arr){
			callback.addStrategy(strategy);
		}
	}
}
