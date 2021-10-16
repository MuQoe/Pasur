package Score;

import ch.aplu.jcardgame.Hand;

public class SursStrategy implements IScoreStrategy{
	private static final int POINT = 5;

	@Override
	public int CalcScore(Hand pickedUp, Hand Surs) {
		return Surs.getNumberOfCards() * POINT;
	}
}
