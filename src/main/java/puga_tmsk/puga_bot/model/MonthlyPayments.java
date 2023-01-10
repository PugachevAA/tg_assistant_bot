package puga_tmsk.puga_bot.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MonthlyPayments {

    private long id;
    private long userId;
    private String title;
    private BigDecimal price;
    private LocalDate payDate;
}
