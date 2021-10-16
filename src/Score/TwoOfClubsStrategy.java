package Score;

import ch.aplu.jcardgame.Hand;
import pasur.Rank;
import pasur.Suit;

public class TwoOfClubsStrategy implements IScoreStrategy{
	private static final int POINT = 2;
	@Override
	public int CalcScore(Hand pickedUp, Hand Surs) {
		if(pickedUp.getCard(Suit.CLUBS, Rank.TWO) != null || Surs.getCard(Suit.CLUBS, Rank.TWO) != null){
			return POINT;
		}
		return 0;
	}
}
