package word.base.ob;

import com.github.miemiedev.mybatis.paginator.domain.Paginator;

import java.util.List;

/**
 * Created by
 * yangkun on 2016/1/19.
 */
public class ResultOb<T> extends BaseOb {
    private int currentPage = 1;
    private boolean lastPage = true;

    private List<T> listOb;
    private int total;     //共多少条
    private int count;     //每页多少条
    private int pagetotal;     //共多少页
    private boolean hasNextPage; //有下一页
    private boolean hasPrePage; //有上一页

    public void setPaginator(Paginator page){
        this.currentPage = page.getPage();
        this.lastPage = !page.isHasNextPage();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public boolean isLastPage() {
        return lastPage;
    }

    public void setLastPage(boolean lastPage) {
        this.lastPage = lastPage;
    }

    public List<T> getListOb() {
        return listOb;
    }

    public void setListOb(List<T> listOb) {
        this.listOb = listOb;
    }

    public void setPage(Paginator p) {
        total = p.getTotalCount();     //共多少条
        count = p.getLimit();     //每页多少条
        pagetotal = p.getTotalPages();     //共多少页
        currentPage = p.getPage();  //第几页
        hasNextPage = p.isHasNextPage(); //有下一页
        hasPrePage = p.isHasPrePage(); //有上一页
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getPagetotal() {
        return pagetotal;
    }

    public void setPagetotal(int pagetotal) {
        this.pagetotal = pagetotal;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public boolean isHasPrePage() {
        return hasPrePage;
    }

    public void setHasPrePage(boolean hasPrePage) {
        this.hasPrePage = hasPrePage;
    }
}
