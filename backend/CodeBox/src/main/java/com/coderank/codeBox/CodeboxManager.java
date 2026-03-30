package com.coderank.codeBox;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码沙箱管理者
 */
@Component
@RequiredArgsConstructor
public class CodeboxManager {

    private Map<String, AbstractCodeBox> codeboxMap = new HashMap<>();

    private final List<AbstractCodeBox> codeBoxList;

    private final ApplicationContext applicationContext;


    /**
     * 初始化所有类型代码沙箱
     */
    @PostConstruct
    public void init() {
        for (AbstractCodeBox codebox : codeBoxList) {
            String[] beanName = applicationContext.getBeanNamesForType(codebox.getClass());
            codeboxMap.put(beanName[0], codebox);
        }
    }

    //根据语言获取执行沙箱
    public AbstractCodeBox getByLanguage(String language) {
        if (language == null || language.isEmpty()) return null;
        AbstractCodeBox codebox = codeboxMap.get(language);
        return codebox == null ? null : codebox;
    }
}
