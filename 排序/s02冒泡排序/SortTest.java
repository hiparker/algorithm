package s02冒泡排序;

import base.SortUtil;

import java.util.Arrays;


/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 选择排序
 * @Author: Parker
 * @CreateTime: 2020-10-15 20:51
 * @Description: 冒泡排序
 */
public class SortTest extends SortUtil{

    public static void main(String[] args) {
        SortTest sortTest = new SortTest();

        int[] randomArray = SortUtil.getRandomArray(10);

        // 排序
        int[] sort = sortTest.sort(Arrays.copyOf(randomArray, randomArray.length));
        // 打印最后排序内容
        SortUtil.printArray(sort);

        // 校验器
        boolean check = sortTest.check(randomArray);

        // 打印最后排序内容
        System.out.println(check);
    }

    /**
     * 排序
     * @param array
     */
    @Override
    public int[] sort(int[] array){
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - 1 - i; j++) {
                if(array[j] > array[j+1]){
                    // 排序
                    swap(array, j, j+1);
                }
            }
        }
        return array;
    }


}
