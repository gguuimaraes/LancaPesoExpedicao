package br.com.vitral.json.modelo;

public class JsonPesoExpedicao {

	private String expedidor;
	private Integer qtdpecas;
	private Float peso;
	private String expedicao;

	public JsonPesoExpedicao(String expedidor, Integer qtdpecas, Float peso, String expedicao) {
		super();
		this.expedidor = expedidor;
		this.qtdpecas = qtdpecas;
		this.peso = peso;
		this.expedicao = expedicao;
	}

	public String getExpedidor() {
		return expedidor;
	}

	public void setExpedidor(String expedidor) {
		this.expedidor = expedidor;
	}

	public Integer getQtdpecas() {
		return qtdpecas;
	}

	public void setQtdpecas(Integer qtdpecas) {
		this.qtdpecas = qtdpecas;
	}

	public Float getPeso() {
		return peso;
	}

	public void setPeso(Float peso) {
		this.peso = peso;
	}

	public String getExpedicao() {
		return expedicao;
	}

	public void setExpedicao(String expedicao) {
		this.expedicao = expedicao;
	}

}
