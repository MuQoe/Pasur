package pasur;

/**
 * @author Alireza Ostovar
 * 29/09/2021
 */

import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.Location;
import ch.aplu.jgamegrid.TextActor;
import ch.aplu.util.Monitor;
import config.Configuration;
import logger.PasurTrainerLogger;
import logger.LogSubject;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class PasurGUI implements PropertyChangeListener
{
    private static final int SCREEN_WIDTH = 1024;
    private static final int SCREEN_HEIGHT = 650;
    private static final int GAP = 20;

    private final int CARD_WIDTH;
    private final int CARD_HEIGHT;
    private final int HAND_WIDTH;
    private final int POOL_WIDTH;
    private boolean animate;
    private final Location[] HAND_LOCATIONS;
    private final Location[] PICKED_CARDS_LOCATIONS;
    private final Location[] SURS_LOCATION;
    private final Location DECK_LOCATION;
    private final Location POOL_LOCATION;

    private CardGame gameGUI;
    private JSlider speedSlider;
    private JLabel scoreLabel;

    private Pasur pasur;

    // private CompositeStrategy strategies = CompositeStrategy.getInstance();

    public PasurGUI() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException,
            InstantiationException
    {
        LogSubject.getInstance().Attach(new PasurTrainerLogger());

        pasur = new Pasur(2);



        animate = Configuration.getInstance().isAnimate();

        setUpGameGUI();

        Dimension cardDimension = pasur.getDECK().getCardDimension();
        CARD_WIDTH = (int) Math.ceil(cardDimension.getWidth());
        CARD_HEIGHT = (int) Math.ceil(cardDimension.getHeight());
        HAND_WIDTH = CARD_WIDTH * 4;
        POOL_WIDTH = CARD_WIDTH * 6;

        DECK_LOCATION = new Location(GAP + CARD_WIDTH / 2, SCREEN_HEIGHT / 2);

        int slideStep = 20; // the distance a card travels per frame
        POOL_LOCATION = new Location(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
        TargetArea poolTargetArea = new TargetArea(POOL_LOCATION, CardOrientation.NORTH, slideStep, true);
        pasur.getPOOL_HAND().setVerso(false);
        pasur.getPOOL_HAND().setTargetArea(poolTargetArea);
        RowLayout poolLayout = new RowLayout(POOL_LOCATION, POOL_WIDTH);
        poolLayout.setRotationAngle(0);
        poolLayout.setCardAlignment(Hand.CardAlignment.MIDDLE);
        pasur.getPOOL_HAND().setView(gameGUI, poolLayout);

        HAND_LOCATIONS = new Location[]{
                new Location(300, CARD_HEIGHT / 2 + GAP),
                new Location(300, SCREEN_HEIGHT - CARD_HEIGHT / 2 - GAP)
        };

        PICKED_CARDS_LOCATIONS = new Location[]{
                new Location(HAND_LOCATIONS[0].getX() + HAND_WIDTH + 2 * GAP, HAND_LOCATIONS[0].getY()),
                new Location(HAND_LOCATIONS[1].getX() + HAND_WIDTH + 2 * GAP, HAND_LOCATIONS[1].getY())
        };

        SURS_LOCATION = new Location[]{
                new Location(PICKED_CARDS_LOCATIONS[0].getX() + CARD_WIDTH + 2 * GAP, PICKED_CARDS_LOCATIONS[0].getY()),
                new Location(PICKED_CARDS_LOCATIONS[1].getX() + CARD_WIDTH + 2 * GAP, PICKED_CARDS_LOCATIONS[1].getY())
        };

        Player[] players = pasur.getPLAYERS();
        for (int i = 0; i < pasur.getN_PLAYERS(); i++)
        {
            Player player = players[i];

            Hand hand = player.getHand();
            hand.setVerso(false);
            hand.setTargetArea(new TargetArea(HAND_LOCATIONS[i], CardOrientation.NORTH, slideStep, true));
            RowLayout handLayout = new RowLayout(HAND_LOCATIONS[i], HAND_WIDTH);
            handLayout.setRotationAngle(0);
            handLayout.setCardAlignment(Hand.CardAlignment.FIRST);
            hand.setView(gameGUI, handLayout);

            // set the picked cards for this player
            Hand pickedCards = player.getPickedCards();
            pickedCards.setVerso(true);
            pickedCards.setTargetArea(new TargetArea(PICKED_CARDS_LOCATIONS[i], CardOrientation.NORTH, slideStep, true));
            RowLayout pickedCardsLayout = new RowLayout(PICKED_CARDS_LOCATIONS[i], CARD_WIDTH);
            pickedCardsLayout.setRotationAngle(0);
            pickedCardsLayout.setCardAlignment(Hand.CardAlignment.FIRST);
            pickedCards.setView(gameGUI, pickedCardsLayout);

            // set the sur cards for this player
            Hand surCards = player.getSurs();
            surCards.setVerso(false);
            surCards.setTargetArea(new TargetArea(SURS_LOCATION[i], CardOrientation.NORTH, slideStep, true));
            RowLayout surCardsLayout = new RowLayout(SURS_LOCATION[i], CARD_WIDTH);
            surCardsLayout.setRotationAngle(0);
            surCardsLayout.setCardAlignment(Hand.CardAlignment.FIRST);
            surCards.setView(gameGUI, surCardsLayout);
        }

        pasur.addPropertyChangeListener(this);
    }

    private void setUpGameGUI()
    {
        gameGUI = new CardGame(SCREEN_WIDTH, SCREEN_HEIGHT, 0);
        gameGUI.addStatusBar(0);
        gameGUI.showStatusBar(false);

        int scorePanelHeight = 40;
        int toolbarPanelHeight = 40;
        JPanel contentPane = (JPanel)gameGUI.getFrame().getContentPane();
        contentPane.setPreferredSize(new Dimension(Math.max(450, SCREEN_WIDTH), SCREEN_HEIGHT +
                scorePanelHeight + toolbarPanelHeight));

        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setPreferredSize(new Dimension(SCREEN_WIDTH, scorePanelHeight));

        int initialSpeed = 1;
        gameGUI.setSimulationPeriod(initialSpeed);
        speedSlider = new JSlider(1, 100, initialSpeed);
        speedSlider.setPreferredSize(new Dimension(100, speedSlider.getPreferredSize().height));
        speedSlider.setMaximumSize(speedSlider.getPreferredSize());
        speedSlider.setInverted(true);
        speedSlider.setPaintLabels(false);
        speedSlider.setEnabled(animate);
        speedSlider.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent evt)
            {
                JSlider source = (JSlider)evt.getSource();
                if (!source.getValueIsAdjusting())
                {
//                    setAnimationSpeed(source);
                    gameGUI.setSimulationPeriod(source.getValue());
                    gameGUI.requestFocus();
                }
            }
        });

        JCheckBox animateCheckBox = new JCheckBox("Animation");
        animateCheckBox.setSelected(animate);
        animateCheckBox.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                animate = e.getStateChange() == ItemEvent.SELECTED;
                speedSlider.setEnabled(animate);

                if(!animate)
                {
                    speedSlider.setValue(initialSpeed);
                    gameGUI.setSimulationPeriod(initialSpeed);
//                    setAnimationSpeed(speedSlider);
                }
            }
        });

        JButton playPauseBtn = new JButton("Play");
        playPauseBtn.setPreferredSize(new Dimension(70, 25));
        playPauseBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent evt)
            {
                pasur.setPaused(!pasur.isPaused());
                playPauseBtn.setText(pasur.isPaused() ? "Play" : "Pause");

                if(!pasur.isPaused())
                {
                    pasur.resumeGame();
                }

            }
        });

        toolbarPanel.add(playPauseBtn);
        toolbarPanel.add(animateCheckBox);
        toolbarPanel.add(new JLabel("     "));
        toolbarPanel.add(new JLabel("Slow"));
        toolbarPanel.add(speedSlider);
        toolbarPanel.add(new JLabel("Fast"));

        JPanel scorePanel = new JPanel();
        scorePanel.setPreferredSize(new Dimension(SCREEN_WIDTH, scorePanelHeight));

        Font font = new Font("Arial", Font.PLAIN, 24);
        JLabel scoreTitleLabel = new JLabel("Total Running Scores:    ");
        scoreTitleLabel.setFont(font);

        scoreLabel = new JLabel();
        scoreLabel.setFont(font);

        scorePanel.add(scoreTitleLabel);
        scorePanel.add(scoreLabel);

        contentPane.add(scorePanel, BorderLayout.NORTH);
        contentPane.add(gameGUI, BorderLayout.CENTER);
        contentPane.add(toolbarPanel, BorderLayout.SOUTH);

        gameGUI.getFrame().pack();

        gameGUI.setTitle(pasur.getN_PLAYERS() + "-player Pasur (V" + Pasur.VERSION + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
    }

    private void setupScene()
    {
        RowLayout layout = new RowLayout(DECK_LOCATION, CARD_WIDTH);
        layout.setRotationAngle(0);
        layout.setCardAlignment(Hand.CardAlignment.FIRST);
        pasur.getDeckHand().setView(gameGUI, layout);
        pasur.getDeckHand().setVerso(true);
        pasur.getDeckHand().draw();
    }

    private void updateScores(String scoreString)
    {
        scoreLabel.setText(scoreString);
    }

    private void startGame()
    {
        pasur.play();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Pasur.ON_RESET))
        {
            setupScene();
        }else if(propertyName.equals(Pasur.ON_UPDATE_SCORE))
        {
            String scoreString = (String) evt.getNewValue();
            updateScores(scoreString);
        }else if(propertyName.equals(Pasur.ON_CARD_TRANSFER))
        {
            Object[] values = (Object[]) evt.getNewValue();
            List<Card> cards = (List<Card>) values[0];
            Hand hand = (Hand) values[1];
            boolean doDraw = (boolean) values[2];

            Hand cardHand = cards.get(0).getHand();
            TargetArea currentTargetArea = cardHand.getTargetArea();

            cardHand.setTargetArea(hand.getTargetArea());

            if(animate)
            {
                AtomicInteger targetCount = new AtomicInteger(0);
                cardHand.addCardListener(new CardAdapter()
                {
                    public void atTarget(Card card, Location loc)
                    {
                        targetCount.incrementAndGet();
                        if (targetCount.get() == cards.size())
                        {
                            Monitor.wakeUp();  // All cards in the hand
                        }
                    }
                });

                for(int i = 0; i < cards.size(); i++)
                {
                    Card c = cards.get(i);
                    cardHand.transferNonBlocking(c, hand, doDraw);
                }
                Monitor.putSleep();
                hand.draw();
            }

            // set back target area to what it used to be before calling this method
            cardHand.setTargetArea(currentTargetArea);
        }else if(propertyName.equals(Pasur.ON_GAME_END))
        {
            String winningText = (String) evt.getNewValue();

            TextActor winnerText = new TextActor(winningText,
                    Color.YELLOW, gameGUI.getBgColor(), new Font("Arial", Font.BOLD, 36));
            Location winnerLocation = new Location(SCREEN_WIDTH / 2 - winnerText.getTextWidth() / 2, SCREEN_HEIGHT / 2);
            gameGUI.addActor(winnerText, winnerLocation);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException
    {
        new PasurGUI().startGame();
    }
}
