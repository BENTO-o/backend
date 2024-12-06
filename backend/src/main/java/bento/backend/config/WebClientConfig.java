package bento.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.beans.factory.annotation.Value;
import reactor.netty.http.client.HttpClient;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import io.netty.channel.ChannelOption;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

	@Value("${AI_SERVER_URL}")
	private String aiServerUrl;

	HttpClient httpClient() {
		return HttpClient.create()
			.option(ChannelOption.SO_KEEPALIVE, true)
			.option(ChannelOption.TCP_NODELAY, true)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 500000)
			.responseTimeout(Duration.ofMillis(500000))
			.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(500000, TimeUnit.MILLISECONDS))
				.addHandlerLast(new WriteTimeoutHandler(500000, TimeUnit.MILLISECONDS)));
	}

	@Bean
	public WebClient webClient() {
		return WebClient.builder()
			.baseUrl(aiServerUrl)
			.clientConnector(new ReactorClientHttpConnector(httpClient()))
			.build();
	}
}
