package src.Barrels;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.util.List;

public interface BarrelInterface extends Remote {
    public List<String> searchWords(String word) throws FileNotFoundException, IOException;
    public List<String> searchLinks(String word) throws FileNotFoundException, IOException;
}