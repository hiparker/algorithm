package s05归并排序;

import base.SortUtil;

import java.util.Arrays;


/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 归并排序1
 * @Author: Parker
 * @CreateTime: 2020-10-15 20:51
 * @Description: 归并排序1
 */
public class SortTest1 extends SortUtil{

    public static void main(String[] args) {
        SortTest1 sortTest = new SortTest1();

        int[] randomArray = {1, 4 , 7, 8, 3 , 6 ,9};

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
        return merge(array);
    }

    static int[] merge(int[] array){

        // 中间游标
        int mid = array.length / 2;

        int[] tmp = new int[array.length];
        int i = 0;
        int j = mid + 1;
        int k = 0;
        while (i <= mid && j < array.length){
            tmp[k++] = array[i] <= array[j] ? array[i++] : array[j++];
        }

        while (i <= mid){
            tmp[k++] = array[i++];
        }
        while (j < array.length){
            tmp[k++] = array[j++];
        }
        return tmp;
    }

}
