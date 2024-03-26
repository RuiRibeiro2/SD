public class Configuration {
    public static boolean AUTO_FAIL_DOWNLOADERS = false;

    // TCP ports
    public static final int PORT_A = 8082; // Had to change cause of spring-boot
    public static final int PORT_B = 8081;

    public static final int MULTICAST_PORT = 4000;
    public static final String MULTICAST_ADDRESS = "224.3.2.1";

    public static final int NUM_DOWNLOADERS = 2;

    public static final int MAXIMUM_REFERENCE_LINKS = 10;
}