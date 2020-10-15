package s01选择排序;

import util.SortUtil;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 选择排序
 * @Author: Parker
 * @CreateTime: 2020-10-15 20:51
 * @Description: 选择排序
 */
public class SortTest {

    public static void main(String[] args) {

        int[] nums = new int[]{5, 2, 4, 1, 3};


        for (int i = 0; i < nums.length -1; i++) {
            int minPost = i;
            for (int j = i + 1; j < nums.length; j++) {
                minPost = nums[minPost] > nums[j] ? j : minPost;
            }
            // 排序
            SortUtil.swap(nums, i, minPost);
            // 打印当前排序内容
            SortUtil.printArray(nums);
        }

        // 打印最后排序内容
        System.out.println();
        SortUtil.printArray(nums);

    }


}
