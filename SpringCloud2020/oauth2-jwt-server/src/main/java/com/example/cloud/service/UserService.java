package com.example.cloud.service;

import com.example.cloud.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dengzhiming
 * @date 2020/3/14 15:35
 */
@Service
public class UserService implements UserDetailsService {
    private List<User> userList;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        List<User> findUserList = this.userList.stream().filter(user -> user.getUsername().equals(s)).collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(findUserList)){
            return findUserList.get(0);
        }else {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
    }
    @PostConstruct
    public void initData() {
        userList = new ArrayList<>();
        userList.add(new User("macro", passwordEncoder.encode("123456"), AuthorityUtils.commaSeparatedStringToAuthorityList("admin")));
        userList.add(new User("andy", passwordEncoder.encode("123456"), AuthorityUtils.commaSeparatedStringToAuthorityList("client")));
        userList.add(new User("mark", passwordEncoder.encode("123456"), AuthorityUtils.commaSeparatedStringToAuthorityList("client")));
    }

}
