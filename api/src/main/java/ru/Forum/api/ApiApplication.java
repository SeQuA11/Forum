package ru.Forum.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "Forum API",
				version = "1.0",
				description = "API для форума с модерацией. Специальность 09.02.07"
		)
)
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner displayUrls(Environment env) {
		return args -> {
			String port = env.getProperty("server.port", "8080");
			String contextPath = env.getProperty("server.servlet.context-path", "");
			String hostAddress = "localhost";

			try {
				hostAddress = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				// Игнорируем, используем localhost
			}

			String localUrl = "http://localhost:" + port + contextPath;
			String networkUrl = "http://" + hostAddress + ":" + port + contextPath;

			System.out.println("\n");
			System.out.println("╔════════════════════════════════════════════════════════════╗");
			System.out.println("║          Forum API успешно запущен!                        ║");
			System.out.println("╚════════════════════════════════════════════════════════════╝");
			System.out.println();
			System.out.println("📄 Документация API:");
			System.out.println("   ├─ Swagger UI:        " + localUrl + "/swagger-ui.html");
			System.out.println("   ├─ OpenAPI JSON:      " + localUrl + "/v3/api-docs");
			System.out.println("   └─ Swagger JSON:      " + localUrl + "/v3/api-docs.yaml");
			System.out.println();
			System.out.println("🌐 Доступ по сети:");
			System.out.println("   └─ Network URL:       " + networkUrl);
			System.out.println();
			System.out.println("🔧 Endpoints:");
			System.out.println("   ├─ Auth:              " + localUrl + "/api/v1/auth");
			System.out.println("   ├─ Categories:        " + localUrl + "/api/v1/categories");
			System.out.println("   └─ Threads:           " + localUrl + "/api/v1/threads");
			System.out.println();
			System.out.println("💡 База данных:");
			System.out.println("   └─ URL:               " + env.getProperty("spring.datasource.url", "Not configured"));
			System.out.println();
			System.out.println("═══════════════════════════════════════════════════════════");
			System.out.println();
		};
	}
}