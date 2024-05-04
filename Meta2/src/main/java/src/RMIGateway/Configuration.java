package src.RMIGateway;

/**
 * Configuration file for app
 */
public class Configuration
{
    /**
     * Special scenarios trigger
     */
    public static boolean SABOTAGE_DOWNLOADERS = false;

    /**
     * IP addresses for RMI connections
     */
    public static String IP_GATEWAY = "localhost";

    /**
     * TCP port of URLQueue for sending URLS
     */
    public static final int SEND_PORT = 8080;
    /**
     * TCP ports of URLQueue for receiving URLS
     */
    public static final int RECEIVE_PORT = 8081;
    /**
     * Multicast port
     */
    public static final int MULTICAST_PORT = 4000;
    /**
     * Multicast address
     */
    public static final String MULTICAST_ADDRESS = "224.3.2.1";

    /**
     * Number of downloaders
     */
    public static final int NUM_DOWNLOADERS = 3;
    /**
     * Number of barrels
     */
    public static final int NUM_BARRELS = 3;

    /**
     * Number of allowed misses for random barrel access (it's best that it is not too low)
     */
    public static final int NUM_MISSES = 10;

    /**
     * Size of string variables that store the context about a webpage
     */
    public static final int CONTEXT_SIZE = 15;
    /**
     * Max number of reference links
     */
    public static final int MAX_REF_LINKS = 10;

}
