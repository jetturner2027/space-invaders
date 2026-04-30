public class Bullet {
    private double x;
    private double y = 0.1;
    private double dy = 0.07;
    private String filename = "bullets/bullet.png";
    private Draw win;

    public Bullet(Draw win, double x0, double y0, boolean enemy){
        x = x0;
        y = y0;
        this.win = win;
        if(enemy){
            filename = "bullets/enemyBullet.png";
            dy = -0.45*dy;
        }
    }

    public void step(){
        y += dy;
        win.picture(x, y, filename, 0.005, 0.02);
    }

    public double[] getCoords(){
        return new double[]{x, y};
    }
}
