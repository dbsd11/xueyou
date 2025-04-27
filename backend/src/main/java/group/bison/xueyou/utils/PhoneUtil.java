package group.bison.xueyou.utils;

public class PhoneUtil {

    public static String encrypt(String phoneNumber) {
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(7);
    }
}
