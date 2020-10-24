package 求栈最小值;

import java.util.Stack;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 求栈最小值
 * @Author: Parker
 * @CreateTime: 2020-10-24 11:51
 * @Description: TODO
 */
public class TestMain {

    public static void main(String[] args) {
        MyStack myStack = new MyStack();
        myStack.push(123);
        myStack.push(456);
        myStack.push(78);
        myStack.push(45);
        myStack.push(33);
        myStack.push(46);
        myStack.push(87989);

        myStack.printData();

        System.out.println(myStack.getMin());

        myStack.poll();
        myStack.poll();
        myStack.poll();
        myStack.poll();
        myStack.printData();
        System.out.println(myStack.getMin());

    }

    private static class MyStack {
        private final Stack<Integer> data = new Stack<>();
        private final Stack<Integer> minData = new Stack<>();

        public void push(int num){
            data.push(num);

            if(minData.isEmpty()){
                minData.push(num);
            }else{
                Integer peek = minData.peek();
                if(num < peek){
                    minData.push(num);
                }else{
                    minData.push(peek);
                }
            }
        }

        public int poll(){
            minData.pop();
            return data.pop();
        }

        public int getMin(){
            return minData.peek();
        }

        public void printData(){
            System.out.println("--------------");
            for (Integer datum : data) {
                System.out.print(datum+"，");
            }
            System.out.println();
            for (Integer datum : minData) {
                System.out.print(datum+"，");
            }
            System.out.println();
            System.out.println("--------------");
        }
    }

}
