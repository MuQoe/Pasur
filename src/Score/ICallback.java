package Score;

public interface ICallback {
	/**
	 * the callback function to add the strategy to the Composite Strategy
	 * @param strategy the strategy that needs to add
	 */
	void addStrategy(IScoreStrategy strategy);
}
