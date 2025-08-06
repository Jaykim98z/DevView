//package com.devview;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class DevViewApplication {
//
//	public static void main(String[] args) {
//		SpringApplication.run(DevViewApplication.class, args);
//	}
//
//}
package com.devview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class DevViewApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevViewApplication.class, args);
	}

}
