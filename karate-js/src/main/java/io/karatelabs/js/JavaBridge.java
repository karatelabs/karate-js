/*
 * The MIT License
 *
 * Copyright 2024 Karate Labs Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.karatelabs.js;

import net.minidev.json.JSONValue;

import java.lang.reflect.*;
import java.util.*;

public interface JavaBridge {

    default boolean typeExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    default Object construct(String className, Object[] args) {
        try {
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = findConstructor(clazz, args);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default Object invokeStatic(String className, String name, Object[] args) {
        try {
            Class<?> clazz = Class.forName(className);
            Method method = findMethod(clazz, name, args);
            if (method == null) {
                throw new RuntimeException("cannot find method [" + name + "] on class: " + clazz);
            }
            return invoke(null, method, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default Object invoke(Object object, String name, Object[] args) {
        try {
            Method method = findMethod(object.getClass(), name, args);
            if (method == null) {
                throw new RuntimeException("cannot find method [" + name + "] on object: " + object.getClass());
            }
            return invoke(object, method, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default Object getStatic(String className, String name) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
            Field field = clazz.getField(name);
            return field.get(null);
        } catch (Exception e) {
            if (clazz != null) {
                for (Method m : clazz.getMethods()) {
                    if (m.getName().equals(name)) {
                        JavaClass jc = new JavaClass(clazz);
                        return new JavaInvokable(name, jc);
                    }
                }
            }
            throw new RuntimeException(e);
        }
    }

    default void setStatic(String className, String name, Object value) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
            Field field = clazz.getField(name);
            field.set(null, value);
        } catch (Exception e) {
            if (clazz != null) {
                for (Method m : clazz.getMethods()) {
                    if (m.getName().equals(name)) {
                        JavaClass jc = new JavaClass(clazz);

                    }
                }
            }
            throw new RuntimeException(e);
        }
    }

    default Object get(Object object, String name) {
        Method method = findGetter(object, name);
        if (method == null) {
            try {
                Field field = object.getClass().getField(name);
                return field.get(object);
            } catch (Exception e) {
                for (Method m : object.getClass().getMethods()) {
                    if (m.getName().equals(name)) {
                        JavaObject jo = new JavaObject(object);
                        return new JavaInvokable(name, jo);
                    }
                }
                throw new RuntimeException("no instance property: " + name);
            }
        }
        try {
            return method.invoke(object, EMPTY);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default void set(Object object, String name, Object value) {
        String setterName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Object[] args = new Object[]{value};
        try {
            Method method = findMethod(object.getClass(), setterName, args);
            if (method == null) {
                return;
            }
            method.invoke(object, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //==================================================================================================================
    //
    static final Object[] EMPTY = new Object[0];

    static Class<?>[] paramTypes(Object[] args) {
        Class<?>[] paramTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            paramTypes[i] = arg == null ? Object.class : arg.getClass();
        }
        return paramTypes;
    }

    private static Object invoke(Object object, Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length > 0 && paramTypes[paramTypes.length - 1].equals(Object[].class)) {
            List<Object> argsList = new ArrayList<>();
            for (int i = 0; i < (paramTypes.length - 1); i++) {
                argsList.add(args[i]);
            }
            List<Object> lastArg = new ArrayList<>();
            for (int i = paramTypes.length - 1; i < args.length; i++) {
                lastArg.add(args[i]);
            }
            argsList.add(lastArg.toArray());
            return method.invoke(object, argsList.toArray());
        } else {
            return method.invoke(object, args);
        }
    }

    static Method findGetter(Object object, String name) {
        String getterSuffix = name.substring(0, 1).toUpperCase() + name.substring(1);
        Method method = findMethod(object.getClass(), "get" + getterSuffix, EMPTY);
        if (method == null) {
            method = findMethod(object.getClass(), "is" + getterSuffix, EMPTY);
        }
        return method;
    }

    static Constructor<?> findConstructor(Class<?> clazz, Object[] args) {
        try {
            return clazz.getConstructor(paramTypes(args));
        } catch (Exception e) {
            for (Constructor<?> constructor : clazz.getConstructors()) {
                Class<?>[] argTypes = constructor.getParameterTypes();
                if (match(argTypes, args)) {
                    return constructor;
                }
            }
        }
        throw new RuntimeException(clazz + " constructor not found, param types: " + Arrays.asList(paramTypes(args)));
    }

    public static Method findMethod(Class<?> clazz, String name, Object[] args) {
        try {
            return clazz.getMethod(name, paramTypes(args));
        } catch (Exception e) {
            for (Method method : clazz.getMethods()) {
                if (method.getName().equals(name)) {
                    Class<?>[] argTypes = method.getParameterTypes();
                    if (match(argTypes, args)) {
                        return method;
                    }
                }
            }
            return null;
        }
    }

    private static boolean match(Class<?>[] types, Object[] args) {
        for (int i = 0; i < types.length; i++) {
            if (i >= args.length) {
                return false;
            }
            Object arg = args[i];
            Class<?> argType = types[i];
            if (argType.isArray()) {
                if (arg instanceof List) {
                    // convert list to array of correct type
                    List<?> list = (List<?>) arg;
                    Class<?> arrayType = argType.getComponentType();
                    int count = list.size();
                    Object result = Array.newInstance(arrayType, count);
                    for (int j = 0; j < count; j++) {
                        Array.set(result, j, list.get(j));
                    }
                    args[i] = result;
                } else if (arg != null) { // nulls are ok
                    return false;
                }
                if (i == (types.length - 1)) { // var args
                    return true;
                }
                continue;
            }
            if (arg == null) {
                continue;
            }
            if (argType.equals(int.class) && arg instanceof Integer) {
                continue;
            }
            if (argType.equals(double.class) && arg instanceof Number) {
                continue;
            }
            if (argType.equals(long.class) && (arg instanceof Integer || arg instanceof Long)) {
                continue;
            }
            if (argType.equals(boolean.class) && arg instanceof Boolean) {
                continue;
            }
            if (argType.equals(byte.class) && arg instanceof Byte) {
                continue;
            }
            if (argType.equals(char.class) && arg instanceof Character) {
                continue;
            }
            if (argType.equals(float.class) && arg instanceof Number) {
                continue;
            }
            if (argType.equals(short.class) && arg instanceof Short) {
                continue;
            }
            if (!argType.isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return types.length == args.length;
    }

    @SuppressWarnings("unchecked")
    static Object toMap(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((k, v) -> result.put(k, toMap(v)));
            return result;
        }
        if (object instanceof String || object instanceof Number || object instanceof Boolean) {
            return object;
        }
        // using json-smart asm based java-bean unpacking
        String json = JSONValue.toJSONString(object);
        return JSONValue.parse(json);
    }

    static Object convertIfArray(Object o) {
        if (o != null && o.getClass().isArray()) {
            List<Object> list = new ArrayList<>();
            int count = Array.getLength(o);
            for (int i = 0; i < count; i++) {
                list.add(Array.get(o, i));
            }
            return list;
        } else {
            return o;
        }
    }

}
