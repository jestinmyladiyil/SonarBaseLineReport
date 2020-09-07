package it.sella.sonar.analyzer.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("it.sella.sonar")
public class SonarTestCoverageAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SonarTestCoverageAnalyzerApplication.class, args);
	}

}
