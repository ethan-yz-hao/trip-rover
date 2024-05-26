package org.ethanhao.triprover;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("org.ethanhao.triprover.repository")
@EntityScan("org.ethanhao.triprover.domain")
public class TripRoverApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripRoverApplication.class, args);
	}

}
