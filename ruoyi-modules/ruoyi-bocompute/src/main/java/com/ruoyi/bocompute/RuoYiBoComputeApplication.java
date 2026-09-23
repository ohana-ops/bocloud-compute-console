package com.ruoyi.bocompute;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.ruoyi.common.security.annotation.EnableCustomConfig;
import com.ruoyi.common.security.annotation.EnableRyFeignClients;

/**
 * 算力资源管理模块
 * 
 * @author bocloud
 */
@EnableCustomConfig
@EnableRyFeignClients
@EnableScheduling
@SpringBootApplication
public class RuoYiBoComputeApplication
{
    /**
     * 算力资源管理模块启动入口
     *
     * @param args 启动参数
     */
    public static void main(String[] args)
    {
        SpringApplication.run(RuoYiBoComputeApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  算力资源管理模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
