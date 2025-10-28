package scrumpledpaper.agiler.auth.oauth2.userinfo;

import java.util.Map;

public class GitHubOAuth2UserInfo extends OAuth2UserInfo {

    public GitHubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        // GitHub email can be null if not public
        return (String) attributes.get("email");
    }

    @Override
    public String getImageUrl() {
        return (String) attributes.get("avatar_url");
    }
}
