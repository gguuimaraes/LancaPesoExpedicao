package br.com.vitral.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import br.com.vitral.entidade.Peso;
import br.com.vitral.util.Conexao;

public class PesoDao implements Dao<Peso> {

	@Override
	public List<Peso> listar() {
		return new ArrayList<>();
	}

	@Override
	public void salvar(Peso objeto) throws SQLException {
		PreparedStatement ps = null;
		try {
			Conexao.getInstance().setAutoCommit(false);
			Float peso = temFuncionarioSetorDia(objeto);
			if (peso != null) {
				if (peso == objeto.getPeso())
					return;
				ps = Conexao.getInstance().prepareStatement(
						"UPDATE peso SET peso = ? WHERE funcionario_id = ? AND setor_id = ? AND data = ?;");
				ps.setFloat(1, objeto.getPeso());
				ps.setDate(4, new java.sql.Date(objeto.getData().getTime()));
				System.out.print("Atualizando o ");
			} else {
				ps = Conexao.getInstance().prepareStatement(
						"INSERT INTO peso (data, funcionario_id, setor_id, peso) VALUES (?, ?, ?, ?);");
				ps.setDate(1, new java.sql.Date(objeto.getData().getTime()));
				ps.setFloat(4, objeto.getPeso());
				System.out.print("Inserindo um ");
			}
			System.out.printf("Peso para o Funcionario %s, Setor %s, Peso %.2f\n", objeto.getFuncionario().getNome(), objeto.getSetor().getNome(), objeto.getPeso());
			ps.setInt(2, objeto.getFuncionario().getId());
			ps.setInt(3, objeto.getSetor().getId());
			ps.execute();
			Conexao.getInstance().commit();
		} catch (SQLException e) {
			Conexao.getInstance().rollback();
			throw e;
		} finally {
			if (ps != null)
				ps.close();
			Conexao.getInstance().setAutoCommit(true);
		}
	}

	/*
	 * verifica se já existe algum peso inserido para o funcionario naquele setor e
	 * dia se sim, retorna o valor do peso se não, retorna nulo
	 */
	private Float temFuncionarioSetorDia(Peso p) throws SQLException {
		Float r = null;
		ResultSet rs = null;
		try (PreparedStatement ps = Conexao.getInstance().prepareStatement(
				"SELECT * FROM peso p WHERE p.funcionario_id = ? AND p.setor_id = ? AND p.data = ?")) {
			ps.setInt(1, p.getFuncionario().getId());
			ps.setInt(2, p.getSetor().getId());
			ps.setDate(3, new java.sql.Date(p.getData().getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				r = rs.getFloat("peso");
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		return r;
	}

}
