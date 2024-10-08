package org.ethanhao.triprover.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class LoginUser implements UserDetails, OAuth2User {

    private User user;

    private List<String> permissions;

    private Map<String, Object> attributes;

    // Constructor for normal login
    public LoginUser(User user, List<String> list) {
        this.user = user;
        this.permissions = list;
    }

    // Constructor for OAuth2 login
    public LoginUser(User user, List<String> permissions, Map<String, Object> attributes) {
        this.user = user;
        this.permissions = permissions;
        this.attributes = attributes;
    }

    @JSONField(serialize = false) // ignore this field when converting to JSON
    private List<SimpleGrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // avoid duplicate conversion
        if (authorities != null){
            return authorities;
        }

        // Convert string type permission information in permissions to GrantedAuthority object and store it in authorities
        authorities = permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // OAuth2User interface method
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return user.getUserName();
    }
}
