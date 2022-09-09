package com.refact.myweco.common.util;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// db.yaml 변수를 인식하기 위한 Processor
public class YamlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String[] propertyUris = { "classpath*:custom/*.yml" };

    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    // propertyUris에 위치한 *.yml 파일을 load 한 뒤 각 yml 파일을 property로 추가
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try{
            List<Resource> resourceList = new ArrayList<>();
            for (String propertyUri : propertyUris) {
                resourceList.addAll(List.of(resourcePatternResolver.getResources(propertyUri)));
            }

            resourceList.stream().map(this::loadYaml).forEach(them -> {
                if (them != null) {
                    for (PropertySource<?> it : them) {
                        environment.getPropertySources().addLast(it);
                    }
                }
            });
        } catch (Exception e) {
            throw new BeanCreationException(e.getMessage(), e);
        }
    }

    private List<PropertySource<?>> loadYaml(Resource resource) {
        if(!resource.exists()){
            throw new IllegalArgumentException("Resource " + resource + "does not exist");
        }
        try{
            return loader.load(resource.getURL().toString(), resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load yaml configuration from " + resource, ex);
        }
    }
}
