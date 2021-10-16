package Score;

import ch.aplu.jcardgame.Hand;
import pasur.Rank;

public class JackStrategy implements IScoreStrategy {
	private static final int POINT = 1;
	@Override
	public int CalcScore(Hand pickedUp, Hand Surs) {
		int Num_Club_pick = pickedUp.getNumberOfCardsWithRank(Rank.JACK);
		int Num_Club_Sur = Surs.getNumberOfCardsWithRank(Rank.JACK);
		return (Num_Club_pick + Num_Club_Sur) * POINT;
	}
}
