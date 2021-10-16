package Score;

import ch.aplu.jcardgame.Hand;
import pasur.Rank;
import pasur.Suit;

public class SevenClubsStrategy implements IScoreStrategy{

	private static final int POINT = 7;
	private static final int NUMBER_OF_CLUBS = 7;
	@Override
	public int CalcScore(Hand pickedUp, Hand Surs) {
		int Num_Club_pick = pickedUp.getNumberOfCardsWithSuit(Suit.CLUBS);
		int Num_Club_Sur = Surs.getNumberOfCardsWithSuit(Suit.CLUBS);
		if (Num_Club_pick + Num_Club_Sur >= NUMBER_OF_CLUBS){
			return POINT;
		}
		return 0;
	}
}
