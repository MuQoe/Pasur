package Score;

import ch.aplu.jcardgame.Hand;

public interface IScoreStrategy {
	int CalcScore(Hand pickedUp, Hand Surs);
}
