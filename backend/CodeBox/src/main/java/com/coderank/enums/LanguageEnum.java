package com.coderank.enums;


import java.util.ArrayList;
import java.util.List;

/**
 * 语言类型枚举
 */
public enum LanguageEnum {

    JAVA("java","java"),
    PYTHON("python","py");


    // 判断语言是否支持
    public static boolean isSupported(String language) {
        for (LanguageEnum lang : values()) {
            if (lang.language.equals(language)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有语言
     * @return
     */
    public static List<String> getAllLanguages(){
        List<String> list = new ArrayList<>();
        for (LanguageEnum language : values()){
            list.add(language.language);
        }
        return list;
    }
    /**
     * 通过语言获取文件后缀
     * @param language
     * @return
     */
    public static String getSuffixByLanguage(String language) {
        for (LanguageEnum lang : values()) {
            if (lang.language.equals(language)) {
                return lang.suffix;
            }
        }
        return null;
    }

    /**
     * 语言
     */
    private final String language;

    /**
     * 文件后缀
     */
    private final String suffix;

    LanguageEnum(String language, String suffix) {
        this.language = language;
        this.suffix = suffix;
    }


    public String getLanguage() {
        return language;
    }

    public String getSuffix() {
        return suffix;
    }
}
