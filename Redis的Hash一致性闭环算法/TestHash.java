
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created Date by 2019/12/27 0027.
 * 模拟 一致性hash算法 前半部分 redis 分布区域选择
 * 这里用随机数代替hash
 *
 * @author Parker
 */
public class TestHash {

    private static String default2 = "default2";
    /** 生成所有指令 */
    private static Map<String, Map<String, String>> instructions = new HashMap<>();
    /**上下文指令 */
    private static Stack<String> contextKeys = new Stack<>();
    /**上下文指令 */
    private static Stack<String> instructionKeys = new Stack<>();
    /** redis 集合 */
    private static Map<Integer, String> redisMap = new TreeMap<>(new Comparator<Integer>() {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
        }
    });
    /** redis 仓库 */
    private static Map<String, List<Integer>> redisRepository = new HashMap<>();


    /**
     * 静态块
     */
    static{
        contextKeys.add("system");
        instructions = TestHash.createInstructions();

        // 添加三台redis服务器 在一个闭环
        TestHash.createRedisMap("xxoo1-redis",1456);
        TestHash.createRedisMap("xxoo2-redis",5437);
        TestHash.createRedisMap("xxoo3-redis",8524);

        // 初始化数据
        TestHash.addNum(1000);
        TestHash.addNum(2000);
        TestHash.addNum(5000);
        TestHash.addNum(6000);
        TestHash.addNum(7000);
        TestHash.addNum(8000);
        TestHash.addNum(10000);
        TestHash.addNum(15000);
        TestHash.addNum(20000);
        TestHash.addNum(25000);
        TestHash.addNum(40000);
    }

    public static void main(String[] args) {
        System.out.println("欢迎使用 redis一致性hash算法 模拟系统 ！");
        System.out.println();


        // 输入检测
        TestHash.invokeScanner(contextKeys,null,false);
    }


    /**
     * 生成Redis 仓库
     *
     * 生成仓库 则 锁定类 这里用 synchronized 代替
     * ☆☆☆☆☆☆☆☆☆☆☆☆☆
     *  现实环境中 需要￿做分布式锁 避免在迁移数据过程中 又有新的数据 做改动 ****** ☆
     * @return
     */
    public synchronized static void createRedisMap(String redisCode,Integer num) {
        Set<Integer> integers = redisMap.keySet();
        Iterator<Integer> iterator = integers.iterator();
        List<Integer> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        // 调用闭环算法
        Integer key = null;
        if(keys != null && !keys.isEmpty()){
            key = TestHash.closedLoopAlgorithm(num,keys);
        }
        // 根据 闭环算法 获得数据
        List<Integer> datas = null;
        if(null != key){
            datas = redisRepository.get(redisMap.get(key));
        }

        // 创建集合
        List<Integer> list = new ArrayList<>();
        redisRepository.put(redisCode,list);
        redisMap.put(num,redisCode);

        // 因为上方有新增redis 获取 新的 keys
        integers = redisMap.keySet();
        iterator = integers.iterator();
        List<Integer> keysT = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keysT.add(iterator.next());
        }


        // 数据迁移
        if(datas != null && !datas.isEmpty()){
            List<Integer> newDatas = new ArrayList<>();
            // 循环所有数据 查找出 要存放在新容器里的数据
            for (Integer data : datas) {
                // 调用闭环算法
                Integer keyT = TestHash.closedLoopAlgorithm(data,keysT);
                if(keyT.equals(num)){
                    newDatas.add(data);
                }
            }

            // 删除 旧数据
            for (Integer newData : newDatas) {
                // 传入keys 因为 keys已做更改 传入旧keys
                TestHash.deleteOldData(newData,keys);
            }
            // 添加新数据
            List<Integer> newQueue = redisRepository.get(redisMap.get(num));
            for (Integer newData : newDatas) {
                newQueue.add(newData);
            }
        }

    }

    /**
     * 生成Redis 数据
     * @return
     */
    public static void addNum(Integer num) {
        Integer key = 0;
        Set<Integer> integers = redisMap.keySet();
        Iterator<Integer> iterator = integers.iterator();
        List<Integer> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        // 调用闭环算法
        key = TestHash.closedLoopAlgorithm(num,keys);

        List<Integer> datas = redisRepository.get(redisMap.get(key));
        datas.add(num);

        List<Integer> nums = new ArrayList<>();
        for (Integer integer : keys) {
            if(redisRepository.get(redisMap.get(integer)) != null){
                nums.addAll(redisRepository.get(redisMap.get(integer)));
            }
        }

        System.out.println();
        System.out.println("当前数据：");
        System.out.println(nums.toString());
        for (Integer integer : keys) {
            if(redisRepository.get(redisMap.get(integer)) != null){
                System.out.println(redisMap.get(integer)+":"+redisRepository.get(redisMap.get(integer)).toString());
            }
        }
        System.out.println();
    }

    /**
     * 查看Redis 所有数据
     * @return
     */
    public static void findAll() {
        Set<Integer> integers = redisMap.keySet();
        Iterator<Integer> iterator = integers.iterator();
        List<Integer> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });


        List<Integer> nums = new ArrayList<>();
        for (Integer integer : keys) {
            if(redisRepository.get(redisMap.get(integer)) != null){
                nums.addAll(redisRepository.get(redisMap.get(integer)));
            }
        }

        System.out.println();
        System.out.println("当前数据：");
        System.out.println(nums.toString());
        for (Integer integer : keys) {
            if(redisRepository.get(redisMap.get(integer)) != null){
                System.out.println(redisMap.get(integer)+":"+redisRepository.get(redisMap.get(integer)).toString());
            }
        }
        System.out.println();
    }

    /**
     * 删除Redis数据
     * @return
     */
    public static void deleteData(Integer num) {
        Integer key = 0;
        Set<Integer> integers = redisMap.keySet();
        Iterator<Integer> iterator = integers.iterator();
        List<Integer> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        // 调用闭环算法
        key = TestHash.closedLoopAlgorithm(num,keys);

        List<Integer> datas = redisRepository.get(redisMap.get(key));
        boolean deFlag = false;
        int count = 0;
        for (int i = 0; i < datas.size(); i++) {
            if(num.equals(datas.get(i))){
                deFlag = true;
                count = i;
                break;
            }
        }
        if(deFlag){
            datas.remove(count);
        }


        List<Integer> nums = new ArrayList<>();
        for (Integer integer : keys) {
            if(redisRepository.get(redisMap.get(integer)) != null){
                nums.addAll(redisRepository.get(redisMap.get(integer)));
            }
        }

        System.out.println();
        System.out.println("当前数据：");
        System.out.println(nums.toString());
        for (Integer integer : keys) {
            if(redisRepository.get(redisMap.get(integer)) != null){
                System.out.println(redisMap.get(integer)+":"+redisRepository.get(redisMap.get(integer)).toString());
            }
        }
        System.out.println();
    }

    /**
     * 删除Redis数据
     * @return
     */
    public synchronized static void deleteOldData(Integer num,List<Integer> keys) {
        Integer key = 0;

        //正序
        keys.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });

        // 调用闭环算法
        key = TestHash.closedLoopAlgorithm(num,keys);

        List<Integer> datas = redisRepository.get(redisMap.get(key));
        boolean deFlag = false;
        int count = 0;
        for (int i = 0; i < datas.size(); i++) {
            if(num.equals(datas.get(i))){
                deFlag = true;
                count = i;
                break;
            }
        }
        if(deFlag){
            datas.remove(count);
        }


        List<Integer> nums = new ArrayList<>();
        for (Integer integer : keys) {
            if(redisRepository.get(redisMap.get(integer)) != null){
                nums.addAll(redisRepository.get(redisMap.get(integer)));
            }
        }
    }

    /**
     * 闭环算法 核心算法
     * ☆☆☆☆☆☆☆☆☆☆
     * @param num
     * @param keys
     * @return
     */
    public static Integer closedLoopAlgorithm(Integer num,List<Integer> keys){
        Integer key = 0;
        if(num > keys.get(keys.size()-1) || num <= keys.get(0)){
			key = keys.get(0);
		}else {
			for (int i = keys.size() - 2; i >= 0; i--) {
				if(num > keys.get(i)){
					key = keys.get(i+1);
					break;
				}
			}
		}
        return key;
    }


    /**
     * 下方代码 是输入 相关 与 一致性算法无关
     */
    /*-----------------------------------------------------------------------------------------------------------------------------------------------*/

    /**
     * 创建随机数
     * @param
     */
    public static int createRandom(){
        // 最大数
        Integer max = 10000;
        return new Random().nextInt(max);
    }

    /**
     * 输入方案
     */
    public static void invokeScanner(Stack<String> contextKey,String instruction,boolean errorF){
        try{
            String key = contextKey.peek();
            String[] keys = key.split("_");
            List<String> keyList = Arrays.asList(keys);

            // 如果输入错误 进行排查
            boolean errorFlag = true;
            if(errorF && keyList.size() > 1){
                List<String> tempList = new ArrayList<>();
                for (int i = 0; i < keyList.size()-1; i++) {
                    tempList.add(keyList.get(i));
                }
                keyList = tempList;
                errorFlag = false;
            }

            // 顶级
            if("system".equals(keys[0])){

                // 一级
                if(keyList.size() == 1){
                    if(errorFlag){
                        contextKey.add(contextKey.peek()+"_top");
                    }


                    instruction = TestHash.validataScanner(instructions.get(TestHash.createInstructionKey()));
                    // 验证指令输入
                    String instructionKeyTemp = null;
                    if(instructionKeys.size() > 0){
                        instructionKeyTemp = instructionKeys.peek();
                    }
                    TestHash.invokeScanner(contextKey,instructionKeyTemp,false);
                }else{
                    // 一级
                    if("top".equals(keyList.get(1))){
                        // 查询当前所有redis集群
                        if("1".equals(instruction)){
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_getRedis");
                            }
                            // 打印当前 redis 服务器数量
                            TestHash.printRedis(redisMap);


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_back");
                            }
                            instruction = TestHash.validataScanner(instructions.get(default2));
                            TestHash.invokeScanner(contextKey,instruction,false);
                        }
                        // 增加redis集群
                        else if("2".equals(instruction)){
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_addRedis");
                            }

                            String code = "";
                            Integer num = 0;

                            System.out.println();
                            System.out.println("------------------------------");
                            System.out.println("请输入redis编号：");

                            boolean fl = true;
                            while (fl){
                                // 开启输入模式
                                Scanner scanner = new Scanner(System.in);
                                String str = scanner.nextLine();

                                String pattern = "[\\w\\d]+";
                                boolean matches = Pattern.matches(pattern, str);
                                if(matches){
                                    List<Integer> integers = redisRepository.get(str);
                                    if(integers != null){
                                        System.out.println("------------------------------");
                                        System.out.println();
                                        System.out.println("[ "+str+" ] 编号已存在");
                                        System.out.println();
                                        System.out.println("请输入redis编号：");
                                        System.out.println();
                                        continue;
                                    }
                                    code = str;
                                    fl = false;
                                }else{
                                    System.out.println("------------------------------");
                                    System.out.println();
                                    System.out.println("[ "+str+" ] 只能包含字母、数字与_");
                                    System.out.println();
                                    System.out.println("请输入redis编号：");
                                    System.out.println();
                                }
                            }

                            System.out.println();
                            System.out.println("------------------------------");
                            System.out.println("请输入redis数据范围：");

                            boolean fl1 = true;
                            while (fl1){
                                // 开启输入模式
                                Scanner scanner = new Scanner(System.in);
                                String str = scanner.nextLine();

                                String pattern = "[\\d]+";
                                boolean matches = Pattern.matches(pattern, str);
                                if(matches){
                                    num = Integer.parseInt(str);
                                    String s = redisMap.get(num);
                                    if(!StringUtils.isEmpty(s)){
                                        System.out.println("------------------------------");
                                        System.out.println();
                                        System.out.println("[ "+str+" ] 数据范围已存在");
                                        System.out.println();
                                        System.out.println("请输入redis数据范围：");
                                        System.out.println();
                                        continue;
                                    }
                                    fl1 = false;
                                }else{
                                    System.out.println("------------------------------");
                                    System.out.println();
                                    System.out.println("[ "+str+" ] 只能包含数字");
                                    System.out.println();
                                    System.out.println("请输入redis数据范围：");
                                    System.out.println();
                                }
                            }


                            TestHash.createRedisMap(code,num);


                            // 打印当前 redis 服务器数量
                            TestHash.printRedis(redisMap);


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_add");
                            }
                            instruction = TestHash.validataScanner(instructions.get(default2));
                            TestHash.invokeScanner(contextKey,instruction,false);
                        }
                        // 增加redis数据
                        else if("3".equals(instruction)){
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_addRedisData");
                            }

                            System.out.println();
                            System.out.println("------------------------------");
                            System.out.println("请输入数值：");

                            Integer num = 0;

                            boolean fl = true;
                            while (fl){
                                // 开启输入模式
                                Scanner scanner = new Scanner(System.in);
                                String str = scanner.nextLine();

                                String pattern = "[\\d]+";
                                boolean matches = Pattern.matches(pattern, str);
                                if(matches){
                                    num = Integer.parseInt(str);
                                    fl = false;
                                }else{
                                    System.out.println("------------------------------");
                                    System.out.println();
                                    System.out.println("[ "+str+" ] 只能包含数字");
                                    System.out.println();
                                    System.out.println("请输入数值：");
                                    System.out.println();
                                }
                            }


                            TestHash.addNum(num);


                            // 打印当前 redis 数值
                            //TestHash.printRedisData();


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_add");
                            }
                            instruction = TestHash.validataScanner(instructions.get(default2));
                            TestHash.invokeScanner(contextKey,instruction,false);
                        }
                        // 查询当前所有redis数据
                        if("4".equals(instruction)){
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_findAll");
                            }
                            // 打印当前 redis 所有数据
                            TestHash.findAll();


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_findAll");
                            }
                            instruction = TestHash.validataScanner(instructions.get(default2));
                            TestHash.invokeScanner(contextKey,instruction,false);
                        }
                        // 删除redis数据
                        else if("5".equals(instruction)){
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_deleteRedisData");
                            }

                            System.out.println();
                            System.out.println("------------------------------");
                            System.out.println("请输入数值：");

                            Integer num = 0;

                            boolean fl = true;
                            while (fl){
                                // 开启输入模式
                                Scanner scanner = new Scanner(System.in);
                                String str = scanner.nextLine();

                                String pattern = "[\\d]+";
                                boolean matches = Pattern.matches(pattern, str);
                                if(matches){
                                    num = Integer.parseInt(str);
                                    fl = false;
                                }else{
                                    System.out.println("------------------------------");
                                    System.out.println();
                                    System.out.println("[ "+str+" ] 只能包含数字");
                                    System.out.println();
                                    System.out.println("请输入数值：");
                                    System.out.println();
                                }
                            }


                            TestHash.deleteData(num);


                            // 打印当前 redis 数值
                            //TestHash.printRedisData();


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_delete");
                            }
                            instruction = TestHash.validataScanner(instructions.get(default2));
                            TestHash.invokeScanner(contextKey,instruction,false);
                        }
                        else{
                            //二级
                            if(keyList.size() > 2){
                                // 查询当前所有redis集群
                                if("getRedis".equals(keyList.get(2))){

                                    // 返回上一层
                                    if("0".equals(instruction)){
                                        // 取两次 包含本次的正确 指令
                                        contextKey.pop();
                                        contextKey.pop();
                                        instructionKeys.pop();
                                        instructionKeys.pop();
                                        String instructionKeyTemp = null;
                                        if(instructionKeys.size() > 0){
                                            instructionKeyTemp = instructionKeys.peek();
                                        }
                                        TestHash.invokeScanner(contextKey,instructionKeyTemp,true);
                                    }
                                }else if("addRedis".equals(keyList.get(2))){

                                    // 返回上一层
                                    if("0".equals(instruction)){
                                        // 取两次 包含本次的正确 指令
                                        contextKey.pop();
                                        contextKey.pop();
                                        instructionKeys.pop();
                                        instructionKeys.pop();
                                        String instructionKeyTemp = null;
                                        if(instructionKeys.size() > 0){
                                            instructionKeyTemp = instructionKeys.peek();
                                        }
                                        TestHash.invokeScanner(contextKey,instructionKeyTemp,true);
                                    }
                                }else if("addRedisData".equals(keyList.get(2))){

                                    // 返回上一层
                                    if("0".equals(instruction)){
                                        // 取两次 包含本次的正确 指令
                                        contextKey.pop();
                                        contextKey.pop();
                                        instructionKeys.pop();
                                        instructionKeys.pop();
                                        String instructionKeyTemp = null;
                                        if(instructionKeys.size() > 0){
                                            instructionKeyTemp = instructionKeys.peek();
                                        }
                                        TestHash.invokeScanner(contextKey,instructionKeyTemp,true);
                                    }
                                }else if("findAll".equals(keyList.get(2))){

                                    // 返回上一层
                                    if("0".equals(instruction)){
                                        // 取两次 包含本次的正确 指令
                                        contextKey.pop();
                                        contextKey.pop();
                                        instructionKeys.pop();
                                        instructionKeys.pop();
                                        String instructionKeyTemp = null;
                                        if(instructionKeys.size() > 0){
                                            instructionKeyTemp = instructionKeys.peek();
                                        }
                                        TestHash.invokeScanner(contextKey,instructionKeyTemp,true);
                                    }
                                }else if("deleteRedisData".equals(keyList.get(2))){

                                    // 返回上一层
                                    if("0".equals(instruction)){
                                        // 取两次 包含本次的正确 指令
                                        contextKey.pop();
                                        contextKey.pop();
                                        instructionKeys.pop();
                                        instructionKeys.pop();
                                        String instructionKeyTemp = null;
                                        if(instructionKeys.size() > 0){
                                            instructionKeyTemp = instructionKeys.peek();
                                        }
                                        TestHash.invokeScanner(contextKey,instructionKeyTemp,true);
                                    }
                                }



                            }
                        }
                    }

                }
            }

            // 如果没有拦截到 则狗咬狗尾巴
            String instructionKeyTemp = null;
            if(instructionKeys.size() > 0){
                instructionKeyTemp = instructionKeys.peek();
            }
            TestHash.invokeScanner(contextKey,instructionKeyTemp,true);

        }catch (Exception e){
            //e.printStackTrace();
            System.out.println(e.getMessage());
            // 错误
            TestHash.errorScanner(e.getMessage());
        }
    }

    /**
     * 判断输入
     * @param map 指令集合
     */
    public static String validataScanner(Map<String, String> map) throws Exception{

        // 打印指令
        TestHash.printScannerKeys(map);
        // 开启输入模式
        Scanner scanner = new Scanner(System.in);
        String str = scanner.nextLine();
        String obj = map.get(str);
        if(obj != null){
            instructionKeys.add(str);
            return str;
        }
        // 否则就报错
        throw new Exception(str);
    }





    /**
     * 生成指令Key
     * @return
     */
    public static String createInstructionKey() {
        return contextKeys.peek();
    }

    /**
     * 生成所有指令
     * @return
     */
    public static Map<String,Map<String, String>> createInstructions() {

        //总 指令
        Map<String, Map<String, String>> map = new HashMap<>();

        //第一层指令
        Map<String, String> allMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        allMap.put("1","查询当前所有redis集群");
        allMap.put("2","增加集群");
        allMap.put("3","添加数据");
        allMap.put("4","查看所有数据");
        allMap.put("5","删除数据");

        map.put("system_top",allMap);

        //默认二级命令
        Map<String, String> default2Map = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        default2Map.put("0","返回上级菜单");
        map.put("default2",default2Map);

        return map;
    }

    /**
     * 输入错误
     */
    public static void errorScanner(String str) {
        String strT = "";
        if(!StringUtils.isEmpty(str)){
            strT = "[ "+str+" ] ";
        }

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println(strT + "输入参数有误，请重新输入！");
        System.out.println();

        String instructionKeyTemp = null;
        if(instructionKeys.size() > 0){
            instructionKeyTemp = instructionKeys.peek();
        }

        // 回调
        TestHash.invokeScanner(contextKeys,instructionKeyTemp,true);
    }
    /**
     * 打印指令
     */
    public static void printScannerKeys(Map<String, String> map) {
        // 打印命令
        Set<String> keySet = map.keySet();
        Iterator<String> iter = keySet.iterator();
        System.out.println();
        System.out.println("------------------------------");
        while (iter.hasNext()) {
            String key = iter.next();
            System.out.println("指令 [ "+key+" ]" + " " + map.get(key));
        }
        System.out.println();
        System.out.println("请输入指令：");
    }
    /**
     * 打印当前redis服务器
     * @param map
     */
    public static void printRedis(Map<Integer, String> map){
        Set<Integer> keySet = map.keySet();
        Iterator<Integer> iter = keySet.iterator();
        System.out.println();
        while (iter.hasNext()) {
            Integer key = iter.next();
            System.out.println(key + ":" + map.get(key));
        }
        System.out.println();
    }

}
