public class Session {
    public static int userId = -1;
    public static String username = null;
    public static String role = null;

    public static void logout() {
        userId = -1;
        username = null;
        role = null;
    }
}
