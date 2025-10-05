package demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //getter,setter,toString,equals,hashCode
@NoArgsConstructor
@AllArgsConstructor

public class Card {
    private String cardnumber;
    private String name;
    private String number;
    private double money;

    public void pay(double money){
        this.money -= money;
    }
    public void charge(double money){
        this.money += money;
    }
}
