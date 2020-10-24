package 队列转栈;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 队列转栈
 * @Author: Parker
 * @CreateTime: 2020-10-24 10:57
 * @Description: TODO
 */
public class TestMain {


    public static void main(String[] args) {
        MyStack<String> myQueue = new MyStack<>();
        myQueue.push("123");
        myQueue.push("456");
        myQueue.push("4561");
        myQueue.push("4562");
        myQueue.push("4563");
        myQueue.push("4564");


        int size = myQueue.size();
        for (int i = 0; i < size; i++) {
            System.out.println(myQueue.pull());
        }
    }


    public static class MyStack<T>{

        private Queue<T> q1;
        private Queue<T> q2;

        public MyStack() {
            q1 = new LinkedList<>();
            q2 = new LinkedList<>();
        }

        public void push(T t){
            q1.add(t);
        }

        public T pull(){
            // 剩下最后一个 其余的全部扔到 q2
            while ((q1.size()-1) > 0){
                q2.add(q1.poll());
            }

            // 将已有的 队列数据重新赋会去
            transferTo();

            // 得到队尾数据 - > 栈数据
            return q2.poll();
        }


        public int size(){
            return q1.size();
        }

        /**
         * 栈数据转移
         */
        private void transferTo(){
            Queue<T> tmp = q1;
            q1 = q2;
            q2 = tmp;
        }
    }
}
