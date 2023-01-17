package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity(name = "todolists")
public class ToDoList {

    @Id
    @GeneratedValue
    private long id;
    private long userId;
    private String title;
    private boolean addMode;

    public ToDoList(String title, long userId) {
        this.userId = userId;
        this.title = title;
        this.addMode = false;
    }


    public ToDoList() {
    }
}
