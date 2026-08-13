package io.github.halcyonsong.liteagent.core.tool.annotation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ToolParamTest {

    @Test
    void required_should_default_to_true() {
        assertTrue(WeatherTools.class.getDeclaredMethods()[0]
                .getParameters()[0]
                .getAnnotation(ToolParam.class)
                .required());
    }

    static class WeatherTools {

        @ToolMethod(name = "get_weather")
        public String getWeather(@ToolParam String city) {
            return city;
        }
    }
}