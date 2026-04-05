package com.pz.beyond.util;

import com.pz.beyond.safeZoneRule.ISafeZoneRuleListener;
import com.pz.beyond.safeZoneRule.SafeZoneRule;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.util.ArrayList;
import java.util.List;

public class SafeZoneRuleUtil {
    private static final List<ISafeZoneRuleListener> RULES = new ArrayList<>();
    public static void initAutoRules() {
        // 获取我们自定义注解的全限定名，用于后续的匹配比较
        String annotationName = SafeZoneRule.class.getName();

        // 获取当前游戏中所有加载的模组的扫描数据 (这是 NeoForge 在启动前就已经扫好的数据，非常高效)
        for (ModFileScanData scanData : ModList.get().getAllScanData()) {

            // 遍历扫描数据中的每一个被注解标记的元素
            for (ModFileScanData.AnnotationData annotation : scanData.getAnnotations()) {

                // 判断当前扫描到的注解，是不是我们的 @SafeZoneRule
                if (annotation.annotationType().getClassName().equals(annotationName)) {
                    try {
                        // 获取被打了该注解的类的全限定名
                        String className = annotation.clazz().getClassName();
                        // 通过类名加载这个类 (Class.forName)
                        Class<?> clazz = Class.forName(className);

                        // 安全校验：确保这个类确实实现了 ISafeZoneRuleListener 接口
                        if (ISafeZoneRuleListener.class.isAssignableFrom(clazz)) {

                            // 利用反射调用无参构造函数，实例化这个规则对象
                            ISafeZoneRuleListener rule = (ISafeZoneRuleListener) clazz.getDeclaredConstructor().newInstance();

                            // 将实例化后的规则加入到我们的运行列表中
                            RULES.add(rule);
                            System.out.println("成功自动加载安全区规则: " + className);
                        }
                    } catch (Exception e) {
                        System.err.println("无法自动实例化安全区规则: " + annotation.clazz().getClassName());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static List<ISafeZoneRuleListener> getRULES() {
        return RULES;
    }
}
