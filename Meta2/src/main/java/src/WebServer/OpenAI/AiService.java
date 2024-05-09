package src.WebServer.OpenAI;

import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AiService
{
   

    public List<String> generateText(final AiRequestDTO aiRequestDTO) {
        try {
            String token = System.getenv("OPENAI_API_KEY");
            System.out.println(token);
            OpenAiService openAiService = new OpenAiService(token);
            CompletionRequest completionRequest = CompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .prompt(aiRequestDTO.text())
                    .build();

            return openAiService
                    .createCompletion(completionRequest)
                    .getChoices()
                    .stream()
                    .map(CompletionChoice::getText)
                    .collect(toList());
        } catch(Exception exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
