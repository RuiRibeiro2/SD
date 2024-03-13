package Gateway;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.util.List;
public interface RMIGatewayInterface extends Remote
{
    public List<String> searchWord(String word) throws FileNotFoundException, IOException;
    public List<String> searchPage(String link) throws FileNotFoundException, IOException;
}