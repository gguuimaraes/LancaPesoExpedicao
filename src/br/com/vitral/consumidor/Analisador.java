package br.com.vitral.consumidor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import br.com.vitral.json.JSONArray;
import br.com.vitral.json.JSONObject;
import br.com.vitral.json.modelo.JsonAreaMesa;
import br.com.vitral.json.modelo.JsonPesoEntrega;
import br.com.vitral.json.modelo.JsonPesoExpedicao;

public abstract class Analisador {

	public static List<JsonAreaMesa> areaMesa() throws Exception {
		List<JsonAreaMesa> areas = new ArrayList<>();
		try {
			JSONArray array = extrairJsonArray(Pagina.obterPagina(Pagina.URL_AREA_MESA));
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				areas.add(
						new JsonAreaMesa(obj.getInt("codmesa"), obj.getFloat("area"), df.parse(obj.getString("data"))));
			}
		} catch (Exception e) {
			throw new Exception(String.format("Falha em obter área mesa.%n%s", e.toString()));
		}
		return areas;
	}

	public static List<JsonPesoEntrega> pesoEntrega() throws Exception {
		List<JsonPesoEntrega> pesos = new ArrayList<>();
		try {
			JSONArray array = extrairJsonArray(Pagina.obterPagina(Pagina.URL_PESO_ENTREGA));
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				Float peso = obj.getFloat("peso");
				if (peso > 0.f) {
					pesos.add(new JsonPesoEntrega(obj.getString("nome"), peso));
				}
			}

		} catch (Exception e) {
			throw new Exception(String.format("Falha em obter peso entrega.%n%s", e.toString()));
		}
		return pesos;
	}

	public static List<JsonPesoExpedicao> pesoExpedicao() throws Exception {
		List<JsonPesoExpedicao> pesos = new ArrayList<>();
		try {
			JSONArray array = extrairJsonArray(Pagina.obterPagina(Pagina.URL_PESO_EXPEDICAO));

			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = array.getJSONObject(i);
				pesos.add(new JsonPesoExpedicao(obj.getString("expedidor"), obj.getInt("qtdpecas"),
						obj.getFloat("peso"), obj.getString("expedicao")));
			}

		} catch (Exception e) {
			throw new Exception(String.format("Falha em obter peso expedição.%n%s", e.toString()));
		}
		return pesos;
	}

	private static JSONArray extrairJsonArray(Document doc) {
		return new JSONArray(doc.getElementsByTag("body").get(0).text().substring(1));
	}
}
