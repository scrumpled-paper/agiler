package scrumpledpaper.agiler.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import scrumpledpaper.agiler.auth.oauth2.CustomOAuth2User;
import scrumpledpaper.agiler.auth.oauth2.ProviderType;
import scrumpledpaper.agiler.auth.oauth2.userinfo.OAuth2UserInfo;
import scrumpledpaper.agiler.user.entity.User;
import scrumpledpaper.agiler.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;

	private static final long DEFAULT_IMAGE_ID = 1L;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		ProviderType providerType = ProviderType.from(registrationId);
		OAuth2UserInfo oAuth2UserInfo = providerType.getOAuth2UserInfo(oAuth2User.getAttributes());

		User user = userRepository.findByVendorAndVendorId(
						providerType,
						oAuth2UserInfo.getId()
				)
				.orElseGet(() -> createUser(oAuth2UserInfo, providerType));

		String nameAttributeKey = userRequest.getClientRegistration()
				.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		return new CustomOAuth2User(
				user.getId(),
				oAuth2User.getAuthorities(),
				oAuth2User.getAttributes(),
				nameAttributeKey
		);
	}

	private User createUser(OAuth2UserInfo userInfo, ProviderType providerType) {
		User newUser = User.builder()
				.email(userInfo.getEmail())
				.nickname(userInfo.getName())
				.vendor(providerType)
				.vendorId(userInfo.getId())
				.imageId(DEFAULT_IMAGE_ID)
				.build();

		return userRepository.save(newUser);
	}

}
