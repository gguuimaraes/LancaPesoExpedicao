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
		c = Calendar.getInstance();
		c2 = Calendar.getInstance();
		c2.add(Calendar.DATE, 1);
		return Jsoup.connect(String.format(url, df.format(c.getTime()), df.format(c2.getTime()))).userAgent("Mozilla")
				.get();
	}

	public static final String URL_PESO_EXPEDICAO = "http://192.168.0.104/webservice/wsexpedicoespordia.php?datainicial=%s&datafinal=%s";
	public static final String URL_PESO_ENTREGA = "http://192.168.0.104/webservice/wsentregapordia.php?datainicial=%s&datafinal=%s";
	public static final String URL_AREA_MESA = "http://192.168.0.104/webservice/wscontroledecorte.php?datainicial=%s&datafinal=%s";
}
