package zerobase.demo.error;

public class InvalidDate extends RuntimeException{
    public static final String MESSAGE = "날짜를 잘못 입력하셨습니다.";

    public InvalidDate() {
        super(MESSAGE);
    }

}
