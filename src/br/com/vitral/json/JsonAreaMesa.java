package br.com.vitral.json;

import java.util.Date;

public class JsonAreaMesa {
	private Integer codmesa;
	private Float area;
	private Date data;
	private Integer qtd;

	public JsonAreaMesa(Integer codmesa, Float area, Date data, Integer qtd) {
		super();
		this.codmesa = codmesa;
		this.area = area;
		this.data = data;
		this.qtd = qtd;
	}

	public Integer getCodmesa() {
		return codmesa;
	}

	public void setCodmesa(Integer codmesa) {
		this.codmesa = codmesa;
	}

	public Float getArea() {
		return area;
	}

	public void setArea(Float area) {
		this.area = area;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Integer getQtd() {
		return qtd;
	}

	public void setQtd(Integer qtd) {
		this.qtd = qtd;
	}

	@Override
	public String toString() {
		return "JsonAreaMesa [codmesa=" + codmesa + ", area=" + area + ", data=" + data + ", qtd=" + qtd + "]";
	}

}
