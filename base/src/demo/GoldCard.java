package demo;

public class GoldCard extends Card{
    public GoldCard() {
        super();
    }
    public GoldCard(String cardnumber, String name, String number, double money) {
        super(cardnumber, name, number, money);
    }
//重写pay方法，享受8折优惠，消费满200提供打印免费洗车票的服务
    @Override
    public void pay(double money) {
        System.out.println("8折优惠，应支付：" + money * 0.8);
        if(getMoney()<money*0.8)
        {
            System.out.println("余额不足，请充值");
            return;
        }
        else
            setMoney(getMoney() - money*0.8);
        if(money*0.8 >= 200)
            System.out.println("提供打印免费洗车票的服务");
    }


}
