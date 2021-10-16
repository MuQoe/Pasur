package Score;

import ch.aplu.jcardgame.Hand;
import pasur.Rank;
import pasur.Suit;

public class TenOfDiamondStrategy implements IScoreStrategy{
	private static final int POINT = 3;
	@Override
	public int CalcScore(Hand pickedUp, Hand Surs) {
		if(pickedUp.getCard(Suit.DIAMONDS, Rank.TEN) != null || Surs.getCard(Suit.DIAMONDS, Rank.TEN) != null){
			return POINT;
		}
		return 0;
	}
}
