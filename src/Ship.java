import static java.awt.event.KeyEvent.*;

public class Ship {
    private double x;
    private double y;
    private double dx;

    private int lives = 3;

    private Draw win;
    private String filename = "ship.png";

    public Ship(Draw win, double x0, double y0){
        x = x0;
        y = y0;
        this.win = win;
    }

    public void step(){
        if(win.isKeyPressed(VK_LEFT)){
            dx = -0.015;
        } else if(win.isKeyPressed(VK_RIGHT)){
            dx = 0.015;
        }
        if(win.isKeyPressed(VK_LEFT) && win.isKeyPressed(VK_RIGHT)){
            dx = 0;
        }
        if(!win.isKeyPressed(VK_LEFT) && !win.isKeyPressed(VK_RIGHT)){
            dx = 0;
        }
        x += dx;

        win.picture(x, y, filename, 0.07, 0.07);
    }

    public void hit(){
        lives --;
    }

    public boolean checkCollide(Bullet bullet){
        double[] coords = bullet.getCoords();
        if(coords[0] > x-0.035 && coords[0] < x+0.035){
            if((coords[1]+0.01 > y-0.035 && coords[1]+0.01 < y+0.035 ) || (coords[1]-0.01 > y-0.035 && coords[1]-0.01 < y+0.035)){
                return true;
            }
        }
        return false;
    }

    public int getLives() {
        return lives;
    }

    public double getX() {
        return x;
    }
}
