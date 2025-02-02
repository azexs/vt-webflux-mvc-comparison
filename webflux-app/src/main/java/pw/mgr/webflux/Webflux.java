package pw.mgr.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBufferFactory;


import io.netty.buffer.ByteBufAllocator;
import org.springframework.core.io.buffer.NettyDataBufferFactory;


@SpringBootApplication
public class Webflux {

	public static void main(String[] args) {
		SpringApplication.run(Webflux.class, args);
	}

}
