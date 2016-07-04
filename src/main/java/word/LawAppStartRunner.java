package word;


import word.verticle.*;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by Administrator on 2015/9/22.
 */
public class LawAppStartRunner {
    private static Logger logger = LoggerFactory.getLogger(LawAppStartRunner.class);

    public static void main(String[] args) {
        //ApplicationContext context = new AnnotationConfigApplicationContext(ExampleSpringConfiguration.class);
        // ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        ApplicationContext context = new ClassPathXmlApplicationContext("DatabaseSource.xml");

        logger.debug("=======================Runner  Deployment======================");

        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new HttpServerVerticle(context));

    }


}
