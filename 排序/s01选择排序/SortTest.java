package s01选择排序;

import base.SortUtil;

import java.util.Arrays;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 选择排序
 * @Author: Parker
 * @CreateTime: 2020-10-15 20:51
 * @Description: 选择排序
 */
public class SortTest extends SortUtil{

    public static void main(String[] args) {
        SortTest sortTest = new SortTest();

        int[] nums = new int[]{5, 2, 4, 1, 3};
        // 排序
        int[] sort = sortTest.sort(Arrays.copyOf(nums, nums.length));
        // 打印最后排序内容
        SortUtil.printArray(sort);

        // 校验器
        boolean check = sortTest.check(nums);

        // 打印最后排序内容
        System.out.println(check);

    }


    @Override
    public int[] sort(int[] array) {
        for (int i = 0; i < array.length -1; i++) {
            int minPost = i;
            for (int j = i + 1; j < array.length; j++) {
                minPost = array[minPost] > array[j] ? j : minPost;
            }
            // 排序
            swap(array, i, minPost);
        }
        return array;
    }
}
