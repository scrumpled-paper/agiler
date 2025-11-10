package scrumpledpaper.agiler;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import scrumpledpaper.agiler.annotation.IntegrationTest;
import scrumpledpaper.agiler.common.TestDataFactory;
import scrumpledpaper.agiler.image.entity.Image;
import scrumpledpaper.agiler.user.entity.User;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TestDataFactory testDataFactory;

    @Test
    @DisplayName("공개 엔드포인트는 인증 없이 접근 가능해야 한다")
    void publicEndpointShouldBeAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/health")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("보호된 엔드포인트는 토큰 없이 접근 시 401 Unauthorized를 반환해야 한다")
    void protectedEndpointShouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("보호된 엔드포인트는 유효하지 않은 토큰으로 접근 시 401 Unauthorized를 반환해야 한다")
    void protectedEndpointShouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer invalid.token.string")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("보호된 엔드포인트는 유효한 토큰으로 접근 시 200 OK를 반환해야 한다")
    void protectedEndpointShouldReturn200WithValidToken() throws Exception {
        // Given
		Image image = testDataFactory.createDefaultImage();
		User user = testDataFactory.createUser(image.getId());
        String cookie = testDataFactory.createAccessToken(user);

        // When & Then
        mockMvc.perform(get("/api/v1/users")
						.cookie(new Cookie("accessToken", cookie))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

}
