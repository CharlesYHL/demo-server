package word.utils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.SessionStore;

/**
 * Created by Neil.Zhou on 2016/03/20.
 */
public class SessionHandlerImpl implements SessionHandler {

    private static final Logger log = LoggerFactory.getLogger(SessionHandlerImpl.class);

    private final SessionStore sessionStore;
    private String sessionCookieName;
    private long sessionTimeout;
    private boolean nagHttps;


    public SessionHandlerImpl(String sessionCookieName, long sessionTimeout, boolean nagHttps, SessionStore sessionStore) {
        this.sessionCookieName = sessionCookieName;
        this.sessionTimeout = sessionTimeout;
        this.nagHttps = nagHttps;
        this.sessionStore = sessionStore;
    }
    public static SessionHandler createSession(SessionStore sessionStore,String name) {
        return new SessionHandlerImpl(name, DEFAULT_SESSION_TIMEOUT, DEFAULT_NAG_HTTPS, sessionStore);
    }

    @Override
    public SessionHandler setSessionTimeout(long timeout) {
        this.sessionTimeout = timeout;
        return this;
    }
    @Override
    public SessionHandler setNagHttps(boolean nag) {
        this.nagHttps = nag;
        return this;
    }

    @Override
    public SessionHandler setSessionCookieName(String sessionCookieName) {
        this.sessionCookieName = sessionCookieName;
        return this;
    }
    @Override
    public SessionHandler setCookieHttpOnlyFlag(boolean httpOnly) {
        return this;
    }
    @Override
    public SessionHandler setCookieSecureFlag(boolean secure) {
        return this;
    }
    @Override
    public void handle(RoutingContext context) {
        context.response().ended();
        if (nagHttps) {
            String uri = context.request().absoluteURI();
            if (!uri.startsWith("https:")) {
                log.warn("Using session cookies without https could make you susceptible to session hijacking: " + uri);
            }
        }
        String sessionID = context.request().headers().get("sessionId");
        String dev = context.request().headers().get("Dev");
        if(!"pc".equals(dev)){
            context.next();
            return;
        }
        if (sessionID != null) {
            sessionStore.get(sessionID, res -> {
                if (res.succeeded()) {
                    Session session = res.result();
                    if (session != null) {
                        context.setSession(session);
                        session.setAccessed();
                        addStoreSessionHandler(context);
                    } else {
                        createNewSession(context);
                    }
                } else {
                    context.fail(res.cause());
                }
                context.next();
            });
        } else {
            createNewSession(context);
            context.next();
        }
    }

    private void addStoreSessionHandler(RoutingContext context) {
        context.addHeadersEndHandler(v -> {
            Session session = context.session();
            if (!session.isDestroyed()) {
                session.setAccessed();
                sessionStore.put(session, res -> {
                    if (res.failed()) {
                        log.error("Failed to store session", res.cause());
                    }
                });
            } else {
                sessionStore.delete(session.id(), res -> {
                    if (res.failed()) {
                        log.error("Failed to delete session", res.cause());
                    }
                });
            }
        });
    }
    private void createNewSession(RoutingContext context) {
        Session session = sessionStore.createSession(sessionTimeout);
        context.setSession(session);
        addStoreSessionHandler(context);
    }
}

