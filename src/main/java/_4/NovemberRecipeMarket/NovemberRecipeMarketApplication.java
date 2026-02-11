package _4.NovemberRecipeMarket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class NovemberRecipeMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(NovemberRecipeMarketApplication.class, args);
	}

}
