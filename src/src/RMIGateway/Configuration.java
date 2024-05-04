package src.RMIGateway;

public class Configuration
{
    // Special scenarios triggers
    public static boolean SABOTAGE_DOWNLOADERS = false;

    // IP addresses for RMI connections
    public static String IP_GATEWAY = "10.16.0.87";

    // TCP ports
    public static final int SEND_PORT = 8080;
    public static final int RECEIVE_PORT = 8081;

    // Multicast port and address
    public static final int MULTICAST_PORT = 4000;
    public static final String MULTICAST_ADDRESS = "224.3.2.1";

    //Number of downloaders and barrels
    public static final int NUM_DOWNLOADERS = 3;
    public static final int NUM_BARRELS = 3;

    // Size of context in links
    public static final int MAX_PAGE_DATA = 65534;

    public static final int NUM_MISSES = 10;
    public static final int CONTEXT_SIZE = 15;
    public static final int MAX_REF_LINKS = 10;








}
