package com.kkl.graffiti.common.util;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ArrayUtils {

   
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    public static final int INDEX_NOT_FOUND = -1;
    
    /**
     * 将一个对象数组转换为Map
     * 
     * @param array 对象数组，其元素可以为{@code Map.Entry<?, ?>}, 也可以为大小为2 的 {@code Object[]}
     * @return
     */
    public static Map<Object, Object> toMap(Object[] array) {
        if (array == null) {
            return null;
        }
        final Map<Object, Object> map = new HashMap<Object, Object>((int) (array.length * 1.5));
        for (int i = 0; i < array.length; i++) {
            Object object = array[i];
            if (object instanceof Map.Entry<?, ?>) {
                Map.Entry<?,?> entry = (Map.Entry<?,?>) object;
                map.put(entry.getKey(), entry.getValue());
            } else if (object instanceof Object[]) {
                Object[] entry = (Object[]) object;
                if (entry.length < 2) {
                    throw new IllegalArgumentException("Array element " + i + ", '"
                        + object
                        + "', has a length less than 2");
                }
                map.put(entry[0], entry[1]);
            } else {
                throw new IllegalArgumentException("Array element " + i + ", '"
                        + object
                        + "', is neither of type Map.Entry nor an Array");
            }
        }
        return map;
    }

    /**
     * 构造一个泛型数组
     * 
     * @param items 数组元素（可以没有），ie.
     * <pre>
     * String[] array = ArrayUtils.toArray("1", "2");
     * String[] emptyArray = ArrayUtils.&lt;String&gt;toArray();
     * </pre>
     * @return
     */
    public static <T> T[] toArray(final T... items) {
        return items;
    }
    
    public static String[] toStringArray(List<String> items) {
    	if(null == items) return null;
    	
    	String[] res = new String[items.size()];
    	for (int i = 0; i < items.size(); i++) {
			res[i] = items.get(i);
		}
    	
    	return res;
    }

    public static int[] toIntArray(List<Integer> items) {
    	if(null == items) return null;
    	
    	int[] res = new int[items.size()];
    	for (int i = 0; i < items.size(); i++) {
			res[i] = items.get(i).intValue();
		}
    	
    	return res;
    }

    /**
     * 在需要Object[]的地方将null 转化一个空的Object[],这样减少出错
     * @param array 对象数组
     * @return
     */
    public static Object[] nullToEmpty(Object[] array) {
        if (array == null || array.length == 0) {
            return EMPTY_OBJECT_ARRAY;
        }
        return array;
    }
    
    /**
     * 求取一个数组的某个范围的子数组
     * @param array 对象数组
     * @param startIndexInclusive 子数组的开始下标 inclusive
     * @param endIndexExclusive 子数组的结束下标 exclusive
     * @return
     */
    public static <T> T[] subarray(T[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }
        if (endIndexExclusive > array.length) {
            endIndexExclusive = array.length;
        }
        int newSize = endIndexExclusive - startIndexInclusive;
        Class<?> type = array.getClass().getComponentType();
        if (newSize <= 0) {
            @SuppressWarnings("unchecked") // OK, because array is of type T
            final T[] emptyArray = (T[]) Array.newInstance(type, 0);
            return emptyArray;
        }
        @SuppressWarnings("unchecked") // OK, because array is of type T
        T[] subarray = (T[]) Array.newInstance(type, newSize);
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }

    /**
     * 判断两个对象数组大小是否相等
     * @param array1
     * @param array2
     * @return
     */
    public static boolean isSameLength(Object[] array1, Object[] array2) {
        if ((array1 == null && array2 != null && array2.length > 0) ||
            (array2 == null && array1 != null && array1.length > 0) ||
            (array1 != null && array2 != null && array1.length != array2.length)) {
                return false;
        }
        return true;
    }

    /**
     * 求取对象数组的大小
     * @param array
     * @return
     */
    public static int getLength(Object array) {
        if (array == null) {
            return 0;
        }
        return Array.getLength(array);
    }

    /**
     * 对象数组顺序倒置
     * @param array
     * 
     */
    public static void reverse(Object[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        Object tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }
    
    /**
     * 找出目标对象在对象数组中第一次出现的位置，从第一个元素开始查找
     * 
     * @param array 对象数组
     * @param objectToFind 目标对象
     * @return -1表示目标对象不存在，否则为目标对象在数组的位置
     */
    public static int indexOf(Object[] array, Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }

    /**
     * 找出目标对象在对象数组中第一次出现的位置
     * 
     * @param array 对象数组
     * @param objectToFind 目标对象
     * @param startIndex 开始搜索的位置
     * @return -1表示目标对象不存在，否则为目标对象在数组的位置
     */
    public static int indexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }
 
    /**
     * 找出目标对象在对象数组中最后一次出现的位置，从最后一个元素开始查找
     * 
     * @param array 对象数组
     * @param objectToFind 目标对象
     * @return -1表示目标对象不存在，否则为目标对象在数组的位置
     */
    public static int lastIndexOf(Object[] array, Object objectToFind) {
        return lastIndexOf(array, objectToFind, Integer.MAX_VALUE);
    }

    /**
     * 找出目标对象在对象数组中最后一次出现的位置
     * 
     * @param array 对象数组
     * @param objectToFind 目标对象
     * @param startIndex 开始搜索的位置
     * @return -1表示目标对象不存在，否则为目标对象在数组的位置
     */
    public static int lastIndexOf(Object[] array, Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            return INDEX_NOT_FOUND;
        } else if (startIndex >= array.length) {
            startIndex = array.length - 1;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i >= 0; i--) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else if (array.getClass().getComponentType().isInstance(objectToFind)) {
            for (int i = startIndex; i >= 0; i--) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 判断对象数组是否包含目标对象
     * 
     * @param array 对象数组
     * @param objectToFind 目标对象
     * @return
     */
    public static boolean contains(Object[] array, Object objectToFind) {
        return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
    }

    /**
     * 判断对象数组是否为空，null 或元素个数为0都为空
     * 
     * @param array 对象数组
     * @return
     */
    public static boolean isEmpty(Object[] array) {
        return array == null || array.length == 0;
    }
  
}
