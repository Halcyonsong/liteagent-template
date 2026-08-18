package io.github.halcyonsong.example.tool;

import io.github.halcyonsong.liteagent.core.tool.annotation.ToolComponent;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolMethod;
import io.github.halcyonsong.liteagent.core.tool.annotation.ToolParam;

/**
 * 示例工具组件，演示 {@code @ToolComponent} + {@code @ToolMethod} 注解用法。
 */
@ToolComponent
public class WeatherTools {

    @ToolMethod(
            name = "get_weather",
            description = "获取指定城市的当前天气信息，包括温度、天气状况等"
    )
    public String getWeather(
            @ToolParam(description = "城市名称，例如：北京") String city,
            @ToolParam(description = "温度单位，默认摄氏度", required = false) String unit
    ) {
        return city + "：晴，气温 28°C，湿度 45%" + (unit != null ? "（单位: " + unit + "）" : "");
    }

    @ToolMethod(
            name = "get_time",
            description = "获取指定时区的当前时间"
    )
    public String getTime(
            @ToolParam(description = "时区，例如：Asia/Shanghai", required = false) String timezone
    ) {
        return java.time.LocalTime.now() + (timezone != null ? " (" + timezone + ")" : "");
    }
}
