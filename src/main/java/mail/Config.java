/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public final class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private static volatile Config instance;

    private Config() throws IOException {
        init();
    }

    public static Config getInstance() throws IOException {
        if (instance == null) {
            synchronized (Config.class) {
                if (instance == null) {
                    instance = new Config();
                }
            }
        }
        return instance;
    }

    private void init() throws IOException {
        LOG.info("Config load from properties = {}", System.getProperty("setting.path"));

        Properties properties = new Properties();
        properties.load(new InputStreamReader(new FileInputStream(System.getProperty("setting.path")), StandardCharsets.UTF_8));

        properties.stringPropertyNames().forEach((key) -> {
            String value = (String) properties.get(key);
            LOG.info("SYSPROPS: " + key + "=" + value);
            System.setProperty(key, value);
        });
    }
}
