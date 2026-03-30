package com.coderank.modules.login;

import com.coderank.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 登录管理者
 */
@Component
@RequiredArgsConstructor
public class LoginManager {

    private Map<String, Login> LoginMap = new HashMap<>();

    private final List<Login> LoginList;
    private final ApplicationContext applicationContext;


    /**
     * 判断指定类型是否支持
     *
     * @param type
     * @return
     */
    public boolean is_exists(String type) {
        return LoginMap.containsKey(type);
    }

    /**
     * 获取指定登录类型对象
     *
     * @param type
     * @return
     */
    public Login getLogin(String type) {
        Login login = LoginMap.get(type);
        if (login == null) {
            throw new BusinessException("不支持当前登录类型");
        }
        return login;
    }

    /**
     * 获取所有登录类型
     *
     * @return
     */
    public Set<String> getAllLoginType() {
        return LoginMap.keySet();
    }

    /**
     * 初始化操作
     */
    @PostConstruct
    private void init() {
        for (Login login : LoginList) {
            String[] type = applicationContext.getBeanNamesForType(login.getClass());
            if (type.length > 0) {
                String beanName = type[0];
                if (beanName.endsWith("Login")) {
                    beanName = beanName.substring(0, beanName.length() - "Login".length());
                    LoginMap.put(beanName, login);
                }
            }
        }

        LoginMap.forEach((k,v) ->{
            System.out.println(k);
        });
    }
}
