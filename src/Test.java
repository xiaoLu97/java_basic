public class Test {
    public static void main(String[] args) {
        SuperMember sMember = new SuperMember();
        Cashier cashier = new Cashier();
        OrdinaryMember oMember = new OrdinaryMember();
        cashier.setMember(oMember);
        cashier.settlement();
        cashier.property();

        System.out.println("哈哈" == "哈哈"); // 字符串常量池
        System.out.println(new String("哈哈") == new String("哈哈")); // 比较引用地址
    }
}
