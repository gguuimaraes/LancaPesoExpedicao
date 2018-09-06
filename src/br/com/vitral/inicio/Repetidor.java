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

	@Override
	public void run() {
		try {
			Calendar c;
			int pausa = 120000; // 2 minutos
			while (pausa > 0) {
				c = Calendar.getInstance();
				if (c.get(Calendar.HOUR_OF_DAY) >= 8 && c.get(Calendar.HOUR_OF_DAY) <= 18) {
					
					salvarPesoExpedicaoAntigo();
					Thread.sleep(1000);
					
					//salvarPesoExpedicao();
					//Thread.sleep(1000);
					
					salvarPesoEntrega();
					Thread.sleep(1000);
					
					salvarAreaMesa();
					
					pausa = 120000;
				} else {
					pausa = 3600000;
				}
				System.out.println("AGUARDANDO " + pausa / 60000 + " MINUTOS...");
				Thread.sleep(pausa);
			}
		} catch (Exception e) {
			System.out.printf("OCORREU UM ERRO: \n%s" + e);
		}

	}

	private static FuncionarioDao fDao = new FuncionarioDao();
	private static SetorDao sDao = new SetorDao();
	private static PesoDao pDao = new PesoDao();
	private static AreaCortadaDao aDao = new AreaCortadaDao();

	public static void salvarPesoExpedicaoAntigo() throws Exception {
		System.out.println("CONSULTANDO PESO EXPEDICAO ANTIGO...");
		for (Peso p : obtemPesoFuncionario()) {
			pDao.salvar(p);
		}
	}

	public static void salvarAreaMesa() throws Exception {
		System.out.println("CONSULTANDO AREA MESA...");
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
	}

	public static void salvarPesoEntrega() throws Exception {
		System.out.println("CONSULTANDO PESO ENTREGA...");
		Exception erro = null;
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
					erro = new Exception(String.format("Erro ao salvar Peso Entrega, Funcionário %s não encontrado.\n",
							jPeso.getNome().trim()));
				else {
					peso.setFuncionario(fu);
					peso.setSetor(setorEntrega);
					pDao.salvar(peso);
				}
			}
		}
		if (erro != null)
			throw erro;
	}

	public static void salvarPesoExpedicao() throws Exception {
		System.out.println("CONSULTANDO PESO EXPEDICAO...");
		Exception erro = null;
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
				Funcionario fu = fDao.consultarPeloNome(jPeso.getExpedidor());
				if (fu == null) {
					erro = new Exception(String.format(
							"Erro ao salvar Peso Expedição, Funcionário %s não encontrado.\n", jPeso.getExpedidor()));
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
					System.out.println(peso);
					pDao.salvar(peso);
				}

			}
		}
		if (erro != null)
			throw erro;
	}

	private static List<Peso> obtemPesoFuncionario() throws Exception {
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
