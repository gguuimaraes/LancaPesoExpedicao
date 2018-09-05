import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.AreaMesa;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import entidades.AreaCortada;
import entidades.Peso;
import persistencia.AreaCortadaDao;
import persistencia.FuncionarioDao;
import persistencia.PesoDao;
import persistencia.SetorDao;

public class Main {

	private static FuncionarioDao fDao = new FuncionarioDao();
	private static SetorDao sDao = new SetorDao();
	private static PesoDao pDao = new PesoDao();
	private static AreaCortadaDao aDao = new AreaCortadaDao();

	private static Thread t = new Thread(new Runnable() {

		@Override
		public void run() {
			try {
				Calendar c;
				int pausa = 120000; // 2 minutos
				while (true) {
					c = Calendar.getInstance();
					System.out.println("HORA DO DIA: " + c.get(Calendar.HOUR_OF_DAY));
					if (c.get(Calendar.HOUR_OF_DAY) >= 8 && c.get(Calendar.HOUR_OF_DAY) <= 18) {
						System.out.println("CONSULTANDO PESOS...");
						inserePesos();
						Thread.sleep(1000);
						System.out.println("CONSULTANDO AREA MESAS...");
						insereAreaCortadaMesas();
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
	});

	public static void main(String[] args) throws Exception {
		t.start();
	}

	public static void inserePesos() throws Exception {
		for (Peso p : obtemPesoFuncionario()) {
			pDao.inserir(p);
		}
	}

	public static void insereAreaCortadaMesas() throws Exception {
		List<AreaMesa> areas = obtemAreaMesas();
		if (!areas.isEmpty()) {
			AreaCortada areaCortada;
			for (AreaMesa area : areas) {
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
				aDao.inserir(areaCortada);
			}

		}
	}

	private static List<Peso> obtemPesoFuncionario() throws Exception {
		List<Peso> pesos = new ArrayList<>();
		try {
			Document doc = obtemPaginaPeso();
			Element table = doc.getElementsByClass("container").get(0).getElementsByTag("table").get(0);
			Elements linhas = table.getElementsByTag("tr");
			linhas.remove(0);
			Map<String, Float> pesoPorFuncionario = new HashMap<>();
			for (Element e : linhas) {
				if (e.getElementsByTag("td").size() == 1) {
					System.out.println("Nenhuma expedição encontrada.");
					return pesos;
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
					System.out.printf("OCORREU UM ERRO: \n%s", e1);
				}
				pesos.add(peso);
			});
		} catch (IOException e) {
			throw e;
		}
		return pesos;

	}

	private static Document obtemPaginaPeso() throws IOException {
		Calendar c = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.DATE, 1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return Jsoup.connect(URL_PESO).data("datainicial", sdf.format(c.getTime()))
				.data("datafinal", sdf.format(c2.getTime())).userAgent("Mozilla").post();
	}

	private static List<AreaMesa> obtemAreaMesas() throws Exception {
		List<AreaMesa> areas = new ArrayList<>();
		try {
			Document doc = obtemPaginaArea();
			Element body = doc.getElementsByTag("body").get(0);
			String json = body.text().substring(1);
			JSONArray array = new JSONArray(json);
			AreaMesa area = null;
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				area = new AreaMesa();
				area.setCodmesa(obj.getInt("codmesa"));
				area.setArea(obj.getFloat("area"));
				area.setData(df.parse(obj.getString("data")));
				areas.add(area);
			}

		} catch (IOException | IndexOutOfBoundsException e) {
			throw e;
		}
		return areas;

	}

	private static Document obtemPaginaArea() throws IOException {
		Calendar c = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		c2.add(Calendar.DATE, 1);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		return Jsoup.connect(String.format(URL_AREA_MESA, df.format(c.getTime()), df.format(c2.getTime())))
				.userAgent("Mozilla").get();
	}

	private static final String URL_PESO = "http://192.168.0.104/controleexpedicao/relgerencial2.php";
	private static final String URL_AREA_MESA = "http://192.168.0.104/webservice/wscontroledecorte.php?datainicial=%s&datafinal=%s";
}
