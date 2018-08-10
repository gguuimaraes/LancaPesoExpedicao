package persistencia;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import entidades.AreaCortada;
import util.Conexao;

public class AreaCortadaDao implements Dao<AreaCortada> {

	@Override
	public List<AreaCortada> listar() {
		return null;
	}

	@Override
	public void inserir(AreaCortada objeto) throws SQLException {
		try {
			Conexao.getInstance().setAutoCommit(false);
			PreparedStatement ps = Conexao.getInstance().prepareStatement(
					"INSERT INTO areacortada (data, funcionario_id, setor_id, area) VALUES (?, ?, ?, ?);");
			ps.setTimestamp(1, new java.sql.Timestamp(objeto.getData().getTime()));
			ps.setInt(2, objeto.getFuncionario().getId());
			ps.setInt(3, objeto.getSetor().getId());
			ps.setFloat(4, objeto.getArea());
			ps.execute();
			Conexao.getInstance().commit();
		} catch (SQLException e) {
			Conexao.getInstance().rollback();
			throw e;
		}
	}

}
