package word.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by louis on 2016/6/7.
 */
public class ConfigManager {
    private Properties conf;
    private static ConfigManager instance;
    private ConfigManager() {
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    public String getString(String key) {
        return conf != null ? conf.getProperty(key) : null;
    }

    public Integer getInt(String key) {
        return conf != null ? (StringUtils.isNotBlank(conf.getProperty(key)) ? Integer
                .valueOf(conf.getProperty(key))
                : null)
                : null;
    }

    private void loadConfig() {
        try {
            conf=new Properties();
            InputStream input = this.getClass().getResourceAsStream("/config.properties");
            conf.load(input);
        } catch (Exception e) {
            final String msg = "can't load config file";
            throw new RuntimeException(msg);
        }
    }

    public static void main(String args[]) {
        System.out.println(ConfigManager.getInstance().getString("redis.url"));
        System.out.println(ConfigManager.getInstance().getInt("aab"));
    }
}
