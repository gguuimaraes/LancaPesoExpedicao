import java.sql.SQLException;

import entidades.Setor;
import persistencia.SetorDao;

public class Negocio {

	private static Negocio o = null;

	private SetorDao sDao = null;

	private Setor setorExpedicao;
	private Setor setorExpedicaoVPE;
	private Setor setorPonteRolante;

	private Negocio() {
		if (sDao == null)
			sDao = new SetorDao();

		try {
			setorExpedicao = sDao.consultarPeloNome("EXPEDICAO");
			setorExpedicaoVPE = sDao.consultarPeloNome("EXPEDICAO VPE");
			setorPonteRolante = sDao.consultarPeloNome("PONTE ROLANTE");
		} catch (SQLException e) {
		}
	}

	public static Negocio getInstance() {
		if (o == null)
			o = new Negocio();
		return o;
	}

	public Setor getSetorDoFuncionario(String nome) {
		switch (nome) {
		case "FIDELCI SOUZA LIMA":
			return setorExpedicaoVPE;
		case "ELISMAR MARTINS":
		case "EDMAR ARAUJO CONCEICAO":
			return setorPonteRolante;
		default:
			return setorExpedicao;
		}

	}

	public String funcionarioPorTipoExpedicao(String funcionario, String tipo) {
		if (tipo.equals("PE"))
			return "FIDELCI SOUZA LIMA";
		else
			return funcionario;
	}
}
