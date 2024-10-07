package com.game.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

public final class ConfigLoader {

    private ConfigLoader() {
    }

    public static GameConfig loadConfig(String filePath) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), GameConfig.class);
    }
}
