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
				Calendar c = Calendar.getInstance();
				int pausa = 120000; // 2 minutos
				while (true) {
					if (c.get(Calendar.HOUR_OF_DAY) > 8 && c.get(Calendar.HOUR_OF_DAY) < 18) {
						System.out.println("CONSULTANDO PESOS...");
						inserePesos();
						Thread.sleep(1000);
						System.out.println("CONSULTANDO AREA MESA PEQUENA...");
						insereAreaCortadaMesaPequena();
						Thread.sleep(1000);
						System.out.println("CONSULTANDO AREA MESA GRANDE...");
						insereAreaCortadaMesaGrande();
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

	public static void insereAreaCortadaMesaPequena() throws Exception {
		Float area = obtemAreaMesa(2);
		if (area != null) {
			AreaCortada areaCortada = new AreaCortada();
			areaCortada.setArea(area);
			areaCortada.setFuncionario(fDao.consultarPeloNome("WESLEY GENILSON DOS SANTOS"));
			areaCortada.setData(new Date());
			areaCortada.setSetor(sDao.consultarPeloNome("MESA PEQUENA"));
			aDao.inserir(areaCortada);
		}
	}

	public static void insereAreaCortadaMesaGrande() throws Exception {
		Float area = obtemAreaMesa(1);
		if (area != null) {
			AreaCortada areaCortada = new AreaCortada();
			areaCortada.setArea(area);
			areaCortada.setFuncionario(fDao.consultarPeloNome("MAURICIO FERREIRA DE SOUZA"));
			areaCortada.setData(new Date());
			areaCortada.setSetor(sDao.consultarPeloNome("MESA GRANDE"));
			aDao.inserir(areaCortada);
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

	private static Float obtemAreaMesa(int mesa) throws Exception {
		Float area = null;
		try {
			Document doc = obtemPaginaArea(mesa);
			Element a = doc.getElementsByAttributeValue("href", "resumopedidoscortados.php?codmesa=" + mesa).get(0);
			area = Float.parseFloat(a.getElementsByTag("strong").get(0).html());
		} catch (IOException | IndexOutOfBoundsException e) {
			throw e;
		}
		return area == 0 ? null : area;

	}

	private static Document obtemPaginaArea(int mesa) throws IOException {
		return Jsoup.connect(URL_AREA_MESA + mesa).userAgent("Mozilla").get();
	}

	private static final String URL_PESO = "http://192.168.0.104/controleexpedicao/relgerencial2.php";
	private static final String URL_AREA_MESA = "http://192.168.0.104/controlecorte/mesadecorte.php?codmesa=";
}
