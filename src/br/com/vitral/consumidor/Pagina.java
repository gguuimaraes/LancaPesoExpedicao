package br.com.vitral.consumidor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Pagina {
	private static Calendar c, c2;
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	public static Document obterPagina(String url) throws IOException {
		if (url.equals(URL_PESO_EXPEDICAO_ANTIGO)) {
			return pesoExpedicaoAntigo();
		} else {
			c = Calendar.getInstance();
			c2 = Calendar.getInstance();
			c2.add(Calendar.DATE, 1);

			return Jsoup.connect(String.format(url, df.format(c.getTime()), df.format(c2.getTime())))
					.userAgent("Mozilla").get();
		}
	}
	
	private static Document pesoExpedicaoAntigo() throws IOException {
		c = Calendar.getInstance();
		c2 = Calendar.getInstance();
		c2.add(Calendar.DATE, 1);
		return Jsoup.connect(Pagina.URL_PESO_EXPEDICAO_ANTIGO).data("datainicial", df.format(c.getTime()))
				.data("datafinal", df.format(c2.getTime())).userAgent("Mozilla").post();
	}

	private static Document areaMesa() throws IOException {
		return Pagina.obterPagina(URL_AREA_MESA);
	}

	private static Document pesoEntrega() throws IOException {
		return Pagina.obterPagina(URL_PESO_ENTREGA);
	}
	
	private static Document pesoExpedicao() throws IOException {
		return Pagina.obterPagina(URL_PESO_EXPEDICAO);
	}

	public static final String URL_PESO_EXPEDICAO_ANTIGO = "http://192.168.0.104/controleexpedicao/relgerencial2.php";
	public static final String URL_PESO_EXPEDICAO = "http://192.168.0.104/webservice/wsexpedicoespordia.php?datainicial=%s&datafinal=%s";
	public static final String URL_PESO_ENTREGA = "http://192.168.0.104/webservice/wsentregapordia.php?datainicial=%s&datafinal=%s";
	public static final String URL_AREA_MESA = "http://192.168.0.104/webservice/wscontroledecorte.php?datainicial=%s&datafinal=%s";
}
