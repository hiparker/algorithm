package util;

import java.util.Random;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: PACKAGE_NAME
 * @Author: Parker
 * @CreateTime: 2020-10-15 22:54
 * @Description: 排序工具类
 */
public final class SortUtil {

    /**
     * 交换值
     * @param array
     * @param i
     * @param minPost
     */
    public static void swap(int[] array, int i, int minPost){
        int tmp = array[i];
        array[i] = array[minPost];
        array[minPost] = tmp;
    }

    /**
     * 打印
     * @param array
     */
    public static void printArray(int[] array){
        System.out.print("[");
        for (int i : array) {
            System.out.print(i + " , ");
        }
        System.out.print("]");
        System.out.println();
    }

    /**
     * 获得随机数
     * @param count
     * @return
     */
    public static int[] getRandomArray(int count){
        Random r = new Random();
        int[] array = new int[count];
        for (int i = 0; i < array.length; i++) {
            array[i]  =r.nextInt(count);
        }
        return array;
    }

    // ===========

    private SortUtil(){}
}
