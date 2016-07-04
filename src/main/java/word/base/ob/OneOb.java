package word.base.ob;

/**
 * 消息返回对象 类
 * @author Neil.Zhou
 * @Date 2016/03/21
 */
public class OneOb<T> extends BaseOb {
    public OneOb(T ob) {
        this.ob = ob;
    }
    public OneOb(boolean flag, int code, String msg) {
        super.setCode(code);
        super.setMsg(msg);
    }
    public OneOb() { }

    private T ob;

    public T getOb() {
        return ob;
    }

    public void setOb(T ob) {
        this.ob = ob;
    }
}
