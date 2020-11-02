import java.util.*;
import java.util.regex.Pattern;

/**
 * Created Date by 2019/12/27 0027.
 * 模拟 一致性hash算法 前半部分 redis 分布区域选择
 * 这里用随机数代替hash
 *
 * @author Parker
 */
public class TestHash1 {

    private static String default2 = "default2";
    // 2^32 次方
    private static long baseNum = 1L << 32;
    /** 生成所有指令 */
    private static Map<String, Map<String, String>> instructions = new HashMap<>();
    /**上下文指令 */
    private static Stack<String> contextKeys = new Stack<>();
    /**上下文指令 */
    private static Stack<String> instructionKeys = new Stack<>();
    /** redis 集合 */
    private static Map<Long, String> redisMap = new TreeMap<>(new Comparator<Long>() {
        @Override
        public int compare(Long o1, Long o2) {
            return o1.compareTo(o2);
        }
    });
    /** redis 仓库 */
    private static Map<String, List<Integer>> redisRepository = new HashMap<>();

    static final int hash(String obj) {
        int h;
        return (obj == null) ? 0 : (h = obj.hashCode()) ^ (h >>> 16);
    }

    /**
     * 静态块
     */
    static{
        contextKeys.add("system");
        instructions = TestHash1.createInstructions();

        // 添加三台redis服务器 在一个闭环
        TestHash1.createRedisMap("xxoo1-redis");
        TestHash1.createRedisMap("xxoo2-redis");
        TestHash1.createRedisMap("xxoo3-redis");

        // 初始化数据
        TestHash1.addNum(1000);
        TestHash1.addNum(2000);
        TestHash1.addNum(5000);
        TestHash1.addNum(6000);
        TestHash1.addNum(7000);
        TestHash1.addNum(8000);
        TestHash1.addNum(10000);
        TestHash1.addNum(15000);
        TestHash1.addNum(20000);
        TestHash1.addNum(25000);
        TestHash1.addNum(40000);
    }



    public static void main(String[] args) {
        System.out.println("欢迎使用 redis一致性hash算法 模拟系统 ！");
        System.out.println();


        // 输入检测
        TestHash1.invokeScanner(contextKeys,null,false);
    }


    /**
     * 生成Redis 仓库
     *
     * 生成仓库 则 锁定类 这里用 synchronized 代替
     * ☆☆☆☆☆☆☆☆☆☆☆☆☆
     *  现实环境中 需要￿做分布式锁 避免在迁移数据过程中 又有新的数据 做改动 ****** ☆
     * @return
     */
    public synchronized static void createRedisMap(String redisCode) {
        long index = hash(redisCode) % baseNum;

        Set<Long> integers = redisMap.keySet();
        Iterator<Long> iterator = integers.iterator();
        List<Long> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(Long::compareTo);

        // 调用闭环算法
        Long key = null;
        if(keys != null && !keys.isEmpty()){
            key = TestHash1.closedLoopAlgorithm(index,keys);
        }
        // 根据 闭环算法 获得数据
        List<Integer> datas = null;
        if(null != key){
            datas = redisRepository.get(redisMap.get(key));
        }

        // 创建集合
        List<Integer> list = new ArrayList<>();
        redisRepository.put(redisCode,list);
        redisMap.put(index, redisCode);

        // 因为上方有新增redis 获取 新的 keys
        integers = redisMap.keySet();
        iterator = integers.iterator();
        List<Long> keysT = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keysT.add(iterator.next());
        }


        // 数据迁移
        if(datas != null && !datas.isEmpty()){
            List<Integer> newDatas = new ArrayList<>();
            // 循环所有数据 查找出 要存放在新容器里的数据
            for (Integer data : datas) {
                long longIndex = data % baseNum;
                // 调用闭环算法
                Long keyT = TestHash1.closedLoopAlgorithm(longIndex,keysT);
                if(keyT.equals(index)){
                    newDatas.add(data);
                }
            }

            // 删除 旧数据
            for (Integer newData : newDatas) {
                // 传入keys 因为 keys已做更改 传入旧keys
                TestHash1.deleteOldData(newData,keys);
            }
            // 添加新数据
            List<Integer> newQueue = redisRepository.get(redisMap.get(index));
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
        long index = hash(String.valueOf(num)) % baseNum;
        Long key = 0L;
        Set<Long> integers = redisMap.keySet();
        Iterator<Long> iterator = integers.iterator();
        List<Long> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(Comparator.naturalOrder());

        // 调用闭环算法
        key = TestHash1.closedLoopAlgorithm(index,keys);

        List<Integer> datas = redisRepository.get(redisMap.get(key));
        datas.add(num);

        List<Integer> nums = new ArrayList<>();
        for (Long longIndex : keys) {
            if(redisRepository.get(redisMap.get(longIndex)) != null){
                nums.addAll(redisRepository.get(redisMap.get(longIndex)));
            }
        }

        System.out.println();
        System.out.println("当前数据：");
        System.out.println(nums.toString());
        for (Long longIndex : keys) {
            if(redisRepository.get(redisMap.get(longIndex)) != null){
                System.out.println(redisMap.get(longIndex)+":"+redisRepository.get(redisMap.get(longIndex)).toString());
            }
        }
        System.out.println();
    }

    /**
     * 查看Redis 所有数据
     * @return
     */
    public static void findAll() {
        Set<Long> integers = redisMap.keySet();
        Iterator<Long> iterator = integers.iterator();
        List<Long> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(Comparator.naturalOrder());


        List<Integer> nums = new ArrayList<>();
        for (Long longIndex : keys) {
            if(redisRepository.get(redisMap.get(longIndex)) != null){
                nums.addAll(redisRepository.get(redisMap.get(longIndex)));
            }
        }

        System.out.println();
        System.out.println("当前数据：");
        System.out.println(nums.toString());
        for (Long longIndex : keys) {
            if(redisRepository.get(redisMap.get(longIndex)) != null){
                System.out.println(redisMap.get(longIndex)+":"+redisRepository.get(redisMap.get(longIndex)).toString());
            }
        }
        System.out.println();
    }

