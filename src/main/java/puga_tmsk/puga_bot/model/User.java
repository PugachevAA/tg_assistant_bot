package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Data
@Entity(name = "usersDataTable")
public class User {

    @Id
    private Long chatId;
    private String firstName;
    private String lastName;
    private String userName;
    private Timestamp registerTime;

    @Override
    public String toString() {
        return  "Имя='" + firstName + '\n' +
                "Фамилия='" + lastName + '\n' +
                "Логин='" + userName + '\n' +
                "Дата регистрации=" + registerTime;
    }

}
