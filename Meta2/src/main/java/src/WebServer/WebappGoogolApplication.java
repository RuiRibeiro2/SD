package src.WebServer;

import java.rmi.Naming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import retrofit2.http.PUT;
import src.RMIGateway.Configuration;
import src.RMIInterface.RMIGatewayInterface;
import src.WebServer.OpenAI.AiService;

@SpringBootApplication
public class WebappGoogolApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebappGoogolApplication.class, args);
	}

	@Bean
	public RMIGatewayInterface searchGateway() throws Exception
	{
		RMIGatewayInterface gateway = null;
		boolean connected = false;
		while(!connected)
		{
			try
			{
				gateway = (RMIGatewayInterface) Naming.lookup("rmi://"+ Configuration.IP_GATEWAY +"/gateway");
				connected = true;
			} catch (Exception e) {
				System.out.println("Error connecting to server, retrying in 3 seconds");
				Thread.sleep(3000);
			}
		}

		return gateway;
	}

	@Bean
	public AiService getAiServiceBean() throws Exception
	{
        return new AiService();
	}
}
