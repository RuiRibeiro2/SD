package src.RMIInterface;

import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

public interface RMIBarrelInterface extends Remote
{
    /**
     * Retrieves all urls that are connected to a word or a set of words
     * @param word Word input from a client
     * @return List of urls related to the client input
     * @throws IOException
     */
    List<String> searchWords(String word) throws IOException;

    /**
     * Retrieves all webpages that link to a webpage provided by client input
     * @param webpage Webpage input from a client
     * @return List of webpages related to the client input
     * @throws IOException
     */
    List<String> searchLinks(String webpage) throws IOException;
}