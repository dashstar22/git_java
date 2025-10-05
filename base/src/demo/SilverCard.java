package demo;

public class SilverCard extends Card{
    public SilverCard() {
        super();
    }

    public SilverCard(String CardNumber, String name, String number, double money) {
        super(CardNumber, name, number, money);
    }
    @Override
    public void pay(double money) {
        System.out.println("9折优惠，应支付：" + money * 0.9);
        if(getMoney()<money)
            System.out.println("余额不足，请充值");
        else
            setMoney(getMoney() - money*0.9);
    }
}
