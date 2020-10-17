package s04希尔排序;

import base.SortUtil;

import java.util.Arrays;


/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 希尔排序
 * @Author: Parker
 * @CreateTime: 2020-10-15 20:51
 * @Description: 希尔排序
 */
public class SortTest extends SortUtil{

    public static void main(String[] args) {
        SortTest sortTest = new SortTest();

        int[] randomArray = SortUtil.getRandomArray(1000);

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
        int h = 1;
        while (h <= array.length/3){
            h = h * 3 + 1;
        }

        for (int gap = h; gap > 0; gap = (gap - 1)/3 ) {
            for (int i = gap; i < array.length; i++) {
                for (int j = i; j > gap-1; j--) {
                    if(array[j] < array[j - gap]){
                        swap(array, j, j - gap);
                    }
                }
            }
        }


//        for (int gap = 0; gap < 4; gap++) {
//            for (int i = gap; i < array.length; i++) {
//                for (int j = i; j > gap-1; j--) {
//                    if(array[j] < array[j - gap]){
//                        swap(array, j, j - gap);
//                    }
//                }
//            }
//        }
        return array;
    }


}
