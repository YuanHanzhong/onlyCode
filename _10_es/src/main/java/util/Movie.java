package util;

public class Movie {
    Integer id;
    String name;
    
    @Override
    public String toString() {
        return "Movie{" +
                 "id=" + id +
                 ", name='" + name + '\'' +
                 '}';
    }
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Movie(Integer id, String name) {
        this.id = id;
        this.name = name;
    }
}
