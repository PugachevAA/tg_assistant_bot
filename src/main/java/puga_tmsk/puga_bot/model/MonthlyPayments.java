package puga_tmsk.puga_bot.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity(name = "monthlyPayments")
public class MonthlyPayments {

    @Id
    @GeneratedValue
    private long id;
    private long userId;
    private String title;
    private BigDecimal price;
    private LocalDate payDate;
    private boolean addFinish;
}
