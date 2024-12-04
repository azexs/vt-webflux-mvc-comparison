package pw.mgr.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.client.RestTemplate;


import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.NettyDataBufferFactory;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public DataBufferFactory dataBufferFactory() {
		return new NettyDataBufferFactory(ByteBufAllocator.DEFAULT);
	}

}
