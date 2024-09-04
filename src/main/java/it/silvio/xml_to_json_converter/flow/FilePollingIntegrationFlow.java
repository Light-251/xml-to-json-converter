package it.silvio.xml_to_json_converter.flow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class FilePollingIntegrationFlow {

    // TODO implementare poller per recuperare i file xml dalla cartella workflow/input
    @Bean
    IntegrationFlow  filePollingFlow() {
        return null;
    }
}