    /**
     * 删除Redis数据
     * @return
     */
    public static void deleteData(Integer num) {
        long index = hash(String.valueOf(num)) % baseNum;
        Long key = 0L;
        Set<Long> integers = redisMap.keySet();
        Iterator<Long> iterator = integers.iterator();
        List<Long> keys = new ArrayList<>(integers.size());
        while (iterator.hasNext()){
            keys.add(iterator.next());
        }
        //正序
        keys.sort(Comparator.naturalOrder());

        // 调用闭环算法
        key = TestHash1.closedLoopAlgorithm(index,keys);

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
        for (Long longIndex : keys) {
            if(redisRepository.get(redisMap.get(longIndex)) != null){
                nums.addAll(redisRepository.get(redisMap.get(longIndex)));
            }
        }

        System.out.println();
        System.out.println("当前数据：");
        System.out.println(nums.toString());
        for (Long longIndex : keys) {
            if(redisRepository.get(redisMap.get(longIndex)) != null){
                System.out.println(redisMap.get(longIndex)+":"+redisRepository.get(redisMap.get(longIndex)).toString());
            }
        }
        System.out.println();
    }

    /**
     * 删除Redis数据
     * @return
     */
    public synchronized static void deleteOldData(Integer num,List<Long> keys) {
        long index = hash(String.valueOf(num)) % baseNum;
        Long key = 0L;

        //正序
        keys.sort(Comparator.naturalOrder());

        // 调用闭环算法
        key = TestHash1.closedLoopAlgorithm(index,keys);

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
        for (Long longIndex : keys) {
            if(redisRepository.get(redisMap.get(longIndex)) != null){
                nums.addAll(redisRepository.get(redisMap.get(longIndex)));
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
    public static Long closedLoopAlgorithm(Long num,List<Long> keys){
        Long key = 0L;
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


                    instruction = TestHash1.validataScanner(instructions.get(TestHash1.createInstructionKey()));
                    // 验证指令输入
                    String instructionKeyTemp = null;
                    if(instructionKeys.size() > 0){
                        instructionKeyTemp = instructionKeys.peek();
                    }
                    TestHash1.invokeScanner(contextKey,instructionKeyTemp,false);
                }else{
                    // 一级
                    if("top".equals(keyList.get(1))){
                        // 查询当前所有redis集群
                        if("1".equals(instruction)){
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_getRedis");
                            }
                            // 打印当前 redis 服务器数量
                            TestHash1.printRedis(redisMap);


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_back");
                            }
                            instruction = TestHash1.validataScanner(instructions.get(default2));
                            TestHash1.invokeScanner(contextKey,instruction,false);
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


                            TestHash1.createRedisMap(code);


                            // 打印当前 redis 服务器数量
                            TestHash1.printRedis(redisMap);


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_add");
                            }
                            instruction = TestHash1.validataScanner(instructions.get(default2));
                            TestHash1.invokeScanner(contextKey,instruction,false);
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


                            TestHash1.addNum(num);


                            // 打印当前 redis 数值
                            //TestHash.printRedisData();


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_add");
                            }
                            instruction = TestHash1.validataScanner(instructions.get(default2));
                            TestHash1.invokeScanner(contextKey,instruction,false);
                        }
                        // 查询当前所有redis数据
                        if("4".equals(instruction)){
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_findAll");
                            }
                            // 打印当前 redis 所有数据
                            TestHash1.findAll();


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_findAll");
                            }
                            instruction = TestHash1.validataScanner(instructions.get(default2));
                            TestHash1.invokeScanner(contextKey,instruction,false);
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


                            TestHash1.deleteData(num);


                            // 打印当前 redis 数值
                            //TestHash.printRedisData();


                            // 发起二次输入
                            // 验证指令输入
                            if(errorFlag){
                                contextKey.add(contextKey.peek()+"_delete");
                            }
                            instruction = TestHash1.validataScanner(instructions.get(default2));
                            TestHash1.invokeScanner(contextKey,instruction,false);
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
                                        TestHash1.invokeScanner(contextKey,instructionKeyTemp,true);
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
                                        TestHash1.invokeScanner(contextKey,instructionKeyTemp,true);
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
                                        TestHash1.invokeScanner(contextKey,instructionKeyTemp,true);
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
                                        TestHash1.invokeScanner(contextKey,instructionKeyTemp,true);
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
                                        TestHash1.invokeScanner(contextKey,instructionKeyTemp,true);
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
            TestHash1.invokeScanner(contextKey,instructionKeyTemp,true);

        }catch (Exception e){
            //e.printStackTrace();
            System.out.println(e.getMessage());
            // 错误
            TestHash1.errorScanner(e.getMessage());
        }
    }

    /**
     * 判断输入
     * @param map 指令集合
     */
    public static String validataScanner(Map<String, String> map) throws Exception{

        // 打印指令
        TestHash1.printScannerKeys(map);
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
        if(str != null && !"".equals(str)){
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
        TestHash1.invokeScanner(contextKeys,instructionKeyTemp,true);
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
    public static void printRedis(Map<Long, String> map){
        Set<Long> keySet = map.keySet();
        Iterator<Long> iter = keySet.iterator();
        System.out.println();
        while (iter.hasNext()) {
            Long key = iter.next();
            System.out.println(key + ":" + map.get(key));
        }
        System.out.println();
    }

}
