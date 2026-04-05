package org.biotech.api.util;

import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.biotech.api.system.gene.core.IGene;
import org.biotech.api.system.trait.core.ITrait;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 模组插件扫描器 - 支持 @AutoInit 注解
 */
public class ModPluginFinder {
    private static final Logger LOGGER = LogManager.getLogger();
    
    private ModPluginFinder() {
        // 工具类，禁止实例化
    }

    /**
     * 获取所有标记为自动注册的基因
     * 扫描 @AutoInit(type = GENE) 注解
     */
    public static List<IGene> getModPlugins() {
        return getInstances(AutoInit.class, AutoInit.InitType.GENE, IGene.class);
    }

    /**
     * 获取所有标记为自动注册的词条
     * 扫描 @AutoInit(type = TRAIT) 注解
     */
    public static List<ITrait> getModTraits() {
        return getInstances(AutoInit.class, AutoInit.InitType.TRAIT, ITrait.class);
    }

    /**
     * 扫描并实例化带有 @AutoInit 注解的类
     * 
     * @param autoInitAnnotation @AutoInit 注解类
     * @param initType           @AutoInit 的 type 值
     * @param instanceClass      实例类型
     * @param <T>                实例类型
     */
    @SuppressWarnings("unchecked")
    private static <T> List<T> getInstances(
            Class<? extends Annotation> autoInitAnnotation,
            AutoInit.InitType initType,
            Class<T> instanceClass) {
        
        Set<String> classNames = new LinkedHashSet<>();
        
        // 扫描 @AutoInit 注解并过滤指定 type
        Type annotationType = Type.getType(autoInitAnnotation);
        scanAutoInitAnnotations(annotationType, initType, classNames);
        
        // 实例化所有找到的类
        List<T> instances = new ArrayList<>();
        for (String className : classNames) {
            try {
                Class<?> asmClass = Class.forName(className, false, ModPluginFinder.class.getClassLoader());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                Constructor<? extends T> constructor = asmInstanceClass.getDeclaredConstructor();
                T instance = constructor.newInstance();
                instances.add(instance);
            } catch (ReflectiveOperationException | LinkageError e) {
                LOGGER.error("Failed to load: {}", className, e);
            }
        }
        return instances;
    }

    /**
     * 扫描 @AutoInit 注解并过滤指定 type
     */
    private static void scanAutoInitAnnotations(
            Type autoInitType,
            AutoInit.InitType targetType,
            Set<String> classNames) {
        int typeMatched = 0;
        int predicateMatched = 0;
        List<ModFileScanData> allScanData = ModList.get().getAllScanData();
        for (ModFileScanData scanData : allScanData) {
            for (ModFileScanData.AnnotationData a : scanData.getAnnotations()) {
                if (Objects.equals(a.annotationType(), autoInitType) && a.targetType() == ElementType.TYPE) {
                    typeMatched++;
                    if (matchesInitType(a.annotationData(), targetType)) {
                        predicateMatched++;
                        // 按示例优先使用 memberName，兼容不同扫描器输出格式
                        String className = normalizeClassName(a.memberName());
                        if (className == null || className.isBlank()) {
                            className = normalizeClassName(a.clazz().getClassName());
                        }
                        if (className != null && !className.isBlank()) {
                            classNames.add(className);
                        }
                    }
                }
            }
        }
        LOGGER.info("AutoInit scan for {}: typeMatched={}, predicateMatched={}, classes={}",
                targetType, typeMatched, predicateMatched, classNames.size());
    }

    private static boolean matchesInitType(Map<String, Object> annotationData, AutoInit.InitType targetType) {
        if (annotationData == null) {
            return false;
        }

        Object typeValue = annotationData.get("type");
        if (typeValue == null) {
            return false;
        }

        String enumName = extractEnumName(typeValue);
        if (enumName == null || enumName.isBlank()) {
            return false;
        }

        return targetType.name().equals(enumName);
    }

    private static String extractEnumName(Object enumHolder) {
        if (enumHolder instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        // 兼容 ModAnnotation.EnumHolder 等类型
        for (String methodName : new String[]{"value", "getValue", "enumValue", "name", "getName"}) {
            try {
                Method method = enumHolder.getClass().getMethod(methodName);
                Object value = method.invoke(enumHolder);
                if (value != null) {
                    String normalized = normalizeEnumName(String.valueOf(value));
                    if (normalized != null) {
                        return normalized;
                    }
                }
            } catch (ReflectiveOperationException ignored) {
                // 尝试下一个方法
            }
        }

        return normalizeEnumName(String.valueOf(enumHolder));
    }

    private static String normalizeEnumName(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();
        int slash = value.lastIndexOf('/');
        if (slash >= 0 && slash + 1 < value.length()) {
            value = value.substring(slash + 1);
        }
        int dot = value.lastIndexOf('.');
        if (dot >= 0 && dot + 1 < value.length()) {
            value = value.substring(dot + 1);
        }
        return value;
    }

    private static String normalizeClassName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return name.replace('/', '.');
    }
}
