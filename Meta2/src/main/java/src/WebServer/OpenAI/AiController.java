package src.WebServer.OpenAI;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.theokanning.openai.service.OpenAiService;

import java.util.List;

@AllArgsConstructor
@RequestMapping(value = "/open-ai/")
@RestController
public class AiController
{
    private AiService aiService;

    @PostMapping("/generate-text")
    public List<String> generateText(@RequestBody AiRequestDTO aiRequestDTO) {
        return aiService.generateText(aiRequestDTO);
    }
}
