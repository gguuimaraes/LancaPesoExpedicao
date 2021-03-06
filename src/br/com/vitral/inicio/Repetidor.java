package br.com.vitral.inicio;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import br.com.vitral.consumidor.Analisador;
import br.com.vitral.entidade.AreaCortada;
import br.com.vitral.entidade.Funcionario;
import br.com.vitral.entidade.Peso;
import br.com.vitral.entidade.Setor;
import br.com.vitral.json.JsonAreaMesa;
import br.com.vitral.json.JsonPesoEntrega;
import br.com.vitral.json.JsonPesoExpedicao;
import br.com.vitral.persistencia.AreaCortadaDao;
import br.com.vitral.persistencia.FuncionarioDao;
import br.com.vitral.persistencia.PesoDao;
import br.com.vitral.persistencia.SetorDao;

public class Repetidor implements Runnable {

	private static FuncionarioDao fDao = new FuncionarioDao();
	private static SetorDao sDao = new SetorDao();
	private static PesoDao pDao = new PesoDao();
	private static AreaCortadaDao aDao = new AreaCortadaDao();

	static Logger logger;

	public Repetidor(Logger logger) {
		Repetidor.logger = logger;
	}

	@Override
	public void run() {
		long pausa = 1; // 2 minutos
		while (pausa > 0) {
			salvarPesoExpedicao();
			salvarPesoEntrega();
			salvarAreaMesa();
			pausa = calcularPausa();
			logger.info(String.format("Aguardando %d minutos", pausa / 60000));
			pausar(pausa);
		}

	}

	private long calcularPausa() {
		Calendar c = Calendar.getInstance();
		Calendar c2 = (Calendar) c.clone();
		int diaSemana = c.get(Calendar.DAY_OF_WEEK);
		int dias = 1;
		if (diaSemana >= Calendar.MONDAY && diaSemana <= Calendar.SATURDAY) {
			int fimExpediente = diaSemana <= Calendar.FRIDAY ? 18 : 12;
			int hora = c.get(Calendar.HOUR_OF_DAY);
			if (hora >= 8 && hora < fimExpediente)
				return 120000; // 2 minutos
			else
				dias = hora >= fimExpediente ? diaSemana - 5 : 0;
		} else
			dias = 1;
		c2.add(Calendar.DATE, dias);
		c2.set(Calendar.HOUR_OF_DAY, 8);
		c2.set(Calendar.MINUTE, 0);
		c2.set(Calendar.SECOND, 1);
		return c2.getTimeInMillis() - c.getTimeInMillis();
	}

	static void pausar(long milisegundos) {
		try {
			Thread.sleep(milisegundos);
		} catch (Exception e) {
			logger.error(String.format("%s", e.getMessage()));
		}
	}

	static void salvarAreaMesa() {
		logger.info("Consultando �rea mesa");
		try {
			List<JsonAreaMesa> areas = Analisador.areaMesa();
			if (!areas.isEmpty()) {
				AreaCortada areaCortada;
				for (JsonAreaMesa area : areas) {
					areaCortada = new AreaCortada();
					areaCortada.setArea(area.getArea());
					areaCortada.setData(area.getData());
					if (area.getCodmesa() == 2) {
						areaCortada.setFuncionario(fDao.consultarPeloNome("WESLEY GENILSON DOS SANTOS"));
						areaCortada.setSetor(sDao.consultarPeloNome("MESA PEQUENA"));
					} else if (area.getCodmesa() == 1) {
						areaCortada.setFuncionario(fDao.consultarPeloNome("MAURICIO FERREIRA DE SOUZA"));
						areaCortada.setSetor(sDao.consultarPeloNome("MESA GRANDE"));
					}
					String retorno = aDao.salvar(areaCortada);
					if (retorno != null)
						logger.info(retorno);
				}

			}
		} catch (Exception e) {
			logger.error(String.format("%s", e.toString()));
		}
	}

	static void salvarPesoEntrega() {
		logger.info("Consultando peso entrega");
		try {
			List<JsonPesoEntrega> pesos = Negocio.getInstance().tratarListaPesoEntrega(Analisador.pesoEntrega());
			if (!pesos.isEmpty()) {
				Peso peso;
				Date agora = new Date();
				Setor setorEntrega = sDao.consultarPeloNome("ENTREGA");
				for (JsonPesoEntrega jPeso : pesos) {
					peso = new Peso();
					peso.setPeso(jPeso.getPeso());
					peso.setData(agora);
					Funcionario fu = fDao.consultarPeloNome(jPeso.getNome());
					if (fu == null)
						logger.error(String.format("Erro ao salvar Peso Entrega, Funcion�rio %s n�o encontrado.",
								jPeso.getNome()));
					else {
						peso.setFuncionario(fu);
						peso.setSetor(setorEntrega);
						String retorno = pDao.salvar(peso);
						if (retorno != null)
							logger.info(retorno);
					}
				}
			}
		} catch (Exception e) {
			logger.error(String.format("%s", e.toString()));
		}
	}

	static void salvarPesoExpedicao() {
		logger.info("Consultando peso expedi��o");
		try {
			List<JsonPesoExpedicao> pesos = Analisador.pesoExpedicao();
			if (!pesos.isEmpty()) {
				Peso peso;
				Date agora = new Date();
				Setor setorExpedicao = sDao.consultarPeloNome("EXPEDICAO");
				Setor setorExpedicaoVPE = sDao.consultarPeloNome("EXPEDICAO VPE");
				Setor setorPonteRolante = sDao.consultarPeloNome("PONTE ROLANTE");
				for (JsonPesoExpedicao jPeso : pesos) {
					peso = new Peso();
					peso.setPeso(jPeso.getPeso());
					peso.setData(agora);
					String nomeFu = Negocio.getInstance().tratarNomeFuncionario(jPeso.getExpedidor());
					Funcionario fu = fDao.consultarPeloNome(nomeFu);
					if (fu == null) {
						logger.error(
								String.format("Erro ao salvar Peso Expedi��o, Funcion�rio %s n�o encontrado.", nomeFu));
					} else {
						peso.setFuncionario(fu);
						switch (fu.getNome()) {
						case "EDMAR ARAUJO CONCEICAO":
						case "ELISMAR MARTINS":
							peso.setSetor(setorPonteRolante);
							break;
						default:
							switch (jPeso.getExpedicao()) {
							case "COMUM":
								peso.setSetor(setorExpedicao);
								break;
							case "ALMOXARIFADO":
								peso.setSetor(setorExpedicaoVPE);
								break;
							}
						}
						String retorno = pDao.salvar(peso);
						if (retorno != null)
							logger.info(retorno);
					}

				}
			}
		} catch (Exception e) {
			logger.error(String.format("%s", e.toString()));
		}
	}

}
