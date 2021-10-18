package Score;

import ch.aplu.jcardgame.Hand;

public interface IScoreStrategy {
	/**
	 * functions to calculate the score obtain by a specific Score strategy
	 * @param pickedUp the picked up card list
	 * @param Surs the picked up surs card list
	 * @return the score calculated by specific score strategy
	 */
	int CalcScore(Hand pickedUp, Hand Surs);
}
