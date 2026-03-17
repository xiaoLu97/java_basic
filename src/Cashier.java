public class Cashier {
    private Member member;
    
    public void setMember(Member member) {
        this.member = member;
    }

    public void settlement() {
        if (member != null) {
            System.out.println("开始结算");
            member.buyBook();
        } else {
            System.out.println("未设置会员，无法进行结算！");
        }
    }

    public static void property() {
        System.out.println("属性");
    }
}
