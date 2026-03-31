package com.coderank.modules.register;


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
 * 注册管理者
 */
@Component
@RequiredArgsConstructor
public class RegisterManager {

    private Map<String, Register> RegisterMap = new HashMap<>();

    private final List<Register> RegisterList;
    private final ApplicationContext applicationContext;

    /**
     * 判断指定类型是否支持
     *
     * @param type
     * @return
     */
    public boolean is_exists(String type) {
        return RegisterMap.containsKey(type);
    }

    /**
     * 获取指定注册类型对象
     *
     * @param type
     * @return
     */
    public Register getRegister(String type) {
        Register register = RegisterMap.get(type);
        if (register == null) {
            throw new BusinessException("不支持当前注册类型");
        }
        return register;
    }

    /**
     * 获取所有注册类型
     *
     * @return
     */
    public Set<String> getAllRegisterType() {
        return RegisterMap.keySet();
    }

    /**
     * 初始化操作
     */
    @PostConstruct
    private void init(){
        for (Register register : RegisterList) {
            String[] type = applicationContext.getBeanNamesForType(register.getClass());
            RegisterMap.put(type[0],register);
        }
    }

}
