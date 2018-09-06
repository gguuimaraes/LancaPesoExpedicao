package br.com.vitral.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.vitral.entidade.AreaCortada;
import br.com.vitral.util.Conexao;

public class AreaCortadaDao implements Dao<AreaCortada> {

	@Override
	public List<AreaCortada> listar() {
		return new ArrayList<>();
	}

	@Override
	public void salvar(AreaCortada objeto) throws SQLException {
		PreparedStatement ps = null;
		try {
			Conexao.getInstance().setAutoCommit(false);
			Float area = temAreaCortadaFuncionarioSetorDia(objeto);
			if (area != null) {
				if (area == objeto.getArea())
					return;
				ps = Conexao.getInstance().prepareStatement(
						"UPDATE areacortada SET area = ? WHERE funcionario_id = ? AND setor_id = ? AND data = ?;");
				ps.setFloat(1, objeto.getArea());
				ps.setDate(4, new java.sql.Date(objeto.getData().getTime()));
				System.out.print("Atualizando a ");
			} else {
				ps = Conexao.getInstance().prepareStatement(
						"INSERT INTO areacortada (data, funcionario_id, setor_id, area) VALUES (?, ?, ?, ?);");
				ps.setDate(1, new java.sql.Date(objeto.getData().getTime()));
				ps.setFloat(4, objeto.getArea());
				System.out.print("Inserindo uma ");
			}
			System.out.printf("AreaCortada para o Funcionario %s, Setor %s, Area %.2f\n", objeto.getFuncionario().getNome(), objeto.getSetor().getNome(), objeto.getArea());
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
	 * verifica se já existe alguma area inserida para o funcionario naquele setor e
	 * dia se sim, retorna o valor da area se não, retorna nulo
	 */
	private Float temAreaCortadaFuncionarioSetorDia(AreaCortada a) throws SQLException {
		Float area = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = Conexao.getInstance().prepareStatement(
					"SELECT * FROM areacortada a WHERE a.funcionario_id = ? AND a.setor_id = ? AND a.data = ?");
			ps.setInt(1, a.getFuncionario().getId());
			ps.setInt(2, a.getSetor().getId());
			ps.setDate(3, new java.sql.Date(a.getData().getTime()));
			rs = ps.executeQuery();
			if (rs.next()) {
				area = rs.getFloat("area");
			}
		} catch (Exception i) {

		} finally {
			if (rs != null)
				rs.close();
			if (ps != null)
				ps.close();
		}
		return area;
	}

}
