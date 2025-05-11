package com.playus.userservice.domain.oauth.dto;

import com.playus.userservice.domain.user.dto.UserDto;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.util.*;

@Getter
public class CustomOAuth2User implements OAuth2User {

    private final UserDto userDto;

    public CustomOAuth2User(UserDto userDto) {
        this.userDto = userDto;
    }

    @Override
    public Map<String, Object> getAttributes() {
        // nickname은 사용자가 나중에 설정할 수도 있으므로 null 가능성 고려
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", userDto.getId());
        //attributes.put("nickname", userDto.getNickname());
        attributes.put("birth", userDto.getBirth());
        attributes.put("gender", userDto.getGender());
        attributes.put("role", userDto.getRole());
        attributes.put("authProvider", userDto.getAuthProvider());
        attributes.put("activated", userDto.isActivated());
        attributes.put("userScore", userDto.getUserScore());
        attributes.put("thumbnailURL", userDto.getThumbnailURL());

        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of((GrantedAuthority) () -> userDto.getRole().name());
    }

    @Override
    public String getName() {
        return String.valueOf(userDto.getId()); // 이거빼면 OAuth2User implements 에서 에러
    }

}
