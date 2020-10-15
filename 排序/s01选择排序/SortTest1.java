package s01选择排序;

import util.SortUtil;

import java.util.Arrays;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 选择排序
 * @Author: Parker
 * @CreateTime: 2020-10-15 20:51
 * @Description: 选择排序
 */
public class SortTest1 {

    public static void main(String[] args) {

        int[] randomArray = SortUtil.getRandomArray(10000);
        boolean check = check(randomArray);

        // 打印最后排序 结果
        System.out.println(check);

    }

    /**
     * 排序
     * @param array
     */
    public static void sort(int[] array){
        for (int i = 0; i < array.length -1; i++) {
            int minPost = i;
            for (int j = i + 1; j < array.length; j++) {
                minPost = array[minPost] > array[j] ? j : minPost;
            }
            // 排序
            SortUtil.swap(array, i, minPost);
        }
    }

    /**
     * 对数器
     * @return
     */
    public static boolean check(int[] array){
        boolean flag = true;

        // 核对1000次
        int count = 1000;
        for (int i = 0; i < count; i++) {
            // 拷贝第一个 array
            int[] array1 = new int[array.length];
            int[] array2 = new int[array.length];
            System.arraycopy(array, 0, array1, 0, array.length);
            System.arraycopy(array, 0, array2, 0, array.length);

            // 安全排序
            Arrays.sort(array1);

            // 自行排序
            sort(array2);

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
