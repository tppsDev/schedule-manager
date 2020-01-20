/*
 * Project written by: Tim Smith
 * 
 */
package schedulemanager.DAO;

import javafx.collections.ObservableList;

/**
 *
 * @author Tim Smith
 * @param <T>
 */
public interface CrudDao<T> {
    public ObservableList<?> getAll() throws SysOpException, Exception;
    // Consider refactoring to accept object for maximum portability
    public T getById(int id) throws SysOpException, Exception;
    public void insert(T dataObj) throws SysOpException, Exception;
    public void update(T dataObj) throws SysOpException, Exception;
    public void delete(T dataObj) throws SysOpException, Exception;
}
