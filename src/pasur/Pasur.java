package pasur;

/**
 * @author Alireza Ostovar
 * 29/09/2021
 */

import Score.CompositeStrategy;
import ch.aplu.jcardgame.Card;
import ch.aplu.jcardgame.Deck;
import ch.aplu.jcardgame.Hand;
import config.Configuration;
import logger.LogSubject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public class Pasur
{
    public static final String VERSION = "1.0";
    public static final String ON_RESET = "onReset";
    public static final String ON_UPDATE_SCORE = "onUpdateScore";
    public static final String ON_CARD_TRANSFER = "onCardTransfer";
    public static final String ON_GAME_END = "onGameEnd";

    // used for the simulation
    private static final Random random = new Random(Configuration.getInstance().getSeed());

    private static final int SCORE_TO_WIN = 62;
    private static final int N_HAND_CARDS = 4;
    private final int nPlayers;

    private boolean paused = true;
    private boolean gameStarted = false;

    private final Deck deck;
    private Hand deckHand;
    private final Hand poolHand;
    private final Player[] players;

    private PropertyChangeSupport propertyChangePublisher = new PropertyChangeSupport(this);

    private final LogSubject logger = LogSubject.getInstance();
    private final CompositeStrategy strategies = CompositeStrategy.getInstance();

    public Pasur(int nPlayers) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException
    {
        // Instantiate players
        this.nPlayers = nPlayers;

        players = new Player[nPlayers];
        Class<?> clazz;
        clazz = Class.forName(Configuration.getInstance().getPlayer0class());
        players[0] = (Player) clazz.getConstructor(int.class).newInstance(0);
        clazz = Class.forName(Configuration.getInstance().getPlayer1class());
        players[1] = (Player) clazz.getConstructor(int.class).newInstance(1);

        deck = new Deck(Suit.values(), Rank.values(), "cover", suit -> Rank.getCardValuesArray());

        poolHand = new Hand(deck);

        for (int i = 0; i < nPlayers; i++)
        {
            Player player = players[i];

            Hand hand = new Hand(deck);
            player.setHand(hand);

            // set the picked cards for this player
            Hand pickedCards = new Hand(deck);
            player.setPickedCards(pickedCards);

            // set the sur cards for this player
            Hand surCards = new Hand(deck);
            player.setSurs(surCards);
        }
    }

    public synchronized void pauseGame()
    {
        try {
            wait();
        } catch (InterruptedException ex) {
            ex.getStackTrace();
        }
    }

    public synchronized void resumeGame()
    {
        paused = false;
        notifyAll();
    }

    public void play()
    {
        if(gameStarted)
            return;
        gameStarted = true;

        // System.out.println("Game starts...");
        logger.notify("Game starts...");

        Player winner = null;

        int currentStartingPlayerPos = 0; // players should alternate for starting each round of game
        Player lastPlayerWhoPickedAcard = players[0];
        int roundOfGame = 0;
        List<Card> cardList = new ArrayList<>(1);
        while(winner == null)
        {
            roundOfGame++;
            // System.out.println("Round " + roundOfGame + " of the game starts...");
            logger.notify("Round " + roundOfGame + " of the game starts...");
            boolean isFirstRound = true;
            reset();

            if (paused) {
                pauseGame();
            }

            while (!deckHand.isEmpty())
            {
                if (paused) {
                    pauseGame();
                }

                dealingOutToPlayers(currentStartingPlayerPos);

                if(isFirstRound)
                {
                    dealingOutToPool();
                    isFirstRound = false;
                }

                boolean isLastRound = deckHand.isEmpty();

                for(int i = 0; i < N_HAND_CARDS; i++)
                {
                    if (paused) {
                        pauseGame();
                    }

                    for(int j = 0, k = currentStartingPlayerPos; j < nPlayers; j++)
                    {
                        if (paused) {
                            pauseGame();
                        }

                        Player player = players[k];
                        Map.Entry<Card, Set<Card>> playedCard_cardsToPick = player.playCard(poolHand);
                        Card playedCard = playedCard_cardsToPick.getKey();
                        Set<Card> cardsToPick = playedCard_cardsToPick.getValue();

                        cardList.clear();
                        cardList.add(playedCard);
                        transfer(cardList, poolHand, false);
                        playedCard.setVerso(false);

                        if(!cardsToPick.isEmpty())
                        {
                            lastPlayerWhoPickedAcard = player;

                            cardList.clear();
                            for(Card card : cardsToPick)
                            {
                                if (paused) {
                                    pauseGame();
                                }

                                cardList.add(card);
                            }

                            cardList.add(playedCard);
                            transfer(cardList, player.getPickedCards(), false);
                            for(Card card : cardList)
                            {
                                card.setVerso(true);
                            }

                            // System.out.println(player.toString() + " picks " + toString(cardList));
                            logger.notify(player.toString() + " picks " + toString(cardList));
                            if(isAsur(playedCard, isLastRound))
                            {
                                // player has a sur. If the other players have a sur this sur will be used to remove one of their surs.
                                // otherwise it will be added as a sur for this player

                                // System.out.println(player.toString() + " scores a sur");
                                logger.notify(player.toString() + " scores a sur");
                                int nOtherPlayersWithSure = 0;
                                for(int r = 0; r < nPlayers; r++)
                                {
                                    if(player != players[r] && !players[r].getSurs().isEmpty())
                                    {
                                        nOtherPlayersWithSure++;
                                    }
                                }

                                if(nOtherPlayersWithSure == nPlayers - 1)
                                {
                                    // other players have surs, so they lose one of their surs
                                    for(int r = 0; r < nPlayers; r++)
                                    {
                                        Player otherPlayer = players[r];
                                        if(player != otherPlayer)
                                        {
                                            Card surCard = otherPlayer.getSurs().get(otherPlayer.getSurs().getNumberOfCards() - 1);
                                            cardList.clear();
                                            cardList.add(surCard);
                                            transfer(cardList, otherPlayer.getPickedCards(), false);
                                            surCard.setVerso(true);
                                        }
                                    }
                                }else
                                {
                                    // other players don't have surs, so we add this as a sur for this player
                                    playedCard.setVerso(false);
                                    cardList.clear();
                                    cardList.add(playedCard);
                                    transfer(cardList, player.getSurs(), false);
                                }
                            }
                        }else
                        {
                            // System.out.println(player.toString() + " picks " + toString(cardsToPick));
                            logger.notify(player.toString() + " picks " + toString(cardsToPick));
                            // the played card of the player can't pick any card, so we have to leave it at the pool
                        }

                        k++;
                        if(k == nPlayers)
                            k = 0;

                        updateScores(false);
                    }
                }

                if(isLastRound)
                {
                    // give remaining cards in the pool to the last player who picked up a card

                    List<Card> poolCards = poolHand.getCardList();
                    if(!poolCards.isEmpty())
                        // System.out.println(lastPlayerWhoPickedAcard + " picks " + toString(poolCards) + " at the end of this round of game");
                        logger.notify(lastPlayerWhoPickedAcard + " picks " + toString(poolCards) + " at the end of this round of game");

                    cardList.clear();
                    for(int i = 0; i < poolCards.size(); i++)
                    {
                        if (paused) {
                            pauseGame();
                        }

                        Card card = poolCards.get(i);
                        cardList.add(card);
                        card.setVerso(true);
                    }
                    transfer(cardList, lastPlayerWhoPickedAcard.getPickedCards(), false);
                }
            }

            updateScores(true);

            currentStartingPlayerPos++;
            if(currentStartingPlayerPos == nPlayers)
                currentStartingPlayerPos = 0;

            // System.out.println("Round " + roundOfGame + " of the game ends...");
            logger.notify("Round " + roundOfGame + " of the game ends...");

            List<Player> playersWithEnoughScore = null;
            for(int i = 0; i < nPlayers; i++)
            {
                Player player = players[i];
                if(player.getTotalPoint() >= SCORE_TO_WIN)
                {
                    if(playersWithEnoughScore == null)
                        playersWithEnoughScore = new ArrayList<>();

                    playersWithEnoughScore.add(player);
                }
            }

            if(playersWithEnoughScore == null)
            {
                continue;
            }else
            {
                if(playersWithEnoughScore.size() > 1)
                {
                    // there are more than one player with the score above the threshold
                    playersWithEnoughScore.sort((o1, o2) -> -Integer.compare(o1.getScore(), o2.getScore()));
                    if(playersWithEnoughScore.get(0).getScore() == playersWithEnoughScore.get(1).getScore())
                    {
                        // the score of the top two players are the same, so we have to play another round
                        continue;
                    }
                }

                // we have a winner
                winner = playersWithEnoughScore.get(0);
            }
        }

        // System.out.println("Game ends...");
        logger.notify("Game ends...");
        String winningText = winner.toString() + " is the winner!";

        propertyChangePublisher.firePropertyChange(ON_GAME_END, null, winningText);

        // System.out.println(winningText);
        logger.notify(winningText);
    }

    private boolean isAsur(Card playedCard, boolean isLastRound)
    {
        if(poolHand.isEmpty())
        {
            // pool has become empty, potentially a sur
            if(!isLastRound && playedCard.getRank() != Rank.JACK)
            {
                // it is only a sur if the played card is not a jack and also we are not in the last round of play
                return true;
            }
        }

        return false;
    }

    /**
     * Reset to start a new round of the game
     */
    private void reset()
    {
        for(int i = 0; i < nPlayers; i++)
        {
            Player player = players[i];
            player.reset();
        }

        poolHand.removeAll(false);

        deckHand = deck.toHand(false);
        deckHand.setVerso(true);

        updateScores(true);

        propertyChangePublisher.firePropertyChange(ON_RESET, null, null);
    }

    private void updateScores(boolean bLastRound)
    {
        String scoreString = "";
        for (int i = 0; i < nPlayers; i++)
        {
            if(i != 0)
                scoreString += "        ";

            Player player = players[i];
            // logger.notify("PickedCard[%s]",player.getPickedCards().toString());
            // logger.notify("Surs[%s]",player.getSurs().toString());

            int score = CompositeStrategy.getInstance().CalcScore(player.getPickedCards(),player.getSurs());
            player.setCurrPoint(score);
            player.setCumulatePoint(player.getTotalPoint() + score);

            if (bLastRound) {
                player.setTotalPoint(player.getCumulatePoint());
            }

            scoreString += player.toString() + " = " + player.getScore() + " (" + player.getSurs().getNumberOfCards() + " Surs)";
        }

        propertyChangePublisher.firePropertyChange(ON_UPDATE_SCORE, null, scoreString);
//        scoreLabel.setText(scoreString);
        // System.out.println("Total Running Scores: " + scoreString);
        logger.notify("Total Running Scores: " + scoreString);
    }

    private void dealingOutToPlayers(int currentStartingPlayerPos)
    {
        // System.out.println("Dealing out to players...");
        logger.notify("Dealing out to players...");
        List<Card> cardList = new ArrayList<>(1);
        for (int i = 0, k = currentStartingPlayerPos; i < nPlayers; i++)
        {
            Player player = players[k];
            Hand hand = player.getHand();

            for (int j = 0; j < N_HAND_CARDS; j++)
            {
                if (paused) {
                    pauseGame();
                }

                // in a real game we should shuffle the cards at the very beginning of the game and
                // take the cards from the bottom of the deck for dealing, but here we do not shuffle the cards and take cards from random positions
                // in the deck so that we can reproduce the same result for the simulation every time (for marking purposes)
                Card card = randomCard(deckHand);
                cardList.clear();
                cardList.add(card);
                card.setVerso(false);  // Show the face
                transfer(cardList, hand, true);
            }

            k++;
            if(k == nPlayers)
                k = 0;

            // System.out.println(player.toString() + " hand: " + toString(player.getHand().getCardList()));
            logger.notify(player.toString() + " hand: " + toString(player.getHand().getCardList()));
        }
    }

    private void dealingOutToPool()
    {
        // System.out.println("Dealing out to pool...");
        logger.notify("Dealing out to pool...");
        List<Card> cardList = new ArrayList<>(1);
        for (int i = 0; i < N_HAND_CARDS; i++)
        {
            if (paused) {
                pauseGame();
            }

            // in a real game we should shuffle the cards at the very beginning of the game and
            // take the cards from the bottom of the deck for dealing, but here we do not shuffle the cards and take cards from random positions
            // in the deck so that we can reproduce the same result for the simulation every time (for marking purposes)
            Card card = randomCard(deckHand);
            if(card.getRank() == Rank.JACK)
            {
                // jack cannot be in the pool. In a real game we should place the jack in a random place in the deck
                i--;
            }else {
                cardList.clear();
                cardList.add(card);
                card.setVerso(false);  // Show the face
                transfer(cardList, poolHand, true);
            }
        }

        // System.out.println("Pool: " + toString(poolHand.getCardList()));
        logger.notify("Pool: " + toString(poolHand.getCardList()));
    }

    private void transfer(List<Card> cards, Hand h, boolean sortAfterTransfer)
    {
        if(cards.isEmpty())
            return;

        boolean doDraw = !sortAfterTransfer;

        propertyChangePublisher.firePropertyChange(ON_CARD_TRANSFER, null, new Object[]{cards, h, doDraw});

        for(int i = 0; i < cards.size(); i++)
        {
            Card c = cards.get(i);
            c.removeFromHand(doDraw);
            h.insert(c, doDraw);
        }

        if(sortAfterTransfer)
        {
            h.sort(Hand.SortType.RANKPRIORITY, true);
        }
    }

    private String toString(Collection<Card> cards)
    {
        Hand h = new Hand(deck); // Clone to sort without changing the original hand
        for (Card c : cards)
        {
            h.insert(c.getSuit(), c.getRank(), false);
        }
        h.sort(Hand.SortType.RANKPRIORITY, false);

        return "[" + h.getCardList().stream().map(Pasur::toString).collect(Collectors.joining(", ")) + "]";
    }

    public int getnPlayers()
    {
        return nPlayers;
    }

    public Deck getDeck()
    {
        return deck;
    }

    public Hand getDeckHand()
    {
        return deckHand;
    }

    public Hand getPoolHand()
    {
        return poolHand;
    }

    public Player[] getPlayers()
    {
        return players;
    }

    public boolean isPaused()
    {
        return paused;
    }

    public void setPaused(boolean paused)
    {
        this.paused = paused;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener)
    {
        propertyChangePublisher.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener)
    {
        propertyChangePublisher.removePropertyChangeListener(propertyChangeListener);
    }

    public static String toString(Card c)
    {
        return ((Rank)c.getRank()).canonical() + "-" + ((Suit)c.getSuit()).canonical();
    }


    public static Card randomCard(Hand hand)
    {
        int x = random.nextInt(hand.getNumberOfCards());
        return hand.get(x);
    }
}
