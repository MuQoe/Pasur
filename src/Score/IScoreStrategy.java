package Score;

import ch.aplu.jcardgame.Hand;

public interface IScoreStrategy {
	public int CalcScore(Hand hand, Hand Surs);
}
