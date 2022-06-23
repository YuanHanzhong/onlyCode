package _06_exam;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Calendar;
import java.util.Random;

public class exeClickSource implements SourceFunction<exeClickEvent> {
    boolean isRuning = true;
    Random random = new Random();
    String[] urls = {"/cart", "/buy"};
    String[] names = {"Jack", "Mike", "Tome"};
    
    @Override
    public void run(SourceContext<exeClickEvent> ctx) throws Exception {
        while (isRuning) {
            ctx.collect(
              new exeClickEvent(
                names[random.nextInt(names.length)],
                urls[random.nextInt(urls.length)],
                Calendar.getInstance().getTimeInMillis()
              )
            );
            Thread.sleep(1000L);
        }
        
    }
    
    @Override
    public void cancel() {
        isRuning = false;
    }
}
