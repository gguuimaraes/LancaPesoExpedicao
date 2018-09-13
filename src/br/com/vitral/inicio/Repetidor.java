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
import br.com.vitral.json.modelo.JsonAreaMesa;
import br.com.vitral.json.modelo.JsonPesoEntrega;
import br.com.vitral.json.modelo.JsonPesoExpedicao;
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
		this.logger = logger;
	}

	@Override
	public void run() {
		Calendar c;
		int pausa = 120000; // 2 minutos
		int hora;
		while (pausa > 0) {
			salvarPesoExpedicao();
			pausar(1000);
			salvarPesoEntrega();
			pausar(1000);
			salvarAreaMesa();
			c = Calendar.getInstance();
			hora = c.get(Calendar.HOUR_OF_DAY);
			if (hora >= 8 && hora <= 18) {
				pausa = 120000;
			} else {
				pausa = 3600000;
			}
			logger.info(String.format("Aguardando %d minutos%n", pausa / 60000));
			pausar(pausa);
		}

	}

	static void pausar(int milisegundos) {
		try {
			Thread.sleep(milisegundos);
		} catch (Exception e) {
			logger.error(String.format("%s%n", e.getMessage()));
		}
	}

	static void salvarAreaMesa() {
		logger.info("Consultando área mesa");
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
					aDao.salvar(areaCortada);
				}

			}
		} catch (Exception e) {
			logger.error(String.format("%s%n", e.getMessage()));
		}
	}

	static void salvarPesoEntrega() {
		logger.info("Consultando peso entrega");
		try {
			List<JsonPesoEntrega> pesos = Analisador.pesoEntrega();
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
						logger.error(String.format("Erro ao salvar Peso Entrega, Funcionário %s não encontrado.%n",
								jPeso.getNome()));
					else {
						peso.setFuncionario(fu);
						peso.setSetor(setorEntrega);
						pDao.salvar(peso);
					}
				}
			}
		} catch (Exception e) {
			logger.error(String.format("%s%n", e.getMessage()));
		}
	}

	static void salvarPesoExpedicao() {
		logger.info("Consultando peso expedição");
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
						logger.error(String.format("Erro ao salvar Peso Expedição, Funcionário %s não encontrado.%n", nomeFu));
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

						pDao.salvar(peso);
					}

				}
			}
		} catch (Exception e) {
			logger.error(String.format("%s%n", e.getMessage()));
		}
	}

}
