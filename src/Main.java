import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import entidades.Peso;
import entidades.Setor;
import persistencia.FuncionarioDao;
import persistencia.PesoDao;
import persistencia.SetorDao;

public class Main {

	private static FuncionarioDao fDao = new FuncionarioDao();
	private static SetorDao sDao = new SetorDao();
	private static PesoDao pDao = new PesoDao();

	public static void main(String[] args) throws Exception {
		inserePesos();
	}

	public static void inserePesos() throws Exception {
		for (Peso p : obtemPesoFuncionario()) {
			pDao.inserir(p);
		}
	}

	private static List<Peso> obtemPesoFuncionario() throws Exception {
		List<Peso> pesos = new ArrayList<Peso>();
		try {
			Document doc = obtemDocPagina();
			Element table = doc.getElementsByClass("container").get(0).getElementsByTag("table").get(0);
			Elements linhas = table.getElementsByTag("tr");
			linhas.remove(0);
			Setor setorExpedicao = sDao.consultarPeloNome("EXPEDICAO");
			Map<String, Float> pesoPorFuncionario = new HashMap<String, Float>();
			for (Element e : linhas) {
				Element f = e.getElementsByTag("td").get(3);
				Element p = e.getElementsByTag("td").get(11);
				String fNome = f.getElementsByTag("font").get(0).html();
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
					peso.setSetor(setorExpedicao);
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

	private static Document obtemDocPagina() throws IOException {
		Calendar c = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.DATE, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return Jsoup.connect("http://192.168.0.104/controleexpedicao/relgerencial2.php")
				.data("datainicial", sdf.format(c.getTime())).data("datafinal", sdf.format(c2.getTime()))
				.userAgent("Mozilla").post();
	}
}
