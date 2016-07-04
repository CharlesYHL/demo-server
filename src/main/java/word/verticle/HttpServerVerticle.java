package word.verticle;


import word.base.annotations.RouteHandler;
import word.base.annotations.RouteMapping;
import word.base.annotations.RouteMethod;
import word.utils.ConfigManager;
import word.utils.SessionHandlerImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static io.vertx.core.http.HttpHeaders.*;

/**
 * Simple web server verticle to expose the results of the Spring service bean call (routed via a verticle - see
 * SpringDemoVerticle)
 */
public class HttpServerVerticle extends AbstractVerticle {
    private static final Logger log = LoggerFactory.getLogger(HttpServerVerticle.class);
    private static final Reflections reflections = new Reflections("com.word.contrl");
    SessionStore store;
    SessionHandler sessionHandler;

    protected Router router;
    Logger logger = LoggerFactory.getLogger(HttpServerVerticle.class);
    HttpServer server;
    public HttpServerVerticle(final ApplicationContext context) {

    }

    @Override
    public void start(Future<Void> future) throws Exception {
        logger.info("==============web start==============");
        super.start();
        store = LocalSessionStore.create(vertx);
        sessionHandler = SessionHandler.create(store);


        server = vertx.createHttpServer();
        server = vertx.createHttpServer(createOptions());
        server.requestHandler(createRouter()::accept);
        server.listen(result2 -> {
            if (result2.succeeded()) {
                future.complete();
            } else {
                future.fail(result2.cause());
            }
        });
    }

    @Override
    public void stop(Future<Void> future) {
        if (server == null) {
            future.complete();
            return;
        }
        server.close(result -> {
            if (result.failed()) {
                future.fail(result.cause());
            } else {
                future.complete();
            }
        });
    }


    private HttpServerOptions createOptions() {
        HttpServerOptions options = new HttpServerOptions();
        //   options.setHost("localhost");
        options.setPort(ConfigManager.getInstance().getInt("api.port"));
        return options;
    }

    private Router createRouter() {
        logger.info("-------createRouter--------");

        router = Router.router(vertx);
        router.route().handler(ctx -> {
            logger.debug("--------path:" + ctx.request().path()
                            + "--------uri:" + ctx.request().absoluteURI()
                            + "-------method:" + ctx.request().method()
            );
            ctx.request().headers().add(CONTENT_TYPE, "charset=utf-8");
            ctx.response().headers().add(CONTENT_TYPE, "application/json; charset=utf-8");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_METHODS, "POST, GET, OPTIONS, PUT, DELETE, HEAD");
            ctx.response().headers().add(ACCESS_CONTROL_ALLOW_HEADERS, "X-PINGOTHER, Origin,Content-Type, Accept, X-Requested-With, sessionId,Dev,Version");
            ctx.response().headers().add(ACCESS_CONTROL_MAX_AGE, "1728000");
            ctx.next();
        });


        /*
        允许跨域请求*/
        Set<HttpMethod> method = new HashSet<HttpMethod>();
        method.add(HttpMethod.GET);
        method.add(HttpMethod.POST);
        method.add(HttpMethod.OPTIONS);
        method.add(HttpMethod.PUT);
        method.add(HttpMethod.DELETE);
        method.add(HttpMethod.HEAD);
        router.route().handler(CorsHandler.create("*").allowedMethods(method));
        router.route().handler(BodyHandler.create());
        router.route().handler(CookieHandler.create());

        SessionStore sessionStore = LocalSessionStore.create(vertx, "demow.web.sessions");
        sessionHandler = SessionHandlerImpl.createSession(sessionStore, "demow.web.sessions");
        sessionHandler.setSessionTimeout(30*60*1000);
        sessionHandler.setNagHttps(false);
        router.route().handler(sessionHandler);


        registerHandlers();
        return router;
    }

    private void registerHandlers() {
        log.debug("Register available request handlers...");

        Set<Class<?>> handlers = reflections.getTypesAnnotatedWith(RouteHandler.class);
        for (Class<?> handler : handlers) {
            try {
                registerNewHandler(handler);
            } catch (Exception e) {
                log.error("Error register {}", handler);
            }
        }
    }

    private void registerNewHandler(Class<?> handler) throws Exception {
        String root = "";
        if (handler.isAnnotationPresent(RouteHandler.class)) {
            RouteHandler routeHandler = handler.getAnnotation(RouteHandler.class);
            root = routeHandler.value();
        }
        Object instance = handler.newInstance();
        Method[] methods = handler.getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RouteMapping.class)) {
                RouteMapping mapping = method.getAnnotation(RouteMapping.class);
                RouteMethod routeMethod = mapping.method();

                 String url = root + "/" + method.getName() + mapping.value();
              //  String url = root + "/" + method.getName() + ".hbs";


                Handler<RoutingContext> methodHandler = (Handler<RoutingContext>) method.invoke(instance);
                log.debug("Register New Handler -> {}:{}", routeMethod, url);
                switch (routeMethod) {
                    case POST:
                        router.post(url).handler(methodHandler);

                        break;
                    case PUT:
                        router.put(url).handler(methodHandler);

                        break;
                    case DELETE:
                        router.delete(url).handler(methodHandler);

                        break;
                    case GET: // fall through
                    default:
                        router.get(url).handler(methodHandler);

                        break;
                }
            }
        }
    }


    private void redirectTo(RoutingContext context, String url) {
        HttpServerResponse response = context.response();
        response.setStatusCode(303);
       // response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        response.headers().add("Location", url);
        response.end();
    }


}
