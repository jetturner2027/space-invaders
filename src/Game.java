import java.awt.*;
import java.io.*;
import java.util.*;

import static java.awt.event.KeyEvent.*;
import static java.lang.System.exit;

public class Game{

    //setupGame fields
    private static boolean inMenu = true;
    private static Draw win = new Draw();
    private static int rows = 5;
    private static int columns = 12;
    private static double speed = 0.01;
    private  static boolean dead = false;
    private static int score = 0;
    private static int highScore = 0;

    //player enemy and bullet fields
    private static Ship ship;
    private static Alien[][] aliens = new Alien[rows][columns];
    private static Shield[] shields = new Shield[4];
    private static Bullet bullet = null;
    private static Bullet[] enemyBullets = new Bullet[]{null, null, null, null, null};

    //timer fields
    private static int stepCount = 0;
    private static int buffer = 12;

    //leaderboard fields
    private static Scanner leaderboardScanner;
    private static PrintWriter leaderboardWriter;

    //font
    private static InputStream is = Game.class.getResourceAsStream("/fonts/HomeVideo-BLG6G.ttf");
    private static Font font;



    public static void main(String[] args) {
        try{
            font = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception e) {
            throw new RuntimeException("Error Retrieving Font");
        }
        setupMenu();
        win.enableTimer(30);
        win.enableDoubleBuffering();

        win.addListener(new DrawListener(){
            @Override
            public void update(){
                //makes the menu scene work
                if(inMenu){
                    if(win.isKeyPressed(VK_ENTER)){
                        inMenu = false;
                        setupGame();
                    } else if (win.isKeyPressed((VK_TAB))) {
                        exit(67);
                    }
                    return;
                }
                //makes the red flash at game over work
                if(dead){
                    win.pause(1500);
                    gameOver();
                    return;
                }

                //draws UI components
                stepCount++;
                win.setPenColor(Draw.BLACK);
                win.filledRectangle(0.5, 0.5, 0.5, 0.5);
                win.setPenColor(0, 252, 0);
                win.line(0, 0.1, 1, 0.1);
                win.setPenColor(Draw.WHITE);
                font = font.deriveFont(26f);
                win.setFont(font);
                win.text(0.05, 0.05, "" + ship.getLives());
                win.text(0.225, 0.95, "SCORE:" + score);
                win.text(0.725, 0.95, "HI-SCORE:" + highScore);

                //draws ship life indicators
                double x = 0.15;
                for(int i = 1; i < ship.getLives(); i++){
                    win.picture(x, 0.06, "ship.png", 0.07, 0.07);
                    x += 0.1;
                }

                //updates ship
                ship.step();

                //registers shots
                if(win.isKeyPressed(VK_SPACE)){
                    if(bullet == null){
                        bullet = new Bullet(win, ship.getX(), 0.1, false);
                    }
                }

                //checks alien bounce
                boolean bounce = false;
                if(stepCount % buffer == 0){
                    for(int r = 0; r < aliens.length; r++){
                        for(int c = 0; c < aliens[0].length; c++){
                            if(aliens[r][c] != null){
                                if(aliens[r][c].checkBounce()){
                                    bounce = true;
                                }
                            }
                        }
                    }
                    if(bounce){
                        for(int r = 0; r < aliens.length; r++){
                            for(int c = 0; c < aliens[0].length; c++){
                                if(aliens[r][c] != null){
                                    aliens[r][c].bounce();
                                }
                            }
                        }
                    }
                }

                //many, many checks for each alien
                for(int r = 0; r < rows; r++){
                    for(int c = 0; c < columns; c++) {
                        if(aliens[r][c] != null){
                            aliens[r][c].step(stepCount%buffer==0);

                            if (bullet != null) {
                                if (aliens[r][c].checkCollide(bullet)) {
                                    win.picture(aliens[r][c].getCoords()[0], aliens[r][c].getCoords()[1], "explosion.jpg", 0.06, 0.06);
                                    aliens[r][c] = null;
                                    bullet = null;

                                    if(r == 0){
                                        score += 30;
                                    } else if(r < 3){
                                        score += 20;
                                    } else{
                                        score += 10;
                                    }

                                    if(countAliens() == 1){
                                        buffer = 2;
                                        speed *= 2.0;
                                        Alien.setDx(speed);
                                    }
                                }
                            }

                            if(Math.random() < 1/15.0/countAliens()){
                                shoot();
                            }
                        }
                    }
                }

                //multiple checks for shields
                for(Shield shield: shields) {
                    if (shield != null){
                        shield.step();
                        for (int i = 0; i < enemyBullets.length; i++) {
                            if (enemyBullets[i] != null) {
                                if (shield.checkCollide(enemyBullets[i])) {
                                    enemyBullets[i] = null;
                                    shield.hit();
                                }
                            }
                        }
                        if (bullet != null) {
                            if (shield.checkCollide(bullet)) {
                                bullet = null;
                            }
                        }
                    }
                }

                //checks if bullet went too far
                if(bullet != null){
                    bullet.step();
                    if(bullet.getCoords()[1] > 1){
                        bullet = null;
                    }
                }

                //runs multiple checks for enemy bullets
                for(int i = 0; i < enemyBullets.length; i++){
                    if(enemyBullets[i] != null) {
                        enemyBullets[i].step();
                        if (enemyBullets[i].getCoords()[1] < 0.1) {
                            enemyBullets[i] = null;
                        } else if (ship.checkCollide(enemyBullets[i])) {
                            enemyBullets[i] = null;
                            ship.hit();
                            win.setPenColor(new Color(255, 0, 0, 100));
                            win.filledRectangle(0.5, 0.5, 0.5, 0.5);

                            if(ship.getLives() == 0){
                                win.show();
                                dead = true;
                                return;
                            }
                        }
                    }
                }

                //checks if game should make a new round
                if(countAliens() == 0 && ship != null){
                    score += 100;
                    speed *= 1.05;
                    setupGame();
                }

                win.show();


            }
        });
    }

