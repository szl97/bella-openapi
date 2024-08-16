package com.ke.bella.openapi.configuration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;

import java.beans.Introspector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenapiBeanNameGenerator extends AnnotationBeanNameGenerator {
    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        String beanClassName = definition.getBeanClassName();
        if(beanClassName.startsWith("com.ke.bella.openapi.protocol") && beanClassName.endsWith("Adaptor")) {
            String[] strs = beanClassName.split("\\.");
            return Introspector.decapitalize(extractName(strs[strs.length - 1])) + "-" + strs[strs.length - 2];
        }
        return super.generateBeanName(definition, registry);
    }

    private static String extractName(String adaptorName) {
        String regex = "^([A-Za-z]+)Adaptor$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(adaptorName);
        if(matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
