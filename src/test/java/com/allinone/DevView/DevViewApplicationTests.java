package com.allinone.DevView;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles; // 이 import 추가

@SpringBootTest
@ActiveProfiles("test") // 테스트 실행 시 application-test.yml 사용
class DevViewApplicationTests {

	@Test
	void contextLoads() {
	}

}
