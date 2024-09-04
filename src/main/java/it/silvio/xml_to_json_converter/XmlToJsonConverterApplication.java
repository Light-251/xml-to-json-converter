package it.silvio.xml_to_json_converter;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class XmlToJsonConverterApplication {

	public static void main(String[] args) {
		SpringApplication.run(XmlToJsonConverterApplication.class, args);
	}

	/**
	 * Verifica della presenza delle cartelle di workflow
	 * e creazione se non sono presenti
	 */
	@PostConstruct
	public void createWorkflowFolder() {

		File workflowFolder = new File("workflow");
		File inputFolder = new File("workflow/input");
		File outputFolder = new File("workflow/output");
		File failedFolder = new File("workflow/failed");
		File processedFolder = new File("workflow/processed");

		if (!workflowFolder.exists())
			workflowFolder.mkdirs();

		if (!inputFolder.exists())
			inputFolder.mkdir();

		if (!outputFolder.exists())
			outputFolder.mkdir();

		if (!failedFolder.exists())
			failedFolder.mkdir();

		if (!processedFolder.exists())
			processedFolder.mkdir();

	}

}
