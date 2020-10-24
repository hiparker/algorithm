package 栈转队列;

import java.util.Stack;

/**
 * @BelongsProject: algorithm
 * @BelongsPackage: 栈转队列
 * @Author: Parker
 * @CreateTime: 2020-10-24 10:57
 * @Description: TODO
 */
public class TestMain {


    public static void main(String[] args) {
        MyQueue<String> myQueue = new MyQueue<>();
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

    public static class MyQueue<T>{

        private final Stack<T> s1;
        private final Stack<T> s2;

        public MyQueue() {
            s1 = new Stack<>();
            s2 = new Stack<>();
        }

        public void push(T t){
            s1.push(t);
            transferTo();
        }

        public T pull(){
            transferTo();
            if(s2.isEmpty()){
                return null;
            }
            return s2.pop();
        }

        public int size(){
            return s1.size() + s2.size();
        }

        /**
         * 栈数据转移
         */
        private void transferTo(){
            if(s2.isEmpty()){
                while (!s1.isEmpty()){
                    T tmp = s1.pop();
                    s2.push(tmp);
                }
            }
        }
    }
}
