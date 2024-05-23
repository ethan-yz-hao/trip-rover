package org.ethanhao.triprover;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.ethanhao.triprover.mapper")
public class TripRoverApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripRoverApplication.class, args);
	}

}
