package persistencia;

import java.util.List;

public interface Dao<T> {
	public List<T> listar() throws Exception;

	public void inserir(T objeto) throws Exception;
}
