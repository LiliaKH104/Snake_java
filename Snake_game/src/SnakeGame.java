import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.font.TextLayout;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    private final Random random=new Random();
    private final int width;
    private final int height;
    private final int cellSize;
    private static final int FRAME_RATE=20;
    private boolean gameStarted=false;
    private boolean gameOver=false;
    private int highScore;
    private GamePoint food;
    private final List<GamePoint> snake=new ArrayList<>();
    private enum Direction{
        UP,DOWN,RIGHT,LEFT
    }
    private Direction newDirection=Direction.RIGHT;
    private Direction direction=Direction.RIGHT;

    public SnakeGame(final int width, final int height){
        this.width=width;
        this.height=height;
        this.cellSize=width/(FRAME_RATE*2);
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.DARK_GRAY);
    }
    public void startGame(){
        resetGameData();
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                handleKeyEvent(e.getKeyCode());
            }
        });
        new Timer(4000/FRAME_RATE,this ).start();

    }

    private void handleKeyEvent(final int keyCode) {
        if(!gameStarted){
            if(keyCode==KeyEvent.VK_SPACE){
                gameStarted=true;
            }
        } else if (!gameOver) {
            switch (keyCode){
                case KeyEvent.VK_UP:
                    if(direction!=Direction.DOWN) {
                        newDirection = Direction.UP;
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if(direction!=Direction.UP) {
                        newDirection = Direction.DOWN;
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if(direction!=Direction.RIGHT) {
                        newDirection = Direction.LEFT;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if(direction!=Direction.LEFT) {
                        newDirection = Direction.RIGHT;
                    }
                    break;
            }
        }
        else if(keyCode==KeyEvent.VK_SPACE){
            gameStarted=false;
            gameOver=false;
            resetGameData();
        }
    }

    private void resetGameData(){
        snake.clear();
        snake.add(new GamePoint(width/2, height/2));
        generateFood();
    }
    private void generateFood(){
        do{
            food=new GamePoint(random.nextInt(width/cellSize)*cellSize,
                    random.nextInt(height/cellSize)*cellSize);
        }
        while(snake.contains(food));
    }

    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if(!gameStarted){
            printMessage(g,"Press Space Bar to start!");
        }
        else{
            g.setColor(Color.BLUE);
            g.fillRect(food.x,food.y,cellSize,cellSize);
            Color snakeColor=Color.GREEN;
            for(final var point:snake){
                g.setColor(snakeColor);
                g.fillRect(point.x,point.y,cellSize,cellSize);
                final int newGreen=(int) Math.round(snakeColor.getGreen()*(0.9));
                snakeColor=new Color(0,newGreen,0);
            }
            if(gameOver){
                final int currentScore=snake.size();
                if(currentScore>highScore){
                    highScore=currentScore;
                }
                printMessage(g,"Your Score: "+currentScore+
                        "\n Highest Score:"+highScore
                        + "\n Press Space Bar to Reset!");
            }
        }
    }
    private void printMessage(final Graphics g,final String message){
        g.setColor(Color.white);
        g.setFont(g.getFont().deriveFont(30F));
        int currentHeight = height / 3;
        final var g2D = (Graphics2D) g;
        final var frc = g2D.getFontRenderContext();
        for (final var line : message.split("\n")) {
            final var layout = new TextLayout(line, g.getFont(), frc);
            final var bounds = layout.getBounds();
            final var targetWidth = (float) (width - bounds.getWidth()) / 2;
            layout.draw(g2D, targetWidth, currentHeight);
            currentHeight += g.getFontMetrics().getHeight();
        }
    }
    private boolean checkCollision(){
        final GamePoint head=snake.getFirst();
        final var invalidWidth=(head.x<0)||(head.x>=width);
        final var invalidHeight=(head.y<0)||(head.y>=height);
        if (invalidHeight || invalidWidth){
            return true;
        }
        return snake.size()!=new HashSet<>(snake).size();
    }
    private void move(){
        direction=newDirection;
        final GamePoint head=snake.getFirst();
        final GamePoint newHead=switch(direction){
            case UP->new GamePoint(head.x,head.y-cellSize);
            case DOWN->new GamePoint(head.x,head.y+cellSize);
            case RIGHT -> new GamePoint(head.x+cellSize,head.y);
            case LEFT -> new GamePoint(head.x-cellSize,head.y);
        };
        snake.addFirst(newHead);
        if(newHead.equals(food)){
            generateFood();
        }
        else if(checkCollision()){
            gameOver=true;
            snake.removeFirst();
        }
        else {
            snake.removeLast();
        }
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if(gameStarted&&!gameOver) {
            move();
        }
        repaint();
    }
    private record GamePoint(int x, int y){}
}
