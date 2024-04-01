package src.RMIInterface;

import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

public interface RMIBarrelInterface extends Remote {
    List<String> searchWords(String word) throws IOException;
    List<String> searchLinks(String word) throws IOException;
}