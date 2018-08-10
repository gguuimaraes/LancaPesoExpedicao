package persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import entidades.Peso;
import util.Conexao;

public class PesoDao implements Dao<Peso> {

	@Override
	public List<Peso> listar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void inserir(Peso objeto) throws SQLException {
		try {
			Conexao.getInstance().setAutoCommit(false);
			PreparedStatement ps;
			Float peso = temFuncionarioSetorDia(objeto);
			if (peso != null) {
				if (peso == objeto.getPeso())
					return;
				ps = Conexao.getInstance().prepareStatement(
						"UPDATE peso SET peso = ? WHERE funcionario_id = ? AND setor_id = ? AND data = ?;");
				ps.setFloat(1, objeto.getPeso());
				ps.setDate(4, new java.sql.Date(objeto.getData().getTime()));
			} else {
				ps = Conexao.getInstance().prepareStatement(
						"INSERT INTO peso (data, funcionario_id, setor_id, peso) VALUES (?, ?, ?, ?);");
				ps.setDate(1, new java.sql.Date(objeto.getData().getTime()));
				ps.setFloat(4, objeto.getPeso());
			}
			ps.setInt(2, objeto.getFuncionario().getId());
			ps.setInt(3, objeto.getSetor().getId());
			ps.execute();
			Conexao.getInstance().commit();
		} catch (SQLException e) {
			Conexao.getInstance().rollback();
			throw e;
		} finally {
			Conexao.getInstance().setAutoCommit(true);
		}
	}

	/*
	 * verifica se já existe algum peso inserido para o funcionario naquele setor e
	 * dia se sim, retorna o valor do peso se não, retorna nulo
	 */
	private Float temFuncionarioSetorDia(Peso p) throws SQLException {
		PreparedStatement ps = Conexao.getInstance()
				.prepareStatement("SELECT * FROM peso p WHERE p.funcionario_id = ? AND p.setor_id = ? AND p.data = ?");
		ps.setInt(1, p.getFuncionario().getId());
		ps.setInt(2, p.getSetor().getId());
		ps.setDate(3, new java.sql.Date(p.getData().getTime()));
		ResultSet rs = ps.executeQuery();
		if (rs.next()) {
			return rs.getFloat("peso");
		}
		return null;
	}

}
