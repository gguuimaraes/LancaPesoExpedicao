package br.com.vitral.persistencia;

import java.sql.SQLException;
import java.util.List;

public interface Dao<T> {
	public List<T> listar() throws SQLException;

	public String salvar(T objeto) throws SQLException;
}
