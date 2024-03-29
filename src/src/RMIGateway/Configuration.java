package src.RMIGateway;

public class Configuration
{
    public static final String CREDENTIALS_FILE = "credentials.bin";
    public boolean DEBUG = true;

    // Special scenarios triggers
    public static boolean COLD_START = false;
    public static boolean AUTO_FAIL_BARRELS = false;
    public static boolean AUTO_FAIL_DOWNLOADERS = false;

    // IP addresses for RMI connections
    public static String gatewayIP = "localhost";
    public static String barrelsIP = "localhost";

    // TCP ports
    public static final int PORT_A = 8080;
    public static final int PORT_B = 8081;

    // Multicast port and address
    public static final int MULTICAST_PORT = 4000;
    public static final String MULTICAST_ADDRESS = "224.3.2.1";

    //Number of downloaders and barrels
    public static final int NUM_DOWNLOADERS = 3;
    public static final int NUM_BARRELS = 2;

    // Size of context in links
    public static final int CONTEXT_SIZE = 15;

    public static final int MAXIMUM_REFERENCE_LINKS = 10;

    public static final int PAGE_SIZE = 10;








}
