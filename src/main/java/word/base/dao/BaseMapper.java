package word.base.dao;

/**
 * Created by Administrator on 2015/9/24.
 */


import com.github.miemiedev.mybatis.paginator.domain.PageBounds;

import java.util.List;

public interface BaseMapper<T> {
    T findOne(T map);
    void insert(T map);
    void update(T map);
    void delete(T map);
    List<T> findPage(T map, PageBounds pb);

}
