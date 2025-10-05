package demo;


import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        Card c1 = new GoldCard("粤U1234", "张三", "13232323", 5000);
        Card c2 = new SilverCard("粤B2468", "李四", "13565343", 2000);
        Pay(c1);
        Pay(c2);
    }
    public static void Pay(Card c) {
        System.out.println("请输入金额：ccc");
        Scanner sc = new Scanner(System.in);
        double money = sc.nextDouble();
        c.pay(money);
        System.out.println("剩余金额：" + c.getMoney());
    }
}
