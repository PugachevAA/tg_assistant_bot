package puga_tmsk.puga_bot.model;


import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity(name = "todoListItems")
public class ToDoListItem {

    @Id
    @GeneratedValue
    private long id;
    private long todoListId;
    private String title;
    private String link;
}
