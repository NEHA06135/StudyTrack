public class TestDB {
    public static void main(String[] args) {

        if (DBConnection.getConnection() != null) {
            System.out.println("SUCCESS ✅");
        } else {
            System.out.println("FAILED ❌");
        }

    }
}