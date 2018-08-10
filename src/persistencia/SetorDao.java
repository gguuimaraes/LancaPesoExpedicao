package persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import entidades.Funcionario;
import entidades.Setor;
import util.Conexao;

public class SetorDao implements Dao<Setor> {

	@Override
	public List<Setor> listar() throws SQLException {
		List<Setor> l = new ArrayList<Setor>();
		try {
			ResultSet rs = Conexao.getInstance().createStatement().executeQuery("SELECT * FROM setor");
			while (rs.next()) {
				Setor s = new Setor();
				s.setId(rs.getInt("id"));
				s.setNome(rs.getString("nome"));
				l.add(s);
			}
		} catch (SQLException e) {
			throw e;
		}
		return l;
	}

	@Override
	public void inserir(Setor objeto) {
		// TODO Auto-generated method stub
		
	}
	
	public Setor consultarPeloNome(String nome) throws Exception {
		Setor s = null;
		try {
			PreparedStatement ps = Conexao.getInstance()
					.prepareStatement("SELECT * FROM setor s WHERE s.nome = ?");
			ps.setString(1, nome);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				if (s != null)
					throw new Exception("Mais de um setor com o mesmo nome encontrado!");
				s = new Setor();
				s.setId(rs.getInt("id"));
				s.setNome(rs.getString("nome"));
			}
		} catch (SQLException e) {
			throw e;
		}
		return s;
	}

}
