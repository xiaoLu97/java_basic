package serialize;

import java.io.Serializable;

public class User extends People implements Serializable {
    private Integer id;
    private String name;
    public String common;

    public User(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public void test() {
        System.out.println("test");
    }

    public void test(int id) {
        System.out.println("test" + id);
    }
}
