package pers.nebo.hdfs.kafkawrite.kafkconsumer;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.*;

/**
 * Hive数据清洗消费者
 */
public class HiveCleanedConsumer {
    private final KafkaConsumer consumer;
    private final String topic = "eshop";

    private static HDFSWriter writer = new HDFSWriter();

    public HiveCleanedConsumer() {
        Properties props = new Properties();
        props.put("zookeeper.connect", "s202:2181");
        props.put("group.id", "ggg2");
        props.put("auto.offset.reset", "smallest");
        props.put("zookeeper.session.timeout.ms", "500");
        props.put("zookeeper.sync.time.ms", "250");
        props.put("auto.commit.interval.ms", "1000");
        // 创建消费者连接器
        consumer =new KafkaConsumer<>(props);
    }

    /**
     * 处理log
     */
    public void processLog() {
        // 指定消费的主题
        Map<String, Integer> topicCount = new HashMap<String, Integer>();
        topicCount.put(topic, new Integer(1));




        consumer.subscribe(Arrays.asList(topic));

        while(true){

            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {

                {

                    MyFSDataOutputStream out = null ;
                    String prePath = "";

                        byte[] msg = record.value().getBytes();
                        String newMsg = null ;
                        String log = new String(msg) ;
                        String[] arr = StringUtil.splitLog(log);
                        //
                        if(arr == null || arr.length < 10){
                            continue ;
                        }

                        System.out.println("hive : " + log);
                        //进行清洗
                        String request = arr[4];
                        String[] reqArr = request.split(" ") ;
                        if(reqArr != null && reqArr.length == 3){
                            if(reqArr[1].endsWith(".html")){
                                newMsg = StringUtil.arr2Str(arr,",");
                            }
                            else{
                                continue ;
                            }
                        }
                        else{
                            continue ;
                        }
                        //主机名
                        String hostname = StringUtil.getHostname(arr);
                        //
                        //取出日期对象
                        Date reqDate = StringUtil.str2Date(arr);
                        //得到日历对象
                        Calendar c = Calendar.getInstance();
                        //设置Date时间
                        c.setTime(reqDate);
                        int y = c.get(Calendar.YEAR);
                        int m = c.get(Calendar.MONTH) + 1;
                        int d = c.get(Calendar.DAY_OF_MONTH);
                        int h = c.get(Calendar.HOUR_OF_DAY);
                        int mi = c.get(Calendar.MINUTE);

                        //path
                        String rawPath = "/user/hive/warehouse/eshop.db/logs/year="+y
                                + "/month=" + m
                                + "/day=" + d
                                + "/hour=" + h
                                + "/minute=" + mi
                                + "/" + hostname+ ".log";
                        try{
                            //判断是否和上一次相同
                            if(!rawPath.equals(prePath)){
                                if(out != null){
                                    out.release();
                                    out = null ;
                                }
                                out = (MyFSDataOutputStream)HDFSOutputStreamPool.getInstance().takeOutputStream(rawPath);
                                prePath = rawPath ;             //
                            }
                            //
                            out.write(newMsg.getBytes());
                            out.write("\r\n".getBytes());
                            out.hsync();
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }

                }



            }


        }

    }
}
