package base;

import java.util.Arrays;
import java.util.Random;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: PACKAGE_NAME
 * @Author: Parker
 * @CreateTime: 2020-10-15 22:54
 * @Description: 排序工具类
 */
public abstract class SortUtil {


    /**
     * 排序
     */
    public abstract int[] sort(int[] array);

    /**
     * 交换值
     * @param array
     * @param i
     * @param j
     */
    protected void swap(int[] array, int i, int j){
        int tmp = array[i];
        array[i] = array[j];
        array[j] = tmp;
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

    /**
     * 对数器
     * @return
     */
    public boolean check(int[] array){
        boolean flag = true;

        // 拷贝第一个 array
        int[] array1 = new int[array.length];
        int[] array2 = new int[array.length];
        System.arraycopy(array, 0, array1, 0, array.length);
        // 安全排序
        Arrays.sort(array1);
        System.out.println("原始排序");
        printArray(array);
        System.out.println("正确排序");
       printArray(array1);

        // 核对1000次
        int count = 1000;
        for (int i = 0; i < count; i++) {
            System.arraycopy(array, 0, array2, 0, array.length);

            // 自行排序
            array2 = sort(array2);

            // 校验是否相等
            for (int j = 0; j < array1.length; j++) {
                if(array1[j] != array2[j]){
                    flag = false;
                    break;
                }
            }

        }
        return flag;
    }

}
