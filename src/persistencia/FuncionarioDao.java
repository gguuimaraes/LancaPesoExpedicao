package persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entidades.Funcionario;
import util.Conexao;

public class FuncionarioDao implements Dao<Funcionario> {

	@Override
	public List<Funcionario> listar() throws SQLException {
		List<Funcionario> l = new ArrayList<>();
		try (ResultSet rs = Conexao.getInstance().createStatement().executeQuery("SELECT * FROM funcionario")) {
			while (rs.next()) {
				Funcionario f = new Funcionario();
				f.setId(rs.getInt("id"));
				f.setNome(rs.getString("nome"));
				l.add(f);
			}
		}
		return l;
	}

	@Override
	public void inserir(Funcionario objeto) {

	}

	public Funcionario consultarPeloNome(String nome) throws SQLException {
		Funcionario f = null;
		ResultSet rs = null;
		try (PreparedStatement ps = Conexao.getInstance()
				.prepareStatement("SELECT * FROM funcionario f WHERE f.nome = ?")) {
			ps.setString(1, nome);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (f != null)
					throw new SQLException("Mais de um funcionário com o mesmo nome encontrado!");
				f = new Funcionario();
				f.setId(rs.getInt("id"));
				f.setNome(rs.getString("nome"));
			}
		} finally {
			if (rs != null)
				rs.close();

		}
		return f;
	}

}
