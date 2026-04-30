public class Alien {
    private double x;
    private double y;
    private static double dx;
    private Draw win;
    private String filename = "invader.png";

    public Alien(Draw win, double x0, double y0){
        this.win = win;
        x = x0;
        y = y0;
        dx = Game.getSpeed();
    }

    public void step(boolean update){
        if(update){x += dx;}
        win.picture(x, y, filename, 0.06, 0.06);
    }

    public boolean checkBounce(){
        return x + 0.035 >= 1 || x - 0.035 <= 0;
    }

    public void bounce(){
        y -= 0.03;
        if(x + 0.035 >= 1){
            dx = -Game.getSpeed();
        }
        if(x - 0.035 <= 0){
            dx = Game.getSpeed();
        }
    }

    public double[] getCoords(){
        return new double[]{x, y};
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

    public static void setDx(double newDx){
        dx = newDx;
    }

}
