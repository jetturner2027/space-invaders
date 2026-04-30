public class Shield {
    private String filename = "shields/shield_1.png";
    private double x;
    private double y;
    private boolean alive = true;
    private double width = 0.14;
    private double height = 0.08;
    private int hits = 0;
    private Draw win;

    public Shield(double x0, double y0, Draw win){
        x = x0;
        y = y0;
        this.win = win;
    }

    public boolean checkCollide(Bullet bullet){
        if(alive) {
            double[] coords = bullet.getCoords();
            if (coords[0] > x - (width / 2) && coords[0] < x + (width / 2)) {
                if ((coords[1] + 0.01 > y - (height / 2) && coords[1] + 0.01 < y + (height / 2)) || (coords[1] - 0.01 > y - (height / 2) && coords[1] - 0.01 < y + (height / 2))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void hit(){
        hits++;
        filename = "shields/shield_" + (hits+1) + ".png";
        if(hits == 8){
            alive = false;
        }
    }

    public void step(){
        if(alive) {
            win.picture(x, y, filename, width, height);
        }
    }

}
