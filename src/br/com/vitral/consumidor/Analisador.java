package br.com.vitral.consumidor;

import java.util.List;

import org.jsoup.nodes.Document;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.com.vitral.json.JsonAreaMesa;
import br.com.vitral.json.JsonPesoEntrega;
import br.com.vitral.json.JsonPesoExpedicao;

public abstract class Analisador {

	public static List<JsonAreaMesa> areaMesa() throws Exception {
		try {
			return new Gson().fromJson(extrairJson(Pagina.obterPagina(Pagina.URL_AREA_MESA)),
					new TypeToken<List<JsonAreaMesa>>() {
					}.getType());
		} catch (Exception e) {
			throw new Exception(String.format("Falha em obter área mesa.%n%s", e.toString()));
		}
	}

	public static List<JsonPesoEntrega> pesoEntrega() throws Exception {
		try {
			return new Gson().fromJson(extrairJson(Pagina.obterPagina(Pagina.URL_PESO_ENTREGA)),
					new TypeToken<List<JsonPesoEntrega>>() {
					}.getType());
		} catch (Exception e) {
			throw new Exception(String.format("Falha em obter peso entrega.%n%s", e.toString()));
		}
	}

	public static List<JsonPesoExpedicao> pesoExpedicao() throws Exception {
		try {
			return new Gson().fromJson(extrairJson(Pagina.obterPagina(Pagina.URL_PESO_EXPEDICAO)),
					new TypeToken<List<JsonPesoExpedicao>>() {
					}.getType());
		} catch (Exception e) {
			throw new Exception(String.format("Falha em obter peso expedição.%n%s", e.toString()));
		}
	}

	private static String extrairJson(Document doc) {
		return doc.getElementsByTag("body").get(0).text().substring(1);
	}
}
