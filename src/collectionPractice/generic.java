package collectionPractice;

public class generic {
    public static void main(String[] args) {
        Time<String> time = new Time<>("Hello");
        String str = time.getValue();
        System.out.println(str.length());
        Time<Integer> time2 = new Time<>(123);
        Integer num = time2.getValue();
        System.out.println(num + 1);

        System.out.println(WeekEnum.TUESDAY + "-" + WeekCode.TUESDAY);
        System.out.println(WeekEnum.TUESDAY.ordinal() + "-" + WeekCode.TUESDAY.ordinal());
        System.out.println(WeekCode.TUESDAY.getCode());
        System.out.println(WeekCode.TUESDAY.getValue());
    }
}

class Time<T> {
    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Time(T value) {
        this.value = value;
    }
}

// 枚举本质上是一种类， 具有简洁、高效、安全、方便等特点
enum WeekEnum {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY;
}

enum WeekCode {
    MONDAY(1, "星期一"), TUESDAY(2, "星期二"), WEDNESDAY(3, "星期三"), THURSDAY(4, "星期四"), FRIDAY(5, "星期五"), SATURDAY(6, "星期六"), SUNDAY(7, "星期日");
    private final Integer code;
    private final String value;
    WeekCode(int code, String value) {
        this.code = code;
        this.value = value;
    }
    public Integer getCode() {
        return code;
    }
    public String getValue() {
        return value;
    }
}