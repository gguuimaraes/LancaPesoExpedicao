package br.com.vitral.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import br.com.vitral.entidade.Setor;
import br.com.vitral.util.Conexao;

public class SetorDao implements Dao<Setor> {

	@Override
	public List<Setor> listar() throws SQLException {
		List<Setor> l = new ArrayList<>();
		try (ResultSet rs = Conexao.getInstance().createStatement().executeQuery("SELECT * FROM setor")) {
			while (rs.next()) {
				Setor s = new Setor();
				s.setId(rs.getInt("id"));
				s.setNome(rs.getString("nome"));
				l.add(s);
			}
		}
		return l;
	}

	@Override
	public String salvar(Setor objeto) {
		return null;
	}

	public Setor consultarPeloNome(String nome) throws SQLException {
		Setor s = null;
		ResultSet rs = null;
		try (PreparedStatement ps = Conexao.getInstance().prepareStatement("SELECT * FROM setor s WHERE s.nome = ?")) {
			ps.setString(1, nome);
			rs = ps.executeQuery();
			while (rs.next()) {
				if (s != null)
					throw new SQLException("Mais de um setor com o mesmo nome encontrado!");
				s = new Setor();
				s.setId(rs.getInt("id"));
				s.setNome(rs.getString("nome"));
			}
		} finally {
			if (rs != null)
				rs.close();
		}
		return s;
	}

}
