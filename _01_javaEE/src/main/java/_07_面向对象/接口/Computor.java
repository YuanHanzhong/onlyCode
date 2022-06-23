package _07_面向对象.接口;

public class Computor implements Typec,USB{
    
    @Override
    public Boolean getStatus() {
        return true;
    }
    
    @Override
    public int getDianliang() {
        return 100;
    }
    
    @Override
    public Typec hanshu() {
        return null;
    }
}
