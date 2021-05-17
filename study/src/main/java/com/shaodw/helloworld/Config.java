package com.shaodw.helloworld;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author shaodw
 * @date 2021/5/5 16:41
 * @description
 */
@Configuration
@ComponentScan("com.shaodw.main1")
public class Config {
}
