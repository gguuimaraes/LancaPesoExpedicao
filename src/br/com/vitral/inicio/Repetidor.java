package br.com.vitral.inicio;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.com.vitral.consumidor.Analisador;
import br.com.vitral.consumidor.Pagina;
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
			System.out.printf("Aguardando %d minutos...%n", pausa / 60000);
			pausar(pausa);
		}

	}

	static void pausar(int milisegundos) {
		try {
			Thread.sleep(milisegundos);
		} catch (Exception e) {
			System.err.printf("Erro: %s%n", e.getMessage());
		}
	}

	static void salvarPesoExpedicaoAntigo() {
		System.out.println("Consultando peso expedição [antigo]...");
		try {
			for (Peso p : obtemPesoFuncionario()) {
				pDao.salvar(p);
			}
		} catch (Exception e) {
			System.err.printf("Erro: %s%n", e.getMessage());
		}
	}

	static void salvarAreaMesa() {
		System.out.println("Consultando área mesa...");
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
			System.err.printf("Erro: %s%n", e.getMessage());
		}
	}

	static void salvarPesoEntrega() {
		System.out.println("Consultando peso entrega...");
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
						System.out.printf("Erro ao salvar Peso Entrega, Funcionário %s não encontrado.%n",
								jPeso.getNome());
					else {
						peso.setFuncionario(fu);
						peso.setSetor(setorEntrega);
						pDao.salvar(peso);
					}
				}
			}
		} catch (Exception e) {
			System.err.printf("Erro: %s%n", e.getMessage());
		}
	}

	static void salvarPesoExpedicao() {
		System.out.println("Consultando peso expedição...");
		try {
			List<JsonPesoExpedicao> pesos = Analisador.pesoExpedicao();
			if (!pesos.isEmpty()) {
				Peso peso;
				Date agora = new Date();
				Setor setorExpedicao = sDao.consultarPeloNome("EXPEDICAO");
				Setor setorExpedicaoVPE = sDao.consultarPeloNome("EXPEDICAO VPE");
				// Setor setorPonteRolante = sDao.consultarPeloNome("PONTE ROLANTE");
				for (JsonPesoExpedicao jPeso : pesos) {
					peso = new Peso();
					peso.setPeso(jPeso.getPeso());
					peso.setData(agora);
					String nomeFu = Negocio.getInstance().tratarNomeFuncionario(jPeso.getExpedidor());
					Funcionario fu = fDao.consultarPeloNome(nomeFu);
					if (fu == null) {
						System.out.printf("Erro ao salvar Peso Expedição, Funcionário %s não encontrado.%n", nomeFu);
					} else {
						peso.setFuncionario(fu);
						switch (jPeso.getExpedicao()) {
						case "COMUM":
							peso.setSetor(setorExpedicao);
							break;
						case "ALMOXARIFADO":
							peso.setSetor(setorExpedicaoVPE);
							break;
						default:
							System.out.printf(
									"Erro ao salvar Peso Expedição, JsonPesoExpedicao.expedicao=%s não encontrada.%n",
									jPeso.getExpedicao());
						}
						// System.out.println(peso);
						pDao.salvar(peso);
					}

				}
			}
		} catch (Exception e) {
			System.err.printf("Erro: %s%n", e.getMessage());
		}
	}

	static List<Peso> obtemPesoFuncionario() throws Exception {
		List<Peso> pesos = new ArrayList<>();
		try {
			Document doc = Pagina.obterPagina(Pagina.URL_PESO_EXPEDICAO_ANTIGO);
			Element table = doc.getElementsByClass("container").get(0).getElementsByTag("table").get(0);
			Elements linhas = table.getElementsByTag("tr");
			linhas.remove(0);
			Map<String, Float> pesoPorFuncionario = new HashMap<>();
			for (Element e : linhas) {
				if (e.getElementsByTag("td").size() == 1) {
					throw new Exception("Nenhuma expedição encontrada.");
				}
				Element t = e.getElementsByTag("td").get(1);
				Element f = e.getElementsByTag("td").get(3);
				Element p = e.getElementsByTag("td").get(11);
				String tipo = t.getElementsByTag("strong").get(0).html();
				String fNome = f.getElementsByTag("font").get(0).html();
				fNome = Negocio.getInstance().funcionarioPorTipoExpedicao(fNome, tipo);
				float pe = Float.parseFloat(p.getElementsByTag("font").get(0).html());
				pesoPorFuncionario.put(fNome,
						pesoPorFuncionario.get(fNome) == null ? pe : pesoPorFuncionario.get(fNome) + pe);
			}
			Date agora = new Date();
			pesoPorFuncionario.forEach((k, v) -> {
				Peso peso = new Peso();
				peso.setData(agora);
				peso.setPeso(v);
				try {
					peso.setFuncionario(fDao.consultarPeloNome(k));
					peso.setSetor(Negocio.getInstance().getSetorDoFuncionario(k));
				} catch (Exception e1) {
					System.out.println(e1);
				}
				pesos.add(peso);
			});
		} catch (IOException e) {
			throw e;
		}
		return pesos;

	}

}
