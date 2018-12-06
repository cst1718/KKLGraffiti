package com.kkl.graffiti.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * 反射工具类
 */

public final class ReflectUtil {

    private static Object operate(Object obj, String fieldName,
                                  Object fieldVal, String type) {
        Object ret = null;
        try {
            // 获得对象类型
            Class<?> classType = obj.getClass();
            // 获得对象的所有属性
            Field fields[] = classType.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.getName().equals(fieldName)) {

                    String firstLetter = fieldName.substring(0, 1)
                            .toUpperCase(Locale.getDefault()); // 获得和属性对应的getXXX()方法的名字
                    if ("set".equals(type)) {
                        String setMethodName = "set" + firstLetter
                                + fieldName.substring(1); // 获得和属性对应的getXXX()方法
                        Method setMethod = classType.getMethod(setMethodName,
                                                               new Class[]{field.getType()}); // 调用原对象的getXXX()方法
                        ret = setMethod.invoke(obj, new Object[]{fieldVal});
                    }
                    if ("get".equals(type)) {
                        String getMethodName = "get" + firstLetter
                                + fieldName.substring(1); // 获得和属性对应的setXXX()方法的名字
                        Method getMethod = classType.getMethod(getMethodName,
                                                               new Class[]{});
                        ret = getMethod.invoke(obj, new Object[]{});
                    }
                    return ret;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static Object getVal(Object obj, String fieldName) {
        return operate(obj, fieldName, null, "get");
    }

    public static void setVal(Object obj, String fieldName, Object fieldVal) {
        operate(obj, fieldName, fieldVal, "set");
    }

    private static Method getDeclaredMethod(Object object, String methodName,
                                            Class<?>[] parameterTypes) {
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                // superClass.getMethod(methodName, parameterTypes);
                return superClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                // Method 不在当前类定义, 继续向上转型
            }
        }

        return null;
    }

    private static void makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers())) {
            field.setAccessible(true);
        }
    }

    private static Field getDeclaredField(Object object, String filedName) {
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass
                .getSuperclass()) {
            try {
                return superClass.getDeclaredField(filedName);
            } catch (NoSuchFieldException e) {
                // Field 不在当前类定义, 继续向上转型
            }
        }
        return null;
    }

    public static Object invokeMethod(Object object, String methodName,
                                      Class<?>[] parameterTypes, Object[] parameters)
            throws InvocationTargetException {
        Method method = getDeclaredMethod(object, methodName, parameterTypes);

        if (method == null) {
            throw new IllegalArgumentException("Could not find method ["
                                                       + methodName + "] on target [" + object + "]");
        }

        method.setAccessible(true);

        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException e) {

        }

        return null;
    }

    public static void setFieldValue(Object object, String fieldName,
                                     Object value) {
        Field field = getDeclaredField(object, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("Could not find field ["
                                                       + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Object getFieldValue(Object object, String fieldName) {
        Field field = getDeclaredField(object, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field ["
                                                       + fieldName + "] on target [" + object + "]");
        }

        makeAccessible(field);

        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }


    /******************************************************************************************************************/
    /**
     * 获取类里指定的变量
     *
     * @param thisClass
     * @param fieldName
     * @return
     */
    public static Field getField(Class<?> thisClass, String fieldName) {
        if (thisClass == null) {
            return null;
        }

        try {
            return thisClass.getDeclaredField(fieldName);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 获取对象里变量的值
     *
     * @param instance
     * @param fieldName
     * @return 返回空则可能值不存在，或变量不存在
     */
    public static Object getValue(Object instance, String fieldName) {
        Field field = getField(instance.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        // 参数值为true，禁用访问控制检查
        field.setAccessible(true);
        try {
            return field.get(instance);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 获取静态变量的值
     *
     * @param clazz
     * @param fieldName
     * @return 返回空则可能值不存在，或变量不存在
     */
    public static Object getValue(Class clazz, String fieldName) {
        Field field = getField(clazz, fieldName);
        if (field == null) {
            return null;
        }
        // 参数值为true，禁用访问控制检查
        field.setAccessible(true);
        try {
            return field.get(null);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 获取类里的方法
     *
     * @param thisClass
     * @param methodName
     * @param parameterTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<?> thisClass, String methodName, Class<?>[] parameterTypes) {
        if (thisClass == null) {
            return null;
        }

        try {
            Method method = thisClass.getDeclaredMethod(methodName, parameterTypes);
            if (method == null) {
                return null;
            }
            method.setAccessible(true);
            return method;
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 执行对象里的方法
     *
     * @param instance
     * @param methodName
     * @param args       方法参数
     * @return 返回值
     * @throws Throwable 方法不存在或者执行失败跑出异常
     */
    public static Object invokeMethod(Object instance, String methodName, Object... args) throws Throwable {
        Class<?>[] parameterTypes = null;
        if (args != null) {
            parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    parameterTypes[i] = args[i].getClass();
                }
            }
        }
        Method method = getMethod(instance.getClass(), methodName, parameterTypes);
        return method.invoke(instance, args);
    }

    /**
     * 执行静态方法
     *
     * @param clazz
     * @param methodName
     * @param args       方法参数
     * @return 返回值
     * @throws Throwable 方法不存在或者执行失败跑出异常
     */
    public static Object invokeMethod(Class clazz, String methodName, Object... args) throws Throwable {
        Class<?>[] parameterTypes = null;
        if (args != null) {
            parameterTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    parameterTypes[i] = args[i].getClass();
                }
            }
        }
        Method method = getMethod(clazz, methodName, parameterTypes);
        return method.invoke(clazz, args);
    }

}