    //does some stuff for ending the game
    private static void gameOver() {
        dead = false;
        ship = null;
        speed = 0.01;
        updateStats();
        score = 0;
        setupMenu();
    }

    //stops game and draws the main menu
    private static void setupMenu(){
        aliens = new Alien[rows][columns];
        shields = new Shield[4];
        bullet = null;
        enemyBullets = new Bullet[]{null, null, null, null, null};
        inMenu = true;
        dead = false;

        buffer = 12;
        win.clear();

        win.setPenColor(Draw.BLACK);
        win.filledRectangle(0.5, 0.5, 0.5, 0.5);

        win.setPenColor(0, 252, 0);
        font = font.deriveFont(48f);
        win.setFont(font);
        win.text(0.5, 0.85, "S P A C E");
        win.text(0.5, 0.75, "I N V A D E R S");
        win.line(0, 0.1, 1, 0.1);

        win.setPenColor(Color.WHITE);
        win.rectangle(0.75, 0.415, 0.225, 0.25);
        win.line(0.525, 0.56, 0.975, 0.56);
        font = font.deriveFont(26f);
        win.setFont(font);
        win.text(0.75, 0.6, "Leaderboard");
        displayStats();

        font = font.deriveFont(32f);
        win.setFont(font);
        win.text(0.25, 0.575, "Press Enter");
        win.text(0.25, 0.5, "To Start");
        win.text(0.25, 0.325, "Press Tab");
        win.text(0.25, 0.25, "To Quit");

        win.show();

    }

    //sets up and starts game
    private static void setupGame() {
        inMenu = false;
        buffer = 12;
        speed /= 2;
        Alien.setDx(speed);

        ship = new Ship(win, 0.5, 0.15);

        double x = 0.11;
        double y = 0.85;

        for(int r = 0; r < aliens.length; r++){
            for(int c = 0; c < aliens[0].length; c++){
                aliens[r][c] = new Alien(win, x, y);
                x += 0.07;
            }
            x = 0.11;
            y -= 0.07;
        }


        x = 0.125;
        for(int i = 0; i < shields.length; i++){
            shields[i] = new Shield(x, 0.25, win);
            x += 0.25;
        }
    }

    //the name says it all
    public static int countAliens(){
        int count = 0;

        for(int r = 0; r < aliens.length; r++){
            for(int c = 0; c < aliens[0].length; c++){
                if(aliens[r][c] != null){
                    count ++;
                }
            }
        }
        return count;
    }

    //try to guess what this one does
    public static double getSpeed(){
        return speed;
    }

    //this is a fun game
    public static void shoot(){
        int numAliens = countAliens();
        int shootingAlien = (int)(Math.random()*numAliens);
        int alienCount = 0;
        boolean shot = false;

        for(int r = 0; r < aliens.length; r++){
            for(int c = 0; c < aliens[0].length; c++){
                if(aliens[r][c] != null){
                    if(shootingAlien == alienCount){
                        for(int i = 0; i < enemyBullets.length; i++){
                            if(enemyBullets[i] == null && !shot){
                                enemyBullets[i] = new Bullet(win, aliens[r][c].getCoords()[0], aliens[r][c].getCoords()[1], true);
                                shot = true;
                            }
                        }
                    } else{
                        alienCount++;
                    }
                }
            }
        }

    }

    //self-explanatory
    private static void updateStats() {
        try {
            leaderboardScanner = new Scanner(new File("leaderboard.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("Error with retrieving files.");
        }

        ArrayList<String> entries = new ArrayList<>();
        while(leaderboardScanner.hasNextLine()) {
            entries.add(leaderboardScanner.nextLine());
        }
        entries.add("" + score);

        try {
            leaderboardWriter = new PrintWriter("leaderboard.txt");
        } catch (FileNotFoundException e) {
            System.out.println("Error with retrieving files.");
        }

        for(int i = 0; i < entries.size(); i++){
            if(i != entries.size()-1) {
                leaderboardWriter.println(entries.get(i));
            }else {
                leaderboardWriter.print(entries.get(i));
            }
        }
        leaderboardWriter.flush();
        leaderboardWriter.close();
    }

    //draws top 5 leaderboard to specific menu
    private static void displayStats(){
        ArrayList<String> leaderboard = new ArrayList<>();
        font = font.deriveFont(24f);
        win.setFont(font);

        try {
            leaderboardScanner = new Scanner(new File("leaderboard.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error with retrieving files.");
        }

        while(leaderboardScanner.hasNextLine()){
            leaderboard.add(leaderboardScanner.nextLine());
        }

        boolean highScoreCalculated = false;
        for(int i = 0; i < 5; i++) {
            if (!leaderboard.isEmpty()) {
                int max = Integer.MIN_VALUE;
                int maxIndex = 0;
                for (String entry : leaderboard) {
                    int value = Integer.parseInt(entry);
                    if (value >= max) {
                        max = value;
                        maxIndex = leaderboard.indexOf(entry);
                    }
                }
                if (!highScoreCalculated) {
                    highScore = max;
                    highScoreCalculated = true;
                }

                win.text(0.75, 0.5-(0.07*i), (i+1) + ". " + max);
                leaderboard.remove(maxIndex);
            }

        }
    }

}
