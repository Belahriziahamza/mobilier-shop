package com.mobilier.shop.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig
                implements WebMvcConfigurer {

        private final String uploadDir;

        public WebConfig(
                        @Value("${app.upload-dir:uploads}") String uploadDir) {

                this.uploadDir = uploadDir;
        }

        @Override
        public void addResourceHandlers(
                        ResourceHandlerRegistry registry) {

                Path uploadDirectory = Paths.get(uploadDir)
                                .toAbsolutePath()
                                .normalize();

                String location = uploadDirectory
                                .toUri()
                                .toString();

                /*
                 * Important pour Spring :
                 * le chemin doit terminer par /
                 */

                if (!location.endsWith("/")) {

                        location += "/";
                }

                registry
                                .addResourceHandler(
                                                "/uploads/**")
                                .addResourceLocations(
                                                location);
        }
}