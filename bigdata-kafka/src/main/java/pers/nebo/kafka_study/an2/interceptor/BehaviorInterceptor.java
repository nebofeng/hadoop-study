package pers.nebo.kafka_study.an2.interceptor;

import java.util.List;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;

import com.google.common.collect.Lists;
import pers.nebo.kafka_study.an2.model.UserBehaviorRequestModel;
import pers.nebo.kafka_study.an2.utils.JSONUtil;

/**
 * Created by John on 2017/4/25.
 */
public class BehaviorInterceptor implements Interceptor
{
    @Override
    public void initialize() {

    }

    @Override
    public Event intercept(Event event) {
        //如果event为空过滤掉
        if(event == null || event.getBody() == null || event.getBody().length == 0){
            return null;
        }

        long userId = 0;

        //解析日志
        try{
            UserBehaviorRequestModel model = JSONUtil.json2Object(new String(event.getBody()),UserBehaviorRequestModel.class);
            userId = model.getUserId();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(userId == 0){
            return null;
        }
        //将userId赋值给key
        event.getHeaders().put("key",userId+"");

        return event;
    }

    @Override
    public List<Event> intercept(List<Event> events) {

        List<Event> out = Lists.newArrayList();
        for (Event event : events) {
            Event outEvent = intercept(event);
            //event 为空过滤掉
            if (outEvent != null) { out.add(outEvent); }
        }
        return out;
    }

    @Override
    public void close() {

    }
    //程序入口
    public static class BehaviorBuilder implements Interceptor.Builder {

        @Override
        public Interceptor build() {
            return new BehaviorInterceptor();
        }

        @Override
        public void configure(Context context) {

        }
    }
}



